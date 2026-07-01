/*
  共用 header：每個消費者頁面載入即執行，根據登入狀態渲染右上角，並把當前頁的 nav 加上 active 樣式。
  使用方式：頁面 <body data-page="consumer" data-active-nav="products"> + 在 <head> 之後載入 auth.js、本檔。
*/
(function clearBackofficeSessionOnConsumer() {
  // 進入任何消費者頁面時，主動清掉殘留的後台 session（離開後台 = 登出原則的最後一道）
  try {
    sessionStorage.removeItem("nong_backoffice_token");
    sessionStorage.removeItem("nong_backoffice_user");
  } catch (_e) { /* ignore */ }
})();

const NongHeader = (function () {
  function init() {
    injectHeaderHostIfNeeded();
    normalizeBrand();
    normalizeMainNav();
    enhanceProductNav();
    enhanceSectionNavMenus();
    highlightActiveNav();
    renderUserZone();
    bindMobileNav();
    refreshCartBadge();
    renderSharedFooter();
  }

  /**
   * 新頁面若用 <div id="headerHost"></div> 取代整個 <header> markup,
   * 這裡會自動注入；舊頁有 inline header 也沒關係,host 不存在就不做事。
   */
  function injectHeaderHostIfNeeded() {
    const host = document.getElementById("headerHost");
    if (!host) return;
    host.outerHTML = `
      <header class="site-header">
        <a class="brand" href="index.html" aria-label="Farmily 首頁">
          <span class="brand-seal">儂</span>
          <span><strong>你儂我農</strong><small>Farmily</small></span>
        </a>
        <button class="mobile-menu" id="mobileMenu" type="button" aria-label="開啟選單">☰</button>
        <nav class="main-nav" id="mainNav" aria-label="消費者主要功能">
          <div class="nav-item"><a href="news.html"       data-nav-key="news">最新消息</a></div>
          <div class="nav-item"><a href="products.html"   data-nav-key="products">全部商品</a></div>
          <div class="nav-item"><a href="group-buys.html" data-nav-key="group-buys">團購</a></div>
          <div class="nav-item"><a href="farm-trips.html" data-nav-key="farm-trips">體驗活動</a></div>
          <div class="nav-item"><a href="blogs.html"      data-nav-key="blogs">部落格</a></div>
          <div class="nav-item"><a href="farm-map.html"   data-nav-key="farm-map">產地地圖</a></div>
          <div class="user-zone" id="userZone"></div>
        </nav>
      </header>
    `;
  }

  function normalizeBrand() {
    document.querySelectorAll(".brand").forEach((brand) => {
      brand.setAttribute("aria-label", "Farmily 首頁");
      const seal = brand.querySelector(".brand-seal");
      const title = brand.querySelector("strong");
      const subtitle = brand.querySelector("small");
      if (seal) seal.textContent = "儂";
      if (title) title.textContent = "你儂我農";
      if (subtitle) subtitle.textContent = "Farmily";
    });
  }

  function normalizeMainNav() {
    const nav = document.getElementById("mainNav");
    if (!nav) return;

    const homeLink = nav.querySelector('[data-nav-key="home"]');
    homeLink?.closest(".nav-item")?.remove();

    let newsLink = nav.querySelector('[data-nav-key="news"]');
    let newsItem = newsLink?.closest(".nav-item");
    if (!newsItem) {
      nav.insertAdjacentHTML("afterbegin", `<div class="nav-item"><a href="news.html" data-nav-key="news">最新消息</a></div>`);
      newsLink = nav.querySelector('[data-nav-key="news"]');
      newsItem = newsLink?.closest(".nav-item");
    }
    if (newsItem && nav.firstElementChild !== newsItem) {
      nav.insertBefore(newsItem, nav.firstElementChild);
    }
    ensureProducerNav(nav);
    ensureFarmMapNav(nav);
  }

  function ensureProducerNav(nav) {
    if (!nav) return;
    let producerLink = nav.querySelector('[data-nav-key="producers"]');
    let producerItem = producerLink?.closest(".nav-item");
    if (!producerItem) {
      const newsItem = nav.querySelector('[data-nav-key="news"]')?.closest(".nav-item");
      const html = `<div class="nav-item"><a href="producers.html" data-nav-key="producers">農場</a></div>`;
      if (newsItem) {
        newsItem.insertAdjacentHTML("afterend", html);
      } else {
        nav.insertAdjacentHTML("afterbegin", html);
      }
      producerLink = nav.querySelector('[data-nav-key="producers"]');
      producerItem = producerLink?.closest(".nav-item");
    }
    if (producerLink) producerLink.textContent = "農場";
  }

  function ensureFarmMapNav(nav) {
    if (!nav) return;
    let mapLink = nav.querySelector('[data-nav-key="farm-map"]');
    let mapItem = mapLink?.closest(".nav-item");
    if (!mapItem) {
      const html = `<div class="nav-item"><a href="farm-map.html" data-nav-key="farm-map">產地地圖</a></div>`;
      const blogItem = nav.querySelector('[data-nav-key="blogs"]')?.closest(".nav-item");
      const userZone = document.getElementById("userZone");
      if (blogItem) {
        blogItem.insertAdjacentHTML("afterend", html);
      } else if (userZone) {
        userZone.insertAdjacentHTML("beforebegin", html);
      } else {
        nav.insertAdjacentHTML("beforeend", html);
      }
      mapLink = nav.querySelector('[data-nav-key="farm-map"]');
      mapItem = mapLink?.closest(".nav-item");
    }
    if (mapLink) {
      mapLink.href = "farm-map.html";
      mapLink.textContent = "產地地圖";
    }

    const blogItem = nav.querySelector('[data-nav-key="blogs"]')?.closest(".nav-item");
    if (blogItem && mapItem && blogItem.nextElementSibling !== mapItem) {
      blogItem.insertAdjacentElement("afterend", mapItem);
    }
  }

  function enhanceProductNav() {
    const link = document.querySelector('[data-nav-key="products"]');
    if (!link) return;
    link.textContent = "全部商品";
    const item = link.closest(".nav-item");
    if (!item) return;
    item.classList.add("product-nav-item");
    if (!item.querySelector(".product-category-menu")) {
      item.insertAdjacentHTML("beforeend", `
        <div class="dropdown product-category-menu" aria-label="商品分類選單">
          <a href="products.html">全部商品</a>
          <span class="dropdown-muted">分類載入中…</span>
        </div>
      `);
    }
    loadProductNavCategories(item.querySelector(".product-category-menu"));
  }

  async function loadProductNavCategories(menu) {
    if (!menu) return;
    try {
      const cats = await NongAuth.request("/api/public/categories");
      const list = Array.isArray(cats) ? cats : [];
      menu.innerHTML = [
        `<a href="products.html">全部商品</a>`,
        ...list.map((cat) => `<a href="products.html?categoryId=${encodeURIComponent(cat.id)}">${escapeHtml(cat.name)}</a>`)
      ].join("");
    } catch (_e) {
      menu.innerHTML = `<a href="products.html">全部商品</a>`;
    }
  }

  function enhanceSectionNavMenus() {
    setStaticDropdown("group-buys", [
      { href: "group-buys.html", label: "全部團購" },
      { href: "products.html", label: "從商品發起團購" },
      { href: "my-group-buy-requests.html", label: "我發起的團購" },
      { href: "my-group-buys.html", label: "我參加的團購" },
      { href: "my-group-buy-orders.html", label: "我的團購整單" }
    ]);

    setStaticDropdown("farm-trips", [
      { href: "farm-trips.html", label: "全部活動" },
      { href: "farm-trips.html?tripType=FARM_EXPERIENCE", label: "農場體驗" },
      { href: "farm-trips.html?tripType=FIELD_VISIT", label: "產地參訪" }
    ]);

    const blogMenu = setStaticDropdown("blogs", [
      { href: "blogs.html", label: "全部文章" }
    ], "文章分類載入中…");
    loadBlogNavTypes(blogMenu);
  }

  function setStaticDropdown(navKey, items, loadingText) {
    const link = document.querySelector(`[data-nav-key="${navKey}"]`);
    const item = link?.closest(".nav-item");
    if (!item) return null;
    item.classList.add("has-nav-menu");

    let menu = item.querySelector(".nav-dropdown-menu");
    if (!menu) {
      item.insertAdjacentHTML("beforeend", `<div class="dropdown nav-dropdown-menu"></div>`);
      menu = item.querySelector(".nav-dropdown-menu");
    }
    menu.innerHTML = [
      ...items.map((entry) => `<a href="${entry.href}">${escapeHtml(entry.label)}</a>`),
      loadingText ? `<span class="dropdown-muted">${escapeHtml(loadingText)}</span>` : ""
    ].join("");
    return menu;
  }

  async function loadBlogNavTypes(menu) {
    if (!menu) return;
    try {
      const types = await NongAuth.request("/api/blogs/types");
      const list = Array.isArray(types) ? types : [];
      const typeLinks = list.map((type) =>
        `<a href="blogs.html?typeId=${encodeURIComponent(type.id)}">${escapeHtml(type.name)}</a>`
      );
      menu.innerHTML = [
        `<a href="blogs.html">全部文章</a>`,
        ...typeLinks
      ].join("");
    } catch (_e) {
      menu.innerHTML = `<a href="blogs.html">全部文章</a>`;
    }
  }

  function highlightActiveNav() {
    const active = document.body.dataset.activeNav;
    if (!active) return;
    document.querySelectorAll("[data-nav-key]").forEach((el) => {
      el.classList.toggle("is-active", el.dataset.navKey === active);
    });
  }

  function renderUserZone() {
    const zone = document.getElementById("userZone");
    if (!zone) return;
    const user = NongAuth.getConsumerUser();
    if (!user) {
      zone.innerHTML = `
        <a class="user-btn ghost" href="login.html">登入</a>
        <a class="user-btn primary" href="register.html">註冊</a>
      `;
      return;
    }
    const type = user.type;
    const typeLabel = type === "FARMER" ? "小農" : type === "ADMIN" ? "管理員" : "會員";
    const initial = (user.name || user.email || "U").slice(0, 1);

    zone.innerHTML = `
      <a class="cart-icon" href="cart.html" aria-label="購物車">
        <span aria-hidden="true">🛒</span>
        <span class="cart-badge" id="cartBadge">0</span>
      </a>
      <span class="user-greeting">${typeLabel}・${escapeHtml(user.name || user.email)}</span>
      <div class="user-dropdown" id="userDropdown">
        <button class="user-avatar" type="button" aria-haspopup="true">${escapeHtml(initial)}</button>
        <div class="user-menu" role="menu">
          <div class="menu-head">
            <strong>${escapeHtml(user.name || "")}</strong>
            ${escapeHtml(user.email || "")}
          </div>
          ${buildUserMenuItems(user)}
        </div>
      </div>
    `;
    bindUserMenu();
  }

  function buildUserMenuItems(user) {
    const type = user.type;
    const isMember = type === "MEMBER";
    const isFarmer = type === "FARMER";
    const isAdmin  = type === "ADMIN";
    const items = [`<a href="profile.html">會員中心</a>`];

    if (isMember) {
      items.push(`<a href="orders.html">我的訂單</a>`);
      items.push(`<a href="wishlist.html">我的收藏 ♥</a>`);
      items.push(`<a href="my-group-buy-requests.html">我發起的團購</a>`);
      items.push(`<a href="my-group-buys.html">我參加的團購</a>`);
      items.push(`<a href="my-group-buy-orders.html">我的團購整單</a>`);
      items.push(`<a href="my-farm-trip-bookings.html">我的體驗預約</a>`);
      items.push(`<a href="my-blogs.html">我的文章</a>`);
    }
    if (isFarmer) {
      items.push(`<a href="farmer.html">前往小農工作台</a>`);
      items.push(`<a href="my-blogs.html">我的文章</a>`);
    }
    if (isAdmin) {
      items.push(`<div class="divider"></div>`);
      items.push(`<a href="admin.html">前往管理員後台</a>`);
    }

    // Phase A 拆分後一個帳號只屬一種身份,不再提供切換按鈕

    items.push(`<div class="divider"></div>`);
    items.push(`<button class="danger" data-action="logout">登出</button>`);
    return items.join("");
  }

  function bindUserMenu() {
    const dropdown = document.getElementById("userDropdown");
    if (!dropdown) return;
    dropdown.addEventListener("click", async (e) => {
      const target = e.target.closest("[data-action]");
      if (!target) return;
      const action = target.dataset.action;
      if (action === "logout") {
        NongAuth.logoutAll();
        window.location.href = "index.html";
      }
      // Phase A 拆分後拿掉 switchRole;若要在會員/小農之間切換,各自獨立帳號重新登入
    });

    const avatar = dropdown.querySelector(".user-avatar");
    avatar.addEventListener("click", (e) => {
      e.stopPropagation();
      dropdown.classList.toggle("is-open");
    });
    document.addEventListener("click", () => dropdown.classList.remove("is-open"));
  }

  function bindMobileNav() {
    const button = document.getElementById("mobileMenu");
    const nav = document.getElementById("mainNav");
    if (!button || !nav) return;
    button.addEventListener("click", () => nav.classList.toggle("is-open"));
  }

  async function refreshCartBadge() {
    const badge = document.getElementById("cartBadge");
    if (!badge) return;
    const user = NongAuth.getConsumerUser();
    if (!user || user.type !== "MEMBER") {
      badge.classList.remove("is-visible");
      return;
    }
    try {
      const cart = await NongAuth.request("/api/cart");
      const qty = cart?.totalQuantity ?? 0;
      badge.textContent = qty > 99 ? "99+" : String(qty);
      badge.classList.toggle("is-visible", qty > 0);
    } catch (_e) {
      badge.classList.remove("is-visible");
    }
  }

  function renderSharedFooter() {
    if (document.body.dataset.page !== "consumer") return;
    const footer = document.querySelector("footer.site-footer");
    const html = `
      <footer class="site-footer scenic-footer">
        <div class="footer-landscape" aria-hidden="true">
          <span class="cloud cloud-left"></span>
          <span class="cloud cloud-right"></span>
          <span class="hill hill-back"></span>
          <span class="hill hill-front"></span>
        </div>
        <div class="footer-content">
          <a class="footer-brand" href="index.html" aria-label="Farmily 首頁">
            <span class="brand-seal">儂</span>
            <strong>你儂我農</strong>
            <p>連結台灣小農與你的餐桌，吃得新鮮，也吃得安心。</p>
          </a>
          <div class="footer-links">
            <a href="producers.html">農場</a>
            <a href="news.html">最新消息</a>
            <a href="products.html">全部商品</a>
            <a href="group-buys.html">團購</a>
            <a href="farm-trips.html">體驗活動</a>
            <a href="blogs.html">部落格</a>
            <a href="login.html">登入 / 註冊</a>
          </div>
          <p class="footer-note">CKA101 第三組專題｜Spring Boot + MySQL｜from soil to table</p>
        </div>
      </footer>
    `;
    if (footer) {
      footer.outerHTML = html;
    } else {
      document.body.insertAdjacentHTML("beforeend", html);
    }
  }

  function escapeHtml(str) {
    return String(str ?? "").replace(/[&<>"']/g, (c) => ({
      "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    })[c]);
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }

  return { refreshCartBadge, renderUserZone, escapeHtml };
})();
