# 體驗活動模組 (farmtrip) — 邏輯說明文件

> 這份文件把整個體驗活動後端的每一條邏輯拆開講清楚，方便理解與後續維護。
> 程式位置：`com.kuanyu.blogdemo123.farmtrip`

---

## 0. 先看懂分層架構

資料流是一條龍：

```
前端 HTTP 請求
   ↓
Controller   只做「轉接」：收請求 → 呼叫 service → 包成 HTTP 回應
   ↓
Service      所有「商業邏輯」：規則判斷、跨表協調、多步驟交易
   ↓
Repository   只做「撈/存資料」：findBy... / save / delete
   ↓
資料庫 (MySQL: farmily)
```

一句話記住每層的職責：

| 層 | 只負責一件事 |
|---|---|
| Controller | 把 HTTP 請求轉給 service，把結果包成回應（薄薄一層） |
| Service | 「該怎麼做」的判斷與規則（厚的一層，邏輯都在這） |
| Repository | 「怎麼撈資料」（繼承 JpaRepository，幾乎不用寫實作） |
| Entity | 對應資料表的一筆資料長什麼樣 |

---

## 1. 資料表 / Entity 對照

模組共 5 張表，全部用**原始外鍵**（外鍵存 `Integer` id，沒有用 `@ManyToOne` 物件關聯）。

| Entity | 資料表 | 用途 | 外鍵欄位 |
|---|---|---|---|
| `FarmTrip` | `farm_trip` | 體驗活動主體 | `farmerId` |
| `FarmTripSession` | `farm_trip_session` | 場次 | `farmTripId` |
| `FarmTripAudit` | `farm_trip_audits` | 審核紀錄 | `farmTripId`, `adminId` |
| `FarmTripOrder` | `farm_trip_order` | 預約訂單 | `farmSessionId`, `userId` |
| `FarmTripComment` | `farm_trip_comment` | 評論 | `farmTripId`, `userId` |

**外鍵串連關係**（很重要，後面「小農查訂單」會用到）：

```
小農 farmer
  └─(farmer_id)→ 活動 FarmTrip
                   └─(farm_trip_id)→ 場次 FarmTripSession
                                       └─(farm_session_id)→ 訂單 FarmTripOrder
```

注意：**訂單只記到「場次」，沒有直接記「哪個小農」**。這是後面 B 方案跨表查詢的根本原因。

### 5 個列舉 (enum)

| Enum | 值 |
|---|---|
| `TripType` | `FARM_EXPERIENCE`(農作體驗), `FIELD_VISIT`(產地參訪) |
| `FarmTripStatus` | `PENDING`(待審), `REJECTED`(退回), `ACTIVE`(上架), `CLOSED`(下架) |
| `FarmTripSessionStatus` | `ACTIVE`(開放), `CANCELLED`(取消), `COMPLETED`(結束) |
| `FarmTripAuditStatus` | `PENDING`, `APPROVED`(通過), `REJECTED`(退回) |
| `FarmTripOrderStatus` | `CONFIRMED`(已確認), `CANCELLED`(取消), `COMPLETED`(完成) |

---

## 2. 三個 Controller（依角色分流）

不是按資料表分，而是按「**誰在用、權限不同**」分。每個 controller 有自己的 base path，避免路由打架。

| Controller | base path | 角色 |
|---|---|---|
| `AdminFarmTripController` | `/admin/farm-trips` | 管理員（審核） |
| `FarmerFarmTripController` | `/farmer` | 小農（管自己的活動/場次/訂單） |
| `FarmTripController` | `/farm-trips` | 會員 + 公開瀏覽 |

---

## 3. 每一條 API 的邏輯（逐條說明）

### 🛡️ AdminFarmTripController（管理員）

#### ① 查看審核清單
- `GET /admin/farm-trips/audits/pending`
- **邏輯**：撈出所有 `status = PENDING` 的活動 → `findByStatus(PENDING)`。
- 就是「等著被審核」的活動清單。

