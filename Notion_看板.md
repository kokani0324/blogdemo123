# 🗂️ 開發看板 — 部落格 + 體驗活動

> 用法:把下面表格貼進 Notion → 選整個表格 → `•••` → **Turn into database** → 右上切 **Board** 視圖 → Group by 設為「狀態」。
> 之後每個任務變成一張卡,直接拖拉跨欄(待辦 → 進行中 → 完成)。

| 任務 | 階段 | 類型 | 完成標準 | 狀態 |
| --- | --- | --- | --- | --- |
| 修復啟動依賴衝突(Jackson) | 前置 | 後端 | app 能正常啟動 | ✅ 完成 |
| 部落格 查全部 / 查單筆 API | 前置 | 後端 | `/blogs`、`/blogs/{id}` 正常 | ✅ 完成 |
| 前端三頁轉 Vue(首頁/列表/活動) | 前置 | 前端 | 版面與原站一致 | ✅ 完成 |
| 查詢:過濾 + 排序 + 分頁 | Day 1 | 後端 | `/blogs` 帶參數回分頁 | ⬜ 待辦 |
| 分類清單 `GET /blog_types` | Day 1 | 後端 | 回分類陣列、前端 pill 出現 | ⬜ 待辦 |
| 文章新增 `POST /blogs` | Day 2 | 後端 | 能新增一篇含圖文章 | ⬜ 待辦 |
| 文章修改 / 刪除 `PUT`/`DELETE` | Day 2 | 後端 | 能改、能刪 | ⬜ 待辦 |
| 圖片上傳(blog_img) | Day 2 | 後端 | 圖片能存能取 | ⬜ 待辦 |
| 留言 CRUD(blog_comment) | Day 3 | 後端 | 留言可增 / 查 / 刪 | ⬜ 待辦 |
| 按讚 `POST /blogs/{id}/like` | Day 3 | 後端 | 讚數 +1 | ⬜ 待辦 |
| 相片集(blog_photo) | Day 4 | 後端 | 多圖可增 / 查 / 刪 | ⬜ 待辦 |
| 檢舉文章 / 留言 | Day 4 | 後端 | 可檢舉 blog 與 comment | ⬜ 待辦 |
| 列表頁:分頁 + 封面圖(Vue) | Day 5 | 前端 | 翻頁 / 搜尋 / 封面正常 | ⬜ 待辦 |
| 詳情頁 `blog-detail.html`(Vue) | Day 6 | 前端 | 內容 / 相片 / 留言 / 按讚可用 | ⬜ 待辦 |
| 發文 / 編輯頁 `blog-form.html`(Vue) | Day 7 | 前端 | 網頁能發新文章 🎉主責完成 | ⬜ 待辦 |
| 體驗活動 基礎 CRUD(FarmTrip) | Day 8 | 後端 | `/farm_trips` 列表/詳情/增改刪 | ⬜ 待辦 |
| 體驗活動 交接文件 | Day 8 | 交接 | 隊友能接著做場次/報名 | ⬜ 待辦 |
| 農場座標 API `GET /farms` | Day 9 | 後端 | 回經緯度 + 名稱 + 簡介 | ⬜ 待辦 |
| 產地地圖 `farm-map.html`(Leaflet) | Day 9 | 前端 | 地圖標農場、可點選 | ⬜ 待辦 |
| 整合測試 + 修 bug | Day 10 | 測試 | 三大功能都能從前端操作 | ⬜ 待辦 |
| 統一錯誤處理(GlobalExceptionHandler) | Day 10 | 後端 | 錯誤回傳格式一致 | ⬜ 待辦 |

---

## 建議的 Notion 欄位(屬性)設定

| 屬性 | 型別 | 選項 |
| --- | --- | --- |
| 狀態 | Select | ✅ 完成 / 🟡 進行中 / ⬜ 待辦 |
| 類型 | Select | 後端 / 前端 / 交接 / 測試 |
| 階段 | Select | 前置 / Day 1 … Day 10 |
| 完成標準 | Text | 該任務「做完」的判斷 |

- **Board 視圖**:Group by「狀態」→ 拖拉追進度
- **另存 Timeline / Calendar 視圖**:Group/排序用「階段」→ 看兩週時間軸
- 想看「今天做什麼」:用 Filter 篩「階段 = Day X」
