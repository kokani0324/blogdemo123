# 體驗活動模組 (farmtrip) — 邏輯說明文件

> 這份文件把整個體驗活動後端的每一條邏輯拆開講清楚，方便理解與後續維護。
> 程式位置：`com.kuanyu.blogdemo123.farmtrip`

---

## 0. 先看懂分層架構

> 架構已對齊參考專案（farm-platform）：**DTO ↔ entity 的轉換都在 service 層**，
> controller 只轉發、不碰 entity。前端進出一律用 DTO。

資料流是一條龍：

```
前端 HTTP 請求 (JSON)
   ↓  收 XxxRequest（@Valid 自動驗證，錯的回 400）
Controller   只做「轉發」：把 Request 丟給 service → 回 service 給的 Response
   ↓  傳 XxxRequest
Service      商業邏輯 + 轉換：Request→entity、跑規則、entity→Response
   ↓  用 entity 操作
Repository   只做「撈/存資料」：findBy... / save / delete
   ↓
資料庫 (MySQL: farmily)
```

一句話記住每層的職責：

| 層 | 只負責一件事 |
|---|---|
| Controller | 收 Request DTO（`@Valid`）→ 轉發給 service → 回 Response DTO（薄薄一層，不碰 entity） |
| Service | 「該怎麼做」的判斷與規則 + **DTO↔entity 轉換**（厚的一層，邏輯都在這） |
| Repository | 「怎麼撈資料」（繼承 JpaRepository，幾乎不用寫實作） |
| Entity | 對應資料表的一筆資料，只在 service / repository 內部流動 |
| DTO | 對前端的語言：`XxxRequest`（進來）/ `XxxResponse`（出去），放 `farmtrip/dto/` |

**轉換寫在哪**：
- `Request → entity`：service 內部的私有方法（如 `toSessionEntity`）或直接在方法裡 set。
- `entity → Response`：各 `XxxResponse` 的靜態 `from(entity)`，由 service 呼叫。
- controller 兩者都不碰。

---

## 1. 資料表 / Entity 對照

模組共 5 張表，全部用**原始外鍵**（外鍵存 `Integer` id，沒有用 `@ManyToOne` 物件關聯）。

| Entity | 資料表 | 用途 | 外鍵欄位 |
|---|---|---|---|
| `FarmTrip` | `farm_trip` | 體驗活動主體 | `farmerId` |
| `FarmTripSession` | `farm_trip_session` | 場次 | `farmTripId` |
| `FarmTripAudit` | `farm_trip_audits` | 審核紀錄 | `farmTripId`, `adminId`（待審時為 null） |
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
  1. 找出小農送審時建立的最新一筆 `PENDING` 紀錄，寫入 `adminId`、結果、理由與 `updatedAt`。
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
| `POST /farmer/farm-trips` | 新增活動，強制 `status=PENDING`、統計歸零，並建立一筆待審紀錄 |
| `PUT /farmer/farm-trips/{tripId}` | 修改活動後重新設為 `PENDING`，並建立下一輪待審紀錄；已待審時不可重複送出 |
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

### 重點 A：新增/修改活動時建立送審紀錄

```java
// createTrip：新活動一律待審核、統計歸零
trip.setStatus(FarmTripStatus.PENDING);
trip.setCommentNumbers(0);
trip.setStarNumbers(0);
FarmTrip savedTrip = farmTripRepository.save(trip);
createPendingAudit(savedTrip.getFarmTripId()); // adminId=null、status=PENDING
```

**為什麼**：絕不信任前端傳來的 `status` 和統計值。否則前端可以直接傳 `status=ACTIVE` 繞過審核，或亂塞星數灌分。

`updateTrip` 只覆蓋 `title / intro / pic / location / referPrice / tripType` 等可編輯欄位，`farmerId` 與評論統計不讓前端改；儲存時由後端把活動改回 `PENDING`，再新增一筆待審紀錄。若活動本來已是 `PENDING`，則阻止重複送審。

---

### 重點 B：預約成功後累加目前預約人數