#### ② 審核單個活動 ⭐(複雜)
- `POST /admin/farm-trips/{tripId}/audit`
- body 帶 `status`(APPROVED/REJECTED)、`reason`、`adminId`
- **邏輯（兩步，包在同一個交易 `@Transactional`）**：
  1. 寫一筆審核紀錄到 `farm_trip_audits`（自動補 `createdAt`/`updatedAt`）。
  2. **連動**更新活動狀態：
     - `APPROVED` → 活動變 `ACTIVE`（上架）
     - `REJECTED` → 活動變 `REJECTED`（退回）
- **為什麼要交易**：「寫紀錄」和「改狀態」必須一起成功；不能紀錄寫了、活動狀態卻沒變。`@Transactional` 保證兩步同生共死。

#### ③ 審核歷史
- `GET /admin/farm-trips/{tripId}/audits`
- **邏輯**：撈某活動的所有審核紀錄，**最新的排最前面** → `findByFarmTripIdOrderByCreatedAtDesc`。

---

### 🌾 FarmerFarmTripController（小農）

> 註：`farmerId` 目前暫用 `?farmerId=` 參數帶入，之後有登入機制要改成從登入身分取得。

#### 活動本體

| API | 邏輯 |
|---|---|
| `GET /farmer/farm-trips?farmerId=` | 查自己的活動 → `findByFarmerId` |
| `POST /farmer/farm-trips` | 新增活動，**後端強制** `status=PENDING`、評論統計歸零（見下方重點 A） |
| `PUT /farmer/farm-trips/{tripId}` | 修改活動，**只開放可編輯欄位**（見下方重點 A） |
| `DELETE /farmer/farm-trips/{tripId}` | 刪除活動 |

#### 場次 CRUD

| API | 邏輯 |
|---|---|
| `POST /farmer/farm-trips/{tripId}/sessions` | 單筆新增；沒帶狀態時**自動補 `ACTIVE`** |
| `POST /farmer/farm-trips/{tripId}/sessions/batch` | 批次新增（一次存多筆，包交易） |
| `PUT /farmer/sessions/{sessionId}` | 修改場次 |
| `DELETE /farmer/sessions/{sessionId}` | 刪除場次（⚠️ 見已知限制） |

#### 訂單管理

| API | 邏輯 |
|---|---|
| `GET /farmer/farm-trip-orders?farmerId=` | 小農查訂單（跨表，見下方重點 D） |
| `POST /farmer/farm-trip-orders/{orderId}/complete` | 完成訂單：status→`COMPLETED`，寫入 `completedAt` |

---

### 👤 FarmTripController（會員 / 公開）

> 註：`userId` 目前暫用 `?userId=` 參數帶入，之後改從登入取得。

#### 公開瀏覽

| API | 邏輯 |
|---|---|
| `GET /farm-trips` | 查全部上架中 → `findByStatus(ACTIVE)` |
| `GET /farm-trips/{id}` | 單個活動；查無回 **404** |
| `GET /farm-trips/{id}/comments` | 該活動評論，最新排前面 |

#### 會員預約

| API | 邏輯 |
|---|---|
| `POST /farm-trips/sessions/{sessionId}/orders` | 預約場次（**名額檢查**，見重點 B） |
| `GET /farm-trips/orders/mine?userId=` | 查自己所有預約 → `findByUserIdOrderByBookedAtDesc` |
| `POST /farm-trips/orders/{orderId}/cancel` | 取消預約（**自動釋出名額**，見重點 C） |

#### 會員評論

| API | 邏輯 |
|---|---|
| `POST /farm-trips/{id}/comments` | 發表評論（**回寫活動星數統計**，見重點 E） |

---

## 4. 五個「重點邏輯」深入講解

這五條是體驗活動最有料、也最該理解的地方。

### 重點 A：新增/修改活動時，「鎖死」敏感欄位

