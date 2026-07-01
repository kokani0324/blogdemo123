# 🌾 部落格 + 體驗活動 開發行程表(兩週)

> 負責人:Gary｜目標:兩週交出可運作成果(部落格做完整、體驗活動做基礎)
> 後端:Spring Boot(springboot-mall 風格)｜前端:Vue 免安裝版(放 `resources/static/`)｜地圖:Leaflet

---

## ✅ 開始前已完成(底子)

- [x] 啟動問題修好(Jackson / spring-cloud 依賴衝突)
- [x] 部落格 `GET /blogs`(查全部)、`GET /blogs/{id}`(查單筆)
- [x] 前端三頁搬好並轉 Vue:`index.html`(首頁)、`blogs.html`(列表)、`farm-trips.html`(活動)
- [x] 共用資源到位:`styles.css`、`vue.global.js`、`auth.js`、`header.js`

---

## 🗓️ 第一週 — 部落格後端為主

### 📌 Day 1｜部落格查詢完整化

**目標**:讓 `/blogs` 支援過濾、排序、分頁(對齊 springboot-mall 的 Product)

**後端**
- [ ] 建 `BlogQueryParms` DTO:`blogTypeId`、`search`、`orderBy`、`sort`、`limit`、`offset`
- [ ] `BlogDao.getBlogs(BlogQueryParms)`:`WHERE 1=1` 動態接 `AND blog_type_id = :blogTypeId`、`AND blog_title LIKE :search`
- [ ] `BlogDao.countBlogs(BlogQueryParms)`:回總筆數
- [ ] Controller 用 `Page<Blog>` 回傳(`limit`/`offset`/`total`/`results`)
- [ ] `GET /blog_types`:分類清單(給前端下拉)

**前端**
- [ ] `blogs.html` 已相容分頁回傳,確認分類 pill 會自動出現

**✅ 完成標準**:`/blogs?blogTypeId=1&search=米&orderBy=blog_time&sort=desc&limit=5&offset=0` 回正確分頁;`blogs.html` 分類過濾可用

**📖 參考**:`springboot-mall` 的 `ProductController` / `ProductDao` / `ProductQueryParms` / `util/Page`

---

### 📌 Day 2｜文章 新增 / 修改 / 刪除

**目標**:文章 CRUD + 圖片

**後端**
- [ ] `BlogRequest` DTO(發文欄位:title、content、typeId、img…)
- [ ] `POST /blogs`(新增)
- [ ] `PUT /blogs/{id}`(修改)
- [ ] `DELETE /blogs/{id}`(刪除)
- [ ] 圖片 `blog_img`:`MultipartFile` 上傳或前端傳 base64,存 `byte[]`

**✅ 完成標準**:Postman 能完整新增 / 修改 / 刪除一篇含圖文章

**📖 參考**:`ProductController` 的 create/update/delete

---

### 📌 Day 3｜留言 + 按讚

**後端**
- [ ] `blog_comment`:`GET /blogs/{id}/comments`、`POST /blogs/{id}/comments`、`DELETE /comments/{id}`
- [ ] `BlogComment` 的 model / dao / rowmapper
- [ ] 按讚:`POST /blogs/{id}/like`(`blog_like_count` +1)

**✅ 完成標準**:能對文章留言、看留言列表、按讚數會增加

---

### 📌 Day 4｜相片集 + 檢舉

**後端**
- [ ] `blog_photo`:`GET /blogs/{id}/photos`、`POST /blogs/{id}/photos`、`DELETE /photos/{id}`(一篇多張圖)
- [ ] `blog_report`(檢舉文章):`POST /blogs/{id}/reports`
- [ ] `blog_comment_report`(檢舉留言):`POST /comments/{id}/reports`

**✅ 完成標準**:能上傳多張相片、能檢舉文章與留言

---

### 📌 Day 5｜部落格列表頁收尾(Vue)

**前端**
- [ ] `blogs.html`:把分頁接成真的(上一頁/下一頁,用 `ThePaginator.js` 概念)
- [ ] 確認分類過濾、搜尋、卡片顯示都正常
- [ ] 卡片補上封面圖(`blog_img`)