```
1) 場次要存在，且 sessionStatus = ACTIVE 才能預約（COMPLETED/CANCELLED 直接擋）
2) 建立訂單：status=CONFIRMED、bookedAt=現在
3) 場次 attendance += 這次訂單的 numPeople
```

`attendance` 代表目前有效預約人數，不是人數上限，也不由前端指定。新增場次時由後端設為 `0`。

> 包 `@Transactional`：確保建立訂單與累加 `attendance` 要嘛一起成功，要嘛一起失敗。

---

### 重點 C：取消預約後扣回預約人數

```java
order.setStatus(FarmTripOrderStatus.CANCELLED);
order.setCancelledAt(LocalDateTime.now());

int attendance = session.getAttendance() == null ? 0 : session.getAttendance();
session.setAttendance(Math.max(0, attendance - order.getNumPeople()));
```

只有原本為 `CONFIRMED` 的訂單能取消，因此每筆訂單最多只會從 `attendance` 扣除一次；`Math.max(0, ...)` 則避免舊資料不一致時出現負數。

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

## 7. 已完成 & 已知限制

### ✅ 已完成（對齊參考專案）
- **全面 DTO**：所有對外 API 進出都用 `XxxRequest` / `XxxResponse`，不再外露 entity。
- **service 層轉換**：DTO↔entity 轉換全在 service，controller 只轉發。
- **Bean Validation**：Request DTO 加 `@NotNull/@NotBlank/@Size/@Min/@Max`，controller `@Valid`，前端傳錯自動回 **400**。

### ⚠️ 已知限制 / 之後要補（誠實列出）
1. **「目前登入者」尚未處理**：`farmerId`/`userId`/`adminId` 暫用 `@RequestParam` 或 DTO 帶。⚠️ 有登入機制後必須改成從 token/session 取得，**不能讓前端自己宣稱「我是 3 號小農」**（這是安全漏洞）。
2. **權限未控管**：目前誰都能呼叫 admin 的審核 API。
3. **例外處理粗糙**：商業邏輯錯誤（場次無法預約、找不到）目前會回 HTTP 500。建議加一個全域 `@RestControllerAdvice`，把「場次無法預約」→ 400、「找不到」→ 404，回應才漂亮。（欄位格式的錯誤已由 `@Valid` 處理成 400。）
4. **刪除場次未防外鍵**：`DELETE /farmer/sessions/{id}` 若該場次底下已有訂單，會違反外鍵約束而報 500。應先檢查「有訂單就不准刪」或改為「軟刪除」（把 sessionStatus 設成 CANCELLED）。
5. **沒有「查場次」公開 endpoint**：目前會員端看不到某活動的場次清單，預約前需要場次 id。之後應補一條 `GET /farm-trips/{id}/sessions`。
6. **service 是 interface + impl 拆分**（參考專案是單一 class）。維持 blogdemo123 既有 `BlogService` 慣例，兩種皆可。

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
├── dto/                                對前端的 Request/Response（含 @Valid 驗證、from() 轉換）
│   ├── FarmTripRequest / FarmTripResponse
│   ├── FarmTripSessionRequest / FarmTripSessionResponse
│   ├── FarmTripOrderRequest / FarmTripOrderResponse
│   ├── FarmTripCommentRequest / FarmTripCommentResponse
│   └── FarmTripAuditRequest / FarmTripAuditResponse
├── repository/
│   ├── FarmTripRepository.java          findByStatus / findByFarmerId
│   ├── FarmTripSessionRepository.java   findByFarmTripId
│   ├── FarmTripAuditRepository.java     審核歷史 / 最新一筆 PENDING
│   ├── FarmTripOrderRepository.java     findByUserId... / findByFarmSessionId / 【B方案 @Query】
│   └── FarmTripCommentRepository.java   findByFarmTripIdOrderByCreatedAtDesc
└── service/
    ├── FarmTripService.java             介面（吃 Request、回 Response）
    └── impl/FarmTripServiceImpl.java    實作（商業邏輯 + DTO↔entity 轉換）
```