```java
// createTrip：新活動一律待審核、統計歸零
trip.setStatus(FarmTripStatus.PENDING);
trip.setCommentNumbers(0);
trip.setStarNumbers(0);
```

**為什麼**：絕不信任前端傳來的 `status` 和統計值。否則前端可以直接傳 `status=ACTIVE` 繞過審核，或亂塞星數灌分。

`updateTrip` 同理：只把 `title / intro / pic / location / referPrice / tripType` 這些可編輯欄位覆蓋回去，**`status`、`farmerId`、評論統計一律不動**。做法是先從資料庫撈出原本那筆，只改該改的欄位再存回去。

---

### 重點 B：預約場次的「名額檢查」⭐ 最複雜

```
1) 場次要存在，且 sessionStatus = ACTIVE 才能預約（COMPLETED/CANCELLED 直接擋）
2) 算目前已佔名額 = 該場次「所有 CONFIRMED 訂單的 numPeople 加總」
3) 已佔 + 這次要預約人數 > 場次上限(attendance) → 丟「名額不足」例外
4) 通過才建立訂單：status=CONFIRMED、bookedAt=現在
```

關鍵在**第 2 步只算 `CONFIRMED` 的訂單**。被取消的訂單因為狀態是 `CANCELLED`，自然不被算進去 → 帶出重點 C。

> 包 `@Transactional`：避免兩個人同時預約最後一個名額時，兩邊都算到「還有位子」而超賣。

---

### 重點 C：取消預約，名額「自動」釋出

```java
// cancelOrder：只把狀態改掉，不需要手動「加回名額」
order.setStatus(FarmTripOrderStatus.CANCELLED);
order.setCancelledAt(LocalDateTime.now());
```

**漂亮的地方**：因為名額計算（重點 B 第 2 步）只數 `CONFIRMED` 的訂單，一旦改成 `CANCELLED`，它就自動不佔名額了。**不需要另外寫一段「把名額加回去」的邏輯** → 少一段程式 = 少一個會出錯的地方。

（測試已驗證：滿 5/5 後取消 2 人，再預約 2 人會成功。）

> 另外有擋：只有 `CONFIRMED` 的訂單能取消，已完成/已取消的不能再取消。

---

### 重點 D：小農查訂單，跨三張表（B 方案 JPQL）⭐

問題根源：**訂單沒有直接記是哪個小農的**（只記到 `farm_session_id`）。要查「某小農所有活動的訂單」，得走：

```
訂單 →(farm_session_id) 場次 →(farm_trip_id) 活動 →(farmer_id) 小農
```

因為 entity 用的是原始外鍵（不是 `@ManyToOne`），不能用 `o.session.trip.farmer` 這種點號導覽，只能手寫 JPQL 把三張表「對齊外鍵」查（theta join）：

```sql
SELECT o FROM FarmTripOrder o, FarmTripSession s, FarmTrip t
WHERE o.farmSessionId = s.farmSessionId   -- 訂單 接 場次
  AND s.farmTripId    = t.farmTripId      -- 場次 接 活動
  AND t.farmerId      = :farmerId         -- 篩出這個小農
ORDER BY o.bookedAt DESC
```

`SELECT o` = 只要訂單本身；中間兩張表純粹是「搭橋」用。

> 對照：會員查自己訂單就**沒這問題**（訂單本身就有 `user_id`，一張表搞定）。差別只在「訂單有沒有直接記住那個人的 id」。

---

### 重點 E：發表評論，回寫活動的星數統計

```
1) 寫一筆評論到 farm_trip_comment（自動補 createdAt）
2) 回頭更新活動：comment_numbers +1、star_numbers += 這次的星數
```

**為什麼要回寫**：`farm_trip` 表上有 `comment_numbers`(評論數) 和 `star_numbers`(星數總和) 兩個統計欄位。維護這兩個值，前端要算平均星數時直接 `star_numbers / comment_numbers` 就好，**不用每次把所有評論撈出來重算**。

