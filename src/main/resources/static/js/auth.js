/*
  你儂我農 — 登入 / 註冊 / 會話 共用函式(Phase A 重構版)

  帳號類型(AccountType):
    MEMBER  → localStorage  (key: nong_token, nong_user)
    FARMER  → sessionStorage(key: nong_backoffice_token, nong_backoffice_user)  關 tab 自動登出
    ADMIN   → sessionStorage(同上 backoffice key)

  AuthResponse 結構(後端 dto/AuthResponse.java):
    { token, tokenType, accountId, type, email, name }
*/
const NongAuth = (function () {
  const API_BASE = "";
  const CONSUMER_TOKEN_KEY = "nong_token";
  const CONSUMER_USER_KEY  = "nong_user";
  const BACKOFFICE_TOKEN_KEY = "nong_backoffice_token";
  const BACKOFFICE_USER_KEY  = "nong_backoffice_user";

  async function request(path, options = {}) {
    const headers = Object.assign(
      { "Content-Type": "application/json" },
      options.headers || {}
    );
    const token = getActiveToken();
    if (token && !headers.Authorization) headers.Authorization = `Bearer ${token}`;
    const res = await fetch(API_BASE + path, Object.assign({}, options, { headers }));
    if (!res.ok) {
      let msg = `${res.status} ${res.statusText}`;
      try {
        const ct = res.headers.get("content-type") || "";
        if (ct.includes("application/json")) {
          const body = await res.json();
          msg = body.message || body.error || JSON.stringify(body);
        } else {
          const text = await res.text();
          if (text) msg = text;
        }
      } catch (_e) { /* swallow */ }
      if (res.status === 401 && !isAuthPage() && !isAuthEndpoint(path)) {
        logoutAll();
        window.location.replace("login.html?reason=expired");
        return new Promise(() => {});
      }
      const err = new Error(msg);
      err.status = res.status;
      throw err;
    }
    if (res.status === 204) return null;
    return res.json();
  }

  function isAuthPage() {
    return document.body && document.body.dataset.page === "auth";
  }
  function isAuthEndpoint(path) {
    return typeof path === "string" && path.startsWith("/api/auth/");
  }

  /** 公開登入:userType ∈ { MEMBER, FARMER } */
  async function login(email, password, userType) {
    return request("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password, userType })
    });
  }

  /** 隱藏管理員登入:對應 /api/auth/admin/login */
  async function adminLogin(email, password) {
    return request("/api/auth/admin/login", {
      method: "POST",
      body: JSON.stringify({ email, password })
    });
  }

  /** 消費者註冊 */
  async function memberRegister(payload) {
    return request("/api/auth/member/register", {
      method: "POST",
      body: JSON.stringify(payload)
    });
  }

  /** 小農註冊:成功後不發 token,需待管理員審核 */
  async function farmerRegister(payload) {
    return request("/api/auth/farmer/register", {
      method: "POST",
      body: JSON.stringify(payload)
    });
  }

  function saveSession(authResp) {
    const type = authResp.type;
    if (type === "MEMBER") {
      localStorage.setItem(CONSUMER_TOKEN_KEY, authResp.token);
      localStorage.setItem(CONSUMER_USER_KEY, JSON.stringify(authResp));
      sessionStorage.removeItem(BACKOFFICE_TOKEN_KEY);
      sessionStorage.removeItem(BACKOFFICE_USER_KEY);
    } else { // FARMER / ADMIN
      sessionStorage.setItem(BACKOFFICE_TOKEN_KEY, authResp.token);
      sessionStorage.setItem(BACKOFFICE_USER_KEY, JSON.stringify(authResp));
      localStorage.removeItem(CONSUMER_TOKEN_KEY);
      localStorage.removeItem(CONSUMER_USER_KEY);
    }
  }

  function getConsumerUser() {
    const raw = localStorage.getItem(CONSUMER_USER_KEY);
    if (!raw) return null;
    try { return JSON.parse(raw); } catch { return null; }
  }
  function getConsumerToken() { return localStorage.getItem(CONSUMER_TOKEN_KEY); }

  function getBackofficeUser() {
    const raw = sessionStorage.getItem(BACKOFFICE_USER_KEY);
    if (!raw) return null;
    try { return JSON.parse(raw); } catch { return null; }
  }
  function getBackofficeToken() { return sessionStorage.getItem(BACKOFFICE_TOKEN_KEY); }

  function getActiveUser() {
    const page = document.body && document.body.dataset.page;
    if (page === "farmer" || page === "admin") return getBackofficeUser();
    return getConsumerUser();
  }
  function getActiveToken() {
    const page = document.body && document.body.dataset.page;
    if (page === "farmer" || page === "admin") return getBackofficeToken();
    return getConsumerToken();
  }

  function logoutAll() {
    localStorage.removeItem(CONSUMER_TOKEN_KEY);
    localStorage.removeItem(CONSUMER_USER_KEY);
    sessionStorage.removeItem(BACKOFFICE_TOKEN_KEY);
    sessionStorage.removeItem(BACKOFFICE_USER_KEY);
  }
  function logoutBackoffice() {
    sessionStorage.removeItem(BACKOFFICE_TOKEN_KEY);
    sessionStorage.removeItem(BACKOFFICE_USER_KEY);
  }

  function requireBackofficeType(expectedType) {
    const user = getBackofficeUser();
    const token = getBackofficeToken();
    if (!user || !token) {
      window.location.replace("login.html?reason=auth");
      return null;
    }
    if (user.type !== expectedType) {
      window.location.replace("login.html?reason=role");
      return null;
    }
    return user;
  }

  function redirectAfterLogin(authResp) {
    if (authResp.type === "ADMIN")  { window.location.href = "admin.html";  return; }
    if (authResp.type === "FARMER") { window.location.href = "farmer.html"; return; }
    window.location.href = "index.html";
  }

  return {
    request,
    login, adminLogin, memberRegister, farmerRegister,
    saveSession,
    getConsumerUser, getConsumerToken,
    getBackofficeUser, getBackofficeToken,
    getActiveUser, getActiveToken,
    logoutAll, logoutBackoffice,
    requireBackofficeType,
    requireBackofficeRole: requireBackofficeType,
    redirectAfterLogin
  };
})();