**✅ 完成標準**:瀏覽器開 `blogs.html` 能過濾、搜尋、翻頁、看到封面圖

**📖 參考**:`C:\vue_class\practice\components\ThePaginator.js`、`vue\vue_fetch.js`

---

## 🗓️ 第二週 — 部落格前端收尾 + 活動 + 地圖

### 📌 Day 6｜部落格詳情頁(Vue)

**前端**
- [ ] `blog-detail.html` + Vue:用網址 `?id=` 抓 `GET /blogs/{id}`
- [ ] 顯示:標題、內容、封面、相片集、按讚鈕、留言列表
- [ ] 留言送出(`POST`)、按讚(`POST .../like`)、檢舉鈕

**✅ 完成標準**:從列表點進詳情 → 看內容/相片 → 留言/按讚都能動

---

### 📌 Day 7｜部落格發文 / 編輯頁(Vue)→ 主責完成 🎉

**前端**
- [ ] `blog-form.html` + Vue:發文與編輯共用表單(`v-model`)
- [ ] 封面圖 + 相片集上傳
- [ ] 新增走 `POST`、編輯走 `PUT`

**✅ 完成標準**:能從網頁發一篇新文章並出現在列表
**🎯 到這裡「部落格(你的主責)」全部完成,後面延誤也不影響交付**

---

### 📌 Day 8｜體驗活動 後端基礎 + 交接文件

**後端(只做基礎)**
- [ ] `FarmTrip` 的 model / dao / service / controller
- [ ] `GET /farm_trips`(列表)、`GET /farm_trips/{id}`(詳情)、新增 / 修改 / 刪除
- [ ] ⚠️ 場次、報名、評論、審核**不做**

**交接**
- [ ] 寫交接文件:後續資料表、預計 API、可參考 farm-platform 哪些檔(`farmtrip` 模組)

**✅ 完成標準**:活動 CRUD 可用(`farm-trips.html` 自動顯示活動)+ 交接文件完成

---

### 📌 Day 9｜產地地圖(Leaflet)

**後端**
- [ ] `GET /farms`:回農場清單(含經緯度 lat/lng + 名稱 + 簡介)
- [ ] ⚠️ 先確認資料庫農場有沒有經緯度,沒有要補幾筆測試資料

**前端**
- [ ] `farm-map.html`:Leaflet 地圖 + 農場 marker + 點選顯示資訊
- [ ] 搬 farm-platform 的 `farm-map.html` / `js\farm-map.js` 改

**✅ 完成標準**:地圖顯示農場位置、可點選看資訊

**📖 參考**:`C:\farm-platform\...\static\farm-map.html`、`js\farm-map.js`

---

### 📌 Day 10｜整合測試 + 緩衝

- [ ] 兩個功能 + 地圖整串測一遍、修 bug
- [ ] 統一錯誤處理(`GlobalExceptionHandler`)
- [ ] 首頁四個區塊資料顯示確認

**✅ 完成標準**:部落格 + 體驗活動 + 地圖都能從前端正常操作

---

## ⚠️ 風險與提醒

| 風險 | 對策 |
| --- | --- |
| 圖片上傳卡關(新手最常見) | Day 2 / 4 多留時間,卡住先用 base64 |
| 進度延誤 | 部落格(主責)排前面,先確保核心交付 |
| 陷進體驗活動 | 只做基礎 CRUD,場次/報名交接給隊友 |
| 地圖沒座標資料 | Day 9 前先補農場經緯度 |
| 每天累積半成品 | 先達「完成標準」再往下 |

## 🆘 卡住時 SOP

1. 先判斷哪一層出錯:Controller → Service → Dao → SQL → 前端 fetch
2. 對照 `springboot-mall`(後端)或 `vue_class`(前端)同樣功能
3. 還是卡 → 把錯誤訊息丟給 Claude

---

## 📊 Notion 小技巧

- 這份貼進 Notion 後,`- [ ]` 會變成可勾選的 To-do
- 建議把每個 **Day** 轉成 Notion 的 toggle(▸)或獨立子頁面,收合後看整體進度
- 可加一個「狀態」欄位(未開始 / 進行中 / 完成)做成看板(Board)視圖