> 一樣是兩步要一起成功，所以包 `@Transactional`。
> 測試已驗證：發一則 star=4 的評論後，活動 `comment_numbers` 1→2、`star_numbers` 5→9。

---

## 5. 共用設計

- **查無資料**：用一個小工具 `getOrThrow(...)` 統一處理 — 找不到就丟例外，不用每個方法重複寫 if-null。唯一例外是「查單個活動」回 `null`，讓 controller 自己決定回 404。
- **交易 `@Transactional`**：凡是「一個動作要動到多張表/多筆寫入」的，都包交易（審核、批次新增場次、預約、評論）。

---

## 6. 測試結果（已實際啟動 App 驗證）

App 啟動成功（port 8080，連 `farmily` DB），以下皆通過：

| 測試 | 結果 |
|---|---|
| 公開查全部 ACTIVE | 5 筆 ✅ |
| 單個活動 / 評論列表 | ✅ |
| 小農查自己的活動 (farmerId=2) | 2 筆 ✅ |
| **小農查訂單（B 跨表 JPQL）** | 2 筆 ✅ |
| 會員查自己預約 (userId=5) | 2 筆 ✅ |
| 待審清單 / 審核歷史 | ✅ |
| **發表評論 → 回寫星數** | comment 1→2, star 5→9 ✅ |
| **預約名額檢查**（2→9999擋→3滿→1擋） | ✅ 全對 |
| **取消後名額釋出 → 可再預約** | ✅ |

---

## 7. 已知限制 / 之後要補（誠實列出）

1. **「目前登入者」尚未處理**：`farmerId`/`userId` 暫用 `@RequestParam` 帶。⚠️ 有登入機制後必須改成從 token/session 取得，**不能讓前端自己宣稱「我是 3 號小農」**（這是安全漏洞）。
2. **權限未控管**：目前誰都能呼叫 admin 的審核 API。
3. **例外處理粗糙**：邏輯錯誤（名額不足、找不到）目前會回 HTTP 500。建議加一個全域 `@RestControllerAdvice`，把「名額不足」→ 400、「找不到」→ 404，回應才漂亮。
4. **request body 還是 entity**：之後建議換成 DTO，把「目前登入者」這類欄位擋在外面、也能做欄位驗證。
5. **刪除場次未防外鍵**：`DELETE /farmer/sessions/{id}` 若該場次底下已有訂單，會違反外鍵約束而報 500。應先檢查「有訂單就不准刪」或改為「軟刪除」（把 sessionStatus 設成 CANCELLED）。
6. **沒有「查場次」公開 endpoint**：目前會員端看不到某活動的場次清單，預約前需要場次 id。之後應補一條 `GET /farm-trips/{id}/sessions`。

---

## 8. 檔案清單

```
farmtrip/
├── controller/
│   ├── AdminFarmTripController.java     管理員：審核
│   ├── FarmerFarmTripController.java    小農：活動/場次/訂單
│   └── FarmTripController.java          會員/公開：瀏覽/預約/評論
├── entity/
│   ├── FarmTrip.java + FarmTripStatus / TripType
│   ├── FarmTripSession.java + FarmTripSessionStatus
│   ├── FarmTripAudit.java + FarmTripAuditStatus
│   ├── FarmTripOrder.java + FarmTripOrderStatus
│   └── FarmTripComment.java
├── repository/
│   ├── FarmTripRepository.java          findByStatus / findByFarmerId
│   ├── FarmTripSessionRepository.java   findByFarmTripId
│   ├── FarmTripAuditRepository.java     findByFarmTripIdOrderByCreatedAtDesc
│   ├── FarmTripOrderRepository.java     findByUserId... / findByFarmSessionId / 【B方案 @Query】
│   └── FarmTripCommentRepository.java   findByFarmTripIdOrderByCreatedAtDesc
└── service/
    ├── FarmTripService.java             介面
    └── impl/FarmTripServiceImpl.java    實作（所有商業邏輯）
```
