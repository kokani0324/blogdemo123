# 體驗活動模組 — 每個請求的完整流程追蹤

> 看法：**一次只看一節**。每一節都是一條完整請求，從前端打進來，一路經過哪個方法、做了什麼、怎麼回傳，全部串起來。
> 共通結構都是：`前端 → Controller 方法 → Service 方法 → Repository 方法 → 資料庫`，再原路包成回應回去。

> ### ⚠️ 架構已更新（2026-07 對齊 farm-platform）
> 本文部分程式片段是「舊版（controller 做轉換）」的示意。現行架構改成：
> - **Controller 只轉發**：收 `XxxRequest`（加 `@Valid` 自動驗證）→ 直接呼叫 service → 回 service 給的 `XxxResponse`。controller 不再自己做 DTO↔entity 轉換。
> - **Service 吃 `XxxRequest`、回 `XxxResponse`**：`Request→entity` 與 `entity→Response`（`XxxResponse.from()`）都在 service 內完成。
> - 所以下面每條若看到「controller 裡做轉換」或「service 回傳 entity」，請理解為**現在那段轉換是在 service 裡、回傳的是 Response DTO**。流程的「呼叫關係」不變，只是轉換的位置換了一層。
> - 已完整更新的範例見第 [4](#4)、[5](#5) 條。

## 怎麼挑你要看的那一條

| # | 請求 | 白話 |
|---|---|---|
| [1](#1) | `GET /farm-trips` | 公開：查全部上架活動 |
| [2](#2) | `GET /farm-trips/{id}` | 公開：看單一活動 |
| [3](#3) | `GET /farm-trips/{id}/comments` | 公開：看活動評論 |
| [4](#4) | `POST /farm-trips/{id}/comments` | 會員：發表評論 ⭐ |
| [5](#5) | `POST /farm-trips/sessions/{sessionId}/orders` | 會員：預約場次 ⭐ |
| [6](#6) | `GET /farm-trips/orders/mine` | 會員：查我的預約 |
| [7](#7) | `POST /farm-trips/orders/{orderId}/cancel` | 會員：取消預約 ⭐ |
| [8](#8) | `GET /farmer/farm-trips` | 小農：查我的活動 |
| [9](#9) | `POST /farmer/farm-trips` | 小農：新增活動 ⭐ |
| [10](#10) | `PUT /farmer/farm-trips/{tripId}` | 小農：修改活動 |
| [11](#11) | `DELETE /farmer/farm-trips/{tripId}` | 小農：刪除活動 |
| [12](#12) | `POST /farmer/farm-trips/{tripId}/sessions` | 小農：新增場次 |
| [13](#13) | `POST .../sessions/batch` | 小農：批次新增場次 |
| [14](#14) | `PUT /farmer/sessions/{sessionId}` | 小農：修改場次 |
| [15](#15) | `DELETE /farmer/sessions/{sessionId}` | 小農：刪除場次 |
| [16](#16) | `GET /farmer/farm-trip-orders` | 小農：查訂單（跨表）⭐ |
| [17](#17) | `POST /farmer/farm-trip-orders/{orderId}/complete` | 小農：完成訂單 |
| [18](#18) | `GET /admin/farm-trips/audits/pending` | 管理員：待審清單 |
| [19](#19) | `POST /admin/farm-trips/{tripId}/audit` | 管理員：審核 ⭐ |
| [20](#20) | `GET /admin/farm-trips/{tripId}/audits` | 管理員：審核歷史 |

---

## 先讀這個：怎麼看一條流程圖

```
前端  ──①請求──▶  Controller 方法
                      │ ②呼叫
                      ▼
                  Service 方法
                      │ ③呼叫
                      ▼
                  Repository 方法
                      │ ④下 SQL
                      ▼
                  資料庫
                      │ ⑤撈到資料
                      ▲ 原路回傳
前端  ◀──⑦JSON──  Controller 用 ResponseEntity 包好  ◀──⑥回傳──  Service
```

- **往下**：呼叫（controller 叫 service，service 叫 repository）。
- **往上**：回傳（資料一層層傳回來，最後 controller 包成 HTTP 回應）。
- `ResponseEntity` = 「HTTP 回應的盒子」，裡面裝「狀態碼（200/404…）+ 內容（JSON）」。

---

<a id="1"></a>
## 1. `GET /farm-trips` — 公開查全部上架活動

**前端送出**：`GET http://localhost:8080/farm-trips`（沒有 body）

### 流程

```
前端 ──GET /farm-trips──▶ FarmTripController.getAll()
                              │
                              ▼ farmTripService.getActiveTrips()
                         FarmTripServiceImpl.getActiveTrips()
                              │
                              ▼ farmTripRepository.findByStatus(ACTIVE)
                         FarmTripRepository.findByStatus()
                              │
                              ▼ Spring Data 自動產生 SQL
                         DB: SELECT * FROM farm_trip WHERE status = 'ACTIVE'
                              │ 回傳 List<FarmTrip>
                              ▲
                         Service 直接 return 這個 List
                              ▲
                         Controller: ResponseEntity.ok(list) → HTTP 200 + JSON 陣列
                              ▲
前端 ◀──── 200 + [5筆活動的JSON] ────┘
```

### 一層一層看

**① Controller**（`FarmTripController`）
```java
@GetMapping                                  // 對應 GET /farm-trips
public ResponseEntity<List<FarmTrip>> getAll() {
    return ResponseEntity.ok(farmTripService.getActiveTrips());
}
```
- `@GetMapping`（沒寫路徑）+ class 上的 `@RequestMapping("/farm-trips")` = 完整路徑 `/farm-trips`。
- 它**自己不查資料**，把事情丟給 `farmTripService.getActiveTrips()`。
- 拿到結果後用 `ResponseEntity.ok(...)` 包成「200 OK + 內容」。

**② Service**（`FarmTripServiceImpl`）
```java
public List<FarmTrip> getActiveTrips() {
    return farmTripRepository.findByStatus(FarmTripStatus.ACTIVE);
}
```
- 規則很簡單：「上架中」= status 是 `ACTIVE`。把這個條件丟給 repository。

**③ Repository**（`FarmTripRepository`）
```java
List<FarmTrip> findByStatus(FarmTripStatus status);
```
- 這個方法**沒有實作**！Spring Data 看方法名 `findByStatus` 就自動幫你生 SQL：`WHERE status = ?`。

### 白話總結
前端要「全部上架活動」→ controller 轉給 service → service 說「條件是 ACTIVE」→ repository 生 SQL 撈出來 → 一路傳回 controller → 包成 200 的 JSON 陣列回前端。

---

<a id="2"></a>
## 2. `GET /farm-trips/{id}` — 看單一活動（含 404 處理）

**前端送出**：`GET /farm-trips/5001`

### 流程

```
前端 ──GET /farm-trips/5001──▶ FarmTripController.getOne(id=5001)
                                   │
                                   ▼ farmTripService.getTripById(5001)
                              ServiceImpl.getTripById()
                                   │
                                   ▼ farmTripRepository.findById(5001)
                              DB: SELECT * FROM farm_trip WHERE farm_trip_id = 5001
                                   │ 回傳 Optional<FarmTrip>
                                   ▲ .orElse(null) → 有就給物件，沒有就 null
                              Controller 判斷：
                                   ├─ 不是 null → ResponseEntity.ok(trip)        → 200
                                   └─ 是 null    → ResponseEntity.notFound()       → 404
```

### 一層一層看

**① Controller**
```java
@GetMapping("/{id}")                         // GET /farm-trips/{id}
public ResponseEntity<FarmTrip> getOne(@PathVariable Integer id) {
    FarmTrip trip = farmTripService.getTripById(id);
    if (trip != null) {
        return ResponseEntity.ok(trip);          // 找到 → 200
    } else {
        return ResponseEntity.notFound().build(); // 找不到 → 404
    }
}
```
- `@PathVariable Integer id`：把網址裡的 `5001` 取出來當參數。
- 這裡 controller 多做了一個判斷：**回 200 還是 404**。

**② Service**
```java
public FarmTrip getTripById(Integer tripId) {
    return farmTripRepository.findById(tripId).orElse(null);
}
```
- `findById` 回傳的是 `Optional`（一個「可能有、可能沒有」的盒子）。
- `.orElse(null)`：有資料就拿出來，沒有就給 `null` —— 故意回 null，**讓 controller 去決定要不要回 404**。

### 白話總結
查單筆 → service 用 id 去撈 → 撈到給物件、撈不到給 null → controller 看是不是 null，決定回 200 還是 404。

---

<a id="3"></a>
## 3. `GET /farm-trips/{id}/comments` — 看某活動的評論

**前端送出**：`GET /farm-trips/5001/comments`

### 流程
```
前端 ──▶ FarmTripController.getComments(id=5001)
            │
            ▼ farmTripService.getCommentsByTrip(5001)
        ServiceImpl.getCommentsByTrip()
            │
            ▼ farmTripCommentRepository.findByFarmTripIdOrderByCreatedAtDesc(5001)
        DB: SELECT * FROM farm_trip_comment
            WHERE farm_trip_id = 5001 ORDER BY created_at DESC
            │ 回傳 List<FarmTripComment>（最新的在前）
            ▲
        Controller: ResponseEntity.ok(list) → 200 + JSON 陣列
```

**① Controller**
```java
@GetMapping("/{id}/comments")
public ResponseEntity<List<FarmTripComment>> getComments(@PathVariable Integer id) {
    return ResponseEntity.ok(farmTripService.getCommentsByTrip(id));
}
```

**② Service**
```java
public List<FarmTripComment> getCommentsByTrip(Integer tripId) {
    return farmTripCommentRepository.findByFarmTripIdOrderByCreatedAtDesc(tripId);
}
```

**③ Repository**
```java
List<FarmTripComment> findByFarmTripIdOrderByCreatedAtDesc(Integer farmTripId);
```
- 方法名裡的 `OrderByCreatedAtDesc` = Spring 自動加上 `ORDER BY created_at DESC`，最新評論排最前面。

### 白話總結
跟第 1 條幾乎一樣，差別是條件變成「`farm_trip_id = 這個活動`」，而且方法名多了排序。

---

<a id="4"></a>
## 4. `POST /farm-trips/{id}/comments` — 發表評論 ⭐（會回寫統計）

**前端送出**：
```
POST /farm-trips/5001/comments
body: { "userId":1, "star":4, "content":"很好玩" }
```

### 流程（現行架構：controller 只轉發，轉換在 service）

```
前端 ──POST + JSON──▶ FarmTripController.addComment(id=5001, request)
                          │ @Valid 先驗證 request（star 1~5、content 長度…）
                          ▼ farmTripService.addComment(5001, request)   ← 傳 Request DTO
                     ServiceImpl.addComment()  【@Transactional 包住整段】
                          │
                          ├─① 先確認活動存在：farmTripRepository.findById(5001)
                          │
                          ├─② Request → entity + 寫評論：
                          │     comment.setUserId(request.getUserId())
                          │     comment.setStar(request.getStar())
                          │     comment.setFarmTripId(5001)      ← 網址帶的
                          │     comment.setCreatedAt(現在)        ← 後端給
                          │     farmTripCommentRepository.save(comment)
                          │     → DB: INSERT INTO farm_trip_comment ...
                          │
                          ├─③ 回寫活動統計：
                          │     trip.commentNumbers += 1
                          │     trip.starNumbers   += 4
                          │     farmTripRepository.save(trip)
                          │     → DB: UPDATE farm_trip SET comment_numbers=?, star_numbers=? ...
                          │
                          └─④ entity → Response：FarmTripCommentResponse.from(saved)
                          ▲ 回傳 Response DTO
                     Controller: ResponseEntity.ok(response) → 200
```

### 一層一層看

**① Controller**（薄，只轉發）
```java
@PostMapping("/{id}/comments")
public ResponseEntity<FarmTripCommentResponse> addComment(@PathVariable Integer id,
                                                          @Valid @RequestBody FarmTripCommentRequest request) {
    return ResponseEntity.ok(farmTripService.addComment(id, request));
}
```
- `@Valid @RequestBody FarmTripCommentRequest request`：JSON 轉成 Request DTO，且**先驗證**（`star` 必填且 1~5、`content` ≤255）。錯的直接回 **400**，根本進不到 service。
- controller **不做任何轉換**，直接把 request 丟給 service、回 service 給的 Response。

**② Service**（轉換 + 一次動兩張表都在這）
```java
@Transactional
public FarmTripCommentResponse addComment(Integer tripId, FarmTripCommentRequest request) {
    FarmTrip trip = getOrThrow(farmTripRepository.findById(tripId), "體驗活動不存在: " + tripId);

    // 1) Request → entity + 寫評論
    FarmTripComment comment = new FarmTripComment();
    comment.setFarmTripId(tripId);
    comment.setUserId(request.getUserId());
    comment.setStar(request.getStar());
    comment.setContent(request.getContent());
    comment.setCreatedAt(LocalDateTime.now());
    FarmTripComment saved = farmTripCommentRepository.save(comment);

    // 2) 回寫活動統計
    int count = trip.getCommentNumbers() == null ? 0 : trip.getCommentNumbers();
    int stars = trip.getStarNumbers()    == null ? 0 : trip.getStarNumbers();
    int addStar = request.getStar() == null ? 0 : request.getStar();
    trip.setCommentNumbers(count + 1);
    trip.setStarNumbers(stars + addStar);
    farmTripRepository.save(trip);

    // 3) entity → Response
    return FarmTripCommentResponse.from(saved);
}
```
- **`@Transactional`**：寫評論、回寫統計兩步必須一起成功，否則整個回滾 → 不會有「有評論但統計沒加」的髒資料。
- 為什麼維護 `comment_numbers / star_numbers`？前端算平均直接 `star_numbers ÷ comment_numbers`，不用每次把所有評論撈出來重算。

### 白話總結
controller 收 Request（先 `@Valid`）→ 丟給 service → service 把 Request 轉成 entity、存評論、回寫統計（交易保護）、再轉成 Response 回傳 → controller 包 200。轉換與邏輯全在 service。

---

<a id="5"></a>
## 5. `POST /farm-trips/sessions/{sessionId}/orders` — 預約場次 ⭐（名額檢查）

**前端送出**：
```
POST /farm-trips/sessions/6001/orders
body: { "userId":1, "numPeople":2, "userName":"小明", "userPhoneNum":"0900000000" }
```

### 流程（現行架構：controller 只轉發，service 擋名額 + 轉換）

```
前端 ──POST + JSON──▶ FarmTripController.bookSession(sessionId=6001, request)
                          │ @Valid 先驗證 request（numPeople≥1、userName/phone 必填…）
                          ▼ farmTripService.bookSession(6001, request)   ← 傳 Request DTO
                     ServiceImpl.bookSession()  【@Transactional】
                          │
                          ├─① 場次要存在：sessionRepository.findById(6001)
                          ├─② 場次必須 ACTIVE，否則丟例外「此場次無法預約」
                          ├─③ 算已佔名額：findByFarmSessionId(6001)
                          │     → 只加總 status=CONFIRMED 的 numPeople
                          │     若 已佔 + request.numPeople > 上限 → 丟例外「名額不足」
                          ├─④ 通過 → Request→entity 建立訂單：
                          │     order.setUserId/NumPeople/...(request 各欄位)
                          │     order.setStatus(CONFIRMED)、setBookedAt(現在)
                          │     orderRepository.save(order) → INSERT
                          └─⑤ entity → Response：FarmTripOrderResponse.from(saved)
                          ▲ 回傳 Response DTO
                     Controller: ResponseEntity.ok(response) → 200
                     （若中途丟例外 → 目前回 500）
```

### 一層一層看

**① Controller**（薄，只轉發）
```java
@PostMapping("/sessions/{sessionId}/orders")
public ResponseEntity<FarmTripOrderResponse> bookSession(@PathVariable Integer sessionId,
                                                         @Valid @RequestBody FarmTripOrderRequest request) {
    return ResponseEntity.ok(farmTripService.bookSession(sessionId, request));
}
```
- `@Valid`：先驗證 `numPeople≥1`、`userName`/`userPhoneNum` 必填等，錯的回 400。

**② Service**（整個模組最有料的邏輯 + Request→entity + entity→Response）
```java
@Transactional
public FarmTripOrderResponse bookSession(Integer sessionId, FarmTripOrderRequest request) {
    FarmTripSession session = getOrThrow(
        farmTripSessionRepository.findById(sessionId), "場次不存在: " + sessionId);

    // 只有開放中的場次能預約
    if (session.getSessionStatus() != FarmTripSessionStatus.ACTIVE) {
        throw new IllegalStateException("此場次無法預約: " + sessionId);
    }

    // 名額檢查：只算 CONFIRMED 訂單的人數加總
    Integer cap = session.getAttendance();
    int incoming = request.getNumPeople() == null ? 0 : request.getNumPeople();
    if (cap != null) {
        int booked = farmTripOrderRepository.findByFarmSessionId(sessionId).stream()
                .filter(o -> o.getStatus() == FarmTripOrderStatus.CONFIRMED)
                .mapToInt(o -> o.getNumPeople() == null ? 0 : o.getNumPeople())
                .sum();
        if (booked + incoming > cap) {
            throw new IllegalStateException("名額不足，剩餘 " + (cap - booked) + " 位");
        }
    }

    // Request → entity
    FarmTripOrder order = new FarmTripOrder();
    order.setFarmSessionId(sessionId);
    order.setUserId(request.getUserId());
    order.setNumPeople(request.getNumPeople());
    order.setUserName(request.getUserName());
    order.setUserPhoneNum(request.getUserPhoneNum());
    order.setNote(request.getNote());
    order.setStatus(FarmTripOrderStatus.CONFIRMED);
    order.setBookedAt(LocalDateTime.now());

    // entity → Response
    return FarmTripOrderResponse.from(farmTripOrderRepository.save(order));
}
```
- **第 ③ 步只數 `CONFIRMED`**：被取消的訂單（`CANCELLED`）自動不佔名額（見第 7 條）。
- **`@Transactional`**：避免兩人同時搶最後一個名額時都算到「還有位」而超賣。

### 白話總結
controller 收 Request（先 `@Valid`）→ 丟給 service。service 過三關（場次在不在 → 是不是開放中 → 名額夠不夠，只算已確認的）→ 通過才把 Request 轉成 entity 建立訂單 → 轉成 Response 回傳。

---

<a id="6"></a>
## 6. `GET /farm-trips/orders/mine?userId=` — 會員查自己的預約

**前端送出**：`GET /farm-trips/orders/mine?userId=5`

### 流程
```
前端 ──▶ FarmTripController.getMyOrders(userId=5)
            │
            ▼ farmTripService.getOrdersByUser(5)
        ServiceImpl.getOrdersByUser()
            │
            ▼ orderRepository.findByUserIdOrderByBookedAtDesc(5)
        DB: SELECT * FROM farm_trip_order
            WHERE user_id = 5 ORDER BY booked_at DESC
            │ 回傳 List<FarmTripOrder>
            ▲
        Controller: ResponseEntity.ok(list) → 200
```

**① Controller**
```java
@GetMapping("/orders/mine")
public ResponseEntity<List<FarmTripOrder>> getMyOrders(@RequestParam Integer userId) {
    return ResponseEntity.ok(farmTripService.getOrdersByUser(userId));
}
```
- `@RequestParam Integer userId`：從網址 `?userId=5` 取值。（之後改成從登入身分拿。）

**② Service**
```java
public List<FarmTripOrder> getOrdersByUser(Integer userId) {
    return farmTripOrderRepository.findByUserIdOrderByBookedAtDesc(userId);
}
```

### 白話總結
訂單本身就記了 `user_id`，所以一張表、一個條件就查到。**很單純** —— 跟第 16 條「小農查訂單」對比就知道差在哪。

---

<a id="7"></a>
## 7. `POST /farm-trips/orders/{orderId}/cancel` — 取消預約 ⭐

**前端送出**：`POST /farm-trips/orders/8001/cancel`（沒有 body）

### 流程
```
前端 ──▶ FarmTripController.cancelOrder(orderId=8001)
            │
            ▼ farmTripService.cancelOrder(8001)
        ServiceImpl.cancelOrder()
            │
            ├─① 訂單要存在：orderRepository.findById(8001)
            ├─② 必須是 CONFIRMED 才能取消，否則丟例外
            └─③ order.setStatus(CANCELLED)
                  order.setCancelledAt(現在)
                  orderRepository.save(order)
                  → DB: UPDATE farm_trip_order SET status='CANCELLED', cancelled_at=? ...
            ▲
        Controller: ResponseEntity.ok().build() → 200（沒有內容）
```

**② Service**
```java
public void cancelOrder(Integer orderId) {
    FarmTripOrder order = getOrThrow(
        farmTripOrderRepository.findById(orderId), "訂單不存在: " + orderId);
    if (order.getStatus() != FarmTripOrderStatus.CONFIRMED) {
        throw new IllegalStateException("只有已確認的訂單可以取消: " + orderId);
    }
    order.setStatus(FarmTripOrderStatus.CANCELLED);
    order.setCancelledAt(LocalDateTime.now());
    farmTripOrderRepository.save(order);
    // 取消後名額自然釋出：因為名額只計算 CONFIRMED 的訂單
}
```

### 為什麼「自動釋出名額」
回去看第 5 條的名額計算——它**只加總 CONFIRMED 的訂單**。這裡把狀態改成 `CANCELLED` 後，這筆就不再被算進去了，名額等於自動空出來。**不需要另外寫「把名額加回去」的程式**。

### 白話總結
取消 = 把訂單狀態改成 CANCELLED + 記下取消時間。因為名額只算「已確認」的，取消後位子自動釋出。

---

<a id="8"></a>
## 8. `GET /farmer/farm-trips?farmerId=` — 小農查自己的活動

**前端送出**：`GET /farmer/farm-trips?farmerId=2`

### 流程
```
前端 ──▶ FarmerFarmTripController.getMyFarmTrips(farmerId=2)
            │
            ▼ farmTripService.getTripsByFarmer(2)
        ServiceImpl.getTripsByFarmer()
            │
            ▼ farmTripRepository.findByFarmerId(2)
        DB: SELECT * FROM farm_trip WHERE farmer_id = 2
            │ 回傳 List<FarmTrip>
            ▲
        Controller: ResponseEntity.ok(list) → 200
```

**① Controller**
```java
@GetMapping("/farm-trips")     // class 上有 @RequestMapping("/farmer") → 完整路徑 /farmer/farm-trips
public ResponseEntity<List<FarmTrip>> getMyFarmTrips(@RequestParam Integer farmerId) {
    return ResponseEntity.ok(farmTripService.getTripsByFarmer(farmerId));
}
```

### 白話總結
跟第 1 條同套路，只是條件換成 `farmer_id`，且路徑掛在 `/farmer` 底下（跟會員端 `/farm-trips` 不衝突）。

---

<a id="9"></a>
## 9. `POST /farmer/farm-trips` — 小農新增活動 ⭐（鎖死初始值）

**前端送出**：
```
POST /farmer/farm-trips
body: { "farmerId":2, "farmTripType":"FARM_EXPERIENCE", "farmTripTitle":"草莓採收",
        "farmTripIntro":"...", "location":"...", "referPrice":500 }
```

### 流程
```
前端 ──POST + JSON──▶ FarmerFarmTripController.createFarmTrip(farmTrip)
                          │
                          ▼ farmTripService.createTrip(farmTrip)
                     ServiceImpl.createTrip()
                          │ 後端強制覆寫：
                          │   status = PENDING（一律待審核）
                          │   commentNumbers = 0
                          │   starNumbers = 0
                          ▼ farmTripRepository.save(trip)
                     DB: INSERT INTO farm_trip ...（status 一定是 PENDING）
                          │ 回傳存好的 trip（含新產生的 id）
                          ▲
                     Controller: ResponseEntity.ok(trip) → 200
```

**② Service**
```java
public FarmTrip createTrip(FarmTrip trip) {
    trip.setStatus(FarmTripStatus.PENDING);   // 不管前端傳什麼，一律 PENDING
    trip.setCommentNumbers(0);
    trip.setStarNumbers(0);
    return farmTripRepository.save(trip);
}
```

### 為什麼要強制覆寫
**絕不信任前端**。如果不覆寫，前端可以偷傳 `status=ACTIVE` 直接上架、繞過審核，或亂塞 `starNumbers=9999` 灌分。所以這三個欄位由後端說了算。

### 白話總結
新增活動時，後端把「狀態、評論統計」強制設成初始值（PENDING、0、0），再存。前端只能決定標題、介紹這類欄位。

---

<a id="10"></a>
## 10. `PUT /farmer/farm-trips/{tripId}` — 小農修改活動

**前端送出**：`PUT /farmer/farm-trips/5002` + 要改的欄位 JSON

### 流程
```
前端 ──PUT + JSON──▶ FarmerFarmTripController.updateFarmTrip(tripId=5002, farmTrip)
                         │
                         ▼ farmTripService.updateTrip(5002, farmTrip)
                    ServiceImpl.updateTrip()
                         │
                         ├─① 先撈出資料庫現有那筆：findById(5002)（沒有就丟例外）
                         ├─② 只把「可編輯欄位」覆蓋上去
                         │     （type/title/pic/intro/location/referPrice）
                         │     status、farmerId、評論統計 → 不動
                         └─③ farmTripRepository.save(db)
                               → DB: UPDATE farm_trip SET ... WHERE farm_trip_id=5002
                         ▲
                    Controller: ResponseEntity.ok(更新後的trip) → 200
```

**② Service**
```java
public FarmTrip updateTrip(Integer tripId, FarmTrip trip) {
    FarmTrip db = getOrThrow(farmTripRepository.findById(tripId), "體驗活動不存在: " + tripId);
    db.setFarmTripType(trip.getFarmTripType());
    db.setFarmTripTitle(trip.getFarmTripTitle());
    db.setFarmTripPic(trip.getFarmTripPic());
    db.setFarmTripIntro(trip.getFarmTripIntro());
    db.setLocation(trip.getLocation());
    db.setReferPrice(trip.getReferPrice());
    return farmTripRepository.save(db);
}
```

### 重點：為什麼要「先撈再改」
不直接拿前端的物件存，而是**先撈出資料庫那筆 `db`，只改該改的欄位**。這樣 `status`、`farmerId`、評論統計就會保留原值，**前端動不了它們**（跟第 9 條同樣的防護思維）。

### 白話總結
修改 = 先撈現有那筆 → 只覆蓋可編輯欄位 → 存回去。敏感欄位刻意不開放修改。

---

<a id="11"></a>
## 11. `DELETE /farmer/farm-trips/{tripId}` — 小農刪除活動

**前端送出**：`DELETE /farmer/farm-trips/5002`

### 流程
```
前端 ──▶ FarmerFarmTripController.deleteFarmTrip(tripId=5002)
            │
            ▼ farmTripService.deleteTrip(5002)
        ServiceImpl.deleteTrip()
            │
            ▼ farmTripRepository.deleteById(5002)
        DB: DELETE FROM farm_trip WHERE farm_trip_id = 5002
            ▲
        Controller: ResponseEntity.noContent().build() → 204（刪除成功，沒有內容）
```

**① Controller**
```java
@DeleteMapping("/farm-trips/{tripId}")
public ResponseEntity<Void> deleteFarmTrip(@PathVariable Integer tripId) {
    farmTripService.deleteTrip(tripId);
    return ResponseEntity.noContent().build();   // 204
}
```
- `204 No Content` = 「做完了，沒東西要回給你」，是刪除成功的慣例狀態碼。

### 白話總結
最單純的一條：直接 `deleteById`。（注意：若活動底下有場次/訂單，會有外鍵問題，見總覽文件「已知限制」。）

---

<a id="12"></a>
## 12. `POST /farmer/farm-trips/{tripId}/sessions` — 新增單一場次

**前端送出**：
```
POST /farmer/farm-trips/5001/sessions
body: { "farmTripStart":"2026-09-01T09:00:00", "farmTripEnd":"...", "attendance":30 }
```

### 流程
```
前端 ──POST + JSON──▶ FarmerFarmTripController.createSession(tripId=5001, session)
                          │
                          ▼ farmTripService.createSession(5001, session)
                     ServiceImpl.createSession()
                          │ session.setFarmTripId(5001)        ← 用網址的 tripId 綁定
                          │ 若 sessionStatus 是 null → 補 ACTIVE
                          ▼ farmTripSessionRepository.save(session)
                     DB: INSERT INTO farm_trip_session ...
                          │ 回傳存好的 session（含新 id）
                          ▲
                     Controller: ResponseEntity.ok(session) → 200
```

**② Service**
```java
public FarmTripSession createSession(Integer tripId, FarmTripSession session) {
    session.setFarmTripId(tripId);               // 場次屬於哪個活動，用網址帶的 tripId
    if (session.getSessionStatus() == null) {
        session.setSessionStatus(FarmTripSessionStatus.ACTIVE);  // 預設開放
    }
    return farmTripSessionRepository.save(session);
}
```
- 注意 `farmTripId` 是**用網址的 `{tripId}` 設定**，不是讓前端在 body 裡自己填，避免把場次掛錯活動。

### 白話總結
新增場次，後端用網址的活動 id 綁定，沒給狀態就預設 ACTIVE，再存。

---

<a id="13"></a>
## 13. `POST /farmer/farm-trips/{tripId}/sessions/batch` — 批次新增場次

**前端送出**：body 是**陣列** `[ {場次1}, {場次2}, ... ]`

### 流程
```
前端 ──POST + JSON陣列──▶ FarmerFarmTripController.createSessionBatch(tripId, sessions)
                              │
                              ▼ farmTripService.createSessions(tripId, sessions)
                         ServiceImpl.createSessions()  【@Transactional】
                              │ for 每一筆場次：
                              │    setFarmTripId(tripId)
                              │    沒狀態就補 ACTIVE
                              ▼ farmTripSessionRepository.saveAll(sessions)  ← 一次存全部
                         DB: 多筆 INSERT
                              │ 回傳存好的 List
                              ▲
                         Controller: ResponseEntity.ok(list) → 200
```

**② Service**
```java
@Transactional
public List<FarmTripSession> createSessions(Integer tripId, List<FarmTripSession> sessions) {
    for (FarmTripSession s : sessions) {
        s.setFarmTripId(tripId);
        if (s.getSessionStatus() == null) s.setSessionStatus(FarmTripSessionStatus.ACTIVE);
    }
    return farmTripSessionRepository.saveAll(sessions);
}
```
- 跟第 12 條一樣，只是 **for 迴圈處理多筆** + 用 `saveAll` 一次存。
- `@Transactional`：要嘛全部成功，要嘛全部失敗（不會只存一半）。

### 白話總結
跟單筆新增同理，差別是 body 收一個陣列、用 `saveAll` 一次存多筆，並用交易確保「全成功或全失敗」。

---

<a id="14"></a>
## 14. `PUT /farmer/sessions/{sessionId}` — 修改場次

**前端送出**：`PUT /farmer/sessions/6001` + 要改的欄位

### 流程
```
前端 ──PUT + JSON──▶ FarmerFarmTripController.updateSession(sessionId=6001, session)
                         │
                         ▼ farmTripService.updateSession(6001, session)
                    ServiceImpl.updateSession()
                         │ ① findById(6001)（沒有就丟例外）
                         │ ② 覆蓋時間4欄、attendance、sessionStatus
                         ▼ ③ save(db)  → DB: UPDATE farm_trip_session ... WHERE farm_session_id=6001
                         ▲
                    Controller: ResponseEntity.ok(更新後session) → 200
```

注意路徑是 `/farmer/sessions/{sessionId}`，**不需要帶 tripId**，因為場次本身已經有獨立 id 了。

### 白話總結
跟第 10 條「修改活動」同套路：先撈現有 → 覆蓋欄位 → 存回。

---

<a id="15"></a>
## 15. `DELETE /farmer/sessions/{sessionId}` — 刪除場次

### 流程
```
前端 ──▶ FarmerFarmTripController.deleteSession(sessionId=6001)
            │
            ▼ farmTripService.deleteSession(6001)
            ▼ farmTripSessionRepository.deleteById(6001)
        DB: DELETE FROM farm_trip_session WHERE farm_session_id = 6001
            ▲
        Controller: ResponseEntity.noContent().build() → 204
```

### ⚠️ 已知問題
若該場次底下**已經有訂單**（`farm_trip_order.farm_session_id` 指著它），這個 `DELETE` 會違反外鍵約束、**回 500**。測試時就遇到了。之後應改成「有訂單就不准刪」或「軟刪除」（把 sessionStatus 設成 CANCELLED 而不是真的刪）。

### 白話總結
直接刪。但場次有訂單時會被外鍵擋下來報錯，是待修補的點。

---

<a id="16"></a>
## 16. `GET /farmer/farm-trip-orders?farmerId=` — 小農查訂單 ⭐（跨三張表）

**前端送出**：`GET /farmer/farm-trip-orders?farmerId=2`

### 流程（重點在 repository 那條手寫 JPQL）
```
前端 ──▶ FarmerFarmTripController.getMyOrders(farmerId=2)
            │
            ▼ farmTripService.getOrdersByFarmer(2)
        ServiceImpl.getOrdersByFarmer()
            │
            ▼ orderRepository.findOrdersByFarmerId(2)   ← 自訂 @Query
        DB 執行這條跨表查詢：
            SELECT o FROM FarmTripOrder o, FarmTripSession s, FarmTrip t
            WHERE o.farmSessionId = s.farmSessionId   -- 訂單 接 場次
              AND s.farmTripId    = t.farmTripId      -- 場次 接 活動
              AND t.farmerId      = 2                 -- 篩出這個小農
            ORDER BY o.bookedAt DESC
            │ 回傳 List<FarmTripOrder>
            ▲
        Controller: ResponseEntity.ok(list) → 200
```

**③ Repository**（這條是手寫的，不是方法名自動生成）
```java
@Query("SELECT o FROM FarmTripOrder o, FarmTripSession s, FarmTrip t " +
       "WHERE o.farmSessionId = s.farmSessionId " +
       "AND s.farmTripId = t.farmTripId " +
       "AND t.farmerId = :farmerId " +
       "ORDER BY o.bookedAt DESC")
List<FarmTripOrder> findOrdersByFarmerId(@Param("farmerId") Integer farmerId);
```

### 為什麼這條特別麻煩
**訂單沒有直接記 `farmer_id`**（只記到 `farm_session_id`）。要從小農找到他所有訂單，必須走「訂單→場次→活動→小農」三層。因為 entity 用原始外鍵（不是 `@ManyToOne`），不能用 `o.session.trip.farmer` 點下去，只能手寫 JPQL 把三張表用 WHERE 對齊外鍵（這寫法叫 theta join）。

對比第 6 條（會員查訂單）只要一張表——差別就在「訂單有沒有直接記住那個人的 id」。

### 白話總結
小農查訂單要跨三張表，所以 repository 裡用一條手寫 `@Query` 把訂單→場次→活動串起來、再用 `farmer_id` 篩。其餘流程跟一般查詢一樣。

---

<a id="17"></a>
## 17. `POST /farmer/farm-trip-orders/{orderId}/complete` — 完成訂單

**前端送出**：`POST /farmer/farm-trip-orders/8001/complete`

### 流程
```
前端 ──▶ FarmerFarmTripController.completeOrder(orderId=8001)
            │
            ▼ farmTripService.completeOrder(8001)
        ServiceImpl.completeOrder()
            │ ① findById(8001)（沒有就丟例外）
            │ ② order.setStatus(COMPLETED)
            │    order.setCompletedAt(現在)
            ▼ ③ save(order) → DB: UPDATE ... SET status='COMPLETED', completed_at=?
            ▲
        Controller: ResponseEntity.ok().build() → 200
```

**② Service**
```java
public void completeOrder(Integer orderId) {
    FarmTripOrder order = getOrThrow(
        farmTripOrderRepository.findById(orderId), "訂單不存在: " + orderId);
    order.setStatus(FarmTripOrderStatus.COMPLETED);
    order.setCompletedAt(LocalDateTime.now());
    farmTripOrderRepository.save(order);
}
```

### 白話總結
跟「取消」(第 7 條) 幾乎一樣，只是把狀態改成 `COMPLETED`、記下完成時間。

---

<a id="18"></a>
## 18. `GET /admin/farm-trips/audits/pending` — 管理員待審清單

**前端送出**：`GET /admin/farm-trips/audits/pending`

### 流程
```
前端 ──▶ AdminFarmTripController.getPendingAuditList()
            │
            ▼ farmTripService.getPendingTrips()
        ServiceImpl.getPendingTrips()
            │
            ▼ farmTripRepository.findByStatus(PENDING)
        DB: SELECT * FROM farm_trip WHERE status = 'PENDING'
            │ 回傳 List<FarmTrip>
            ▲
        Controller: ResponseEntity.ok(list) → 200
```

### 白話總結
跟第 1 條一模一樣的套路，只是條件是 `PENDING`（待審）而不是 `ACTIVE`。同一張表、同一個 repository 方法，換個參數而已。

---

<a id="19"></a>
## 19. `POST /admin/farm-trips/{tripId}/audit` — 審核 ⭐（連動改狀態）

**前端送出**：
```
POST /admin/farm-trips/5001/audit
body: { "adminId":1, "status":"APPROVED", "reason":"資料完整" }
```

### 流程（service 動兩張表）
```
前端 ──POST + JSON──▶ AdminFarmTripController.auditTrip(tripId=5001, auditRequest)
                          │
                          ▼ farmTripService.auditTrip(5001, audit)
                     ServiceImpl.auditTrip()  【@Transactional】
                          │
                          ├─① 活動要存在：findById(5001)
                          │
                          ├─② 寫審核紀錄：
                          │     audit.setFarmTripId(5001)
                          │     audit.setCreatedAt/UpdatedAt(現在)
                          │     auditRepository.save(audit)
                          │     → DB: INSERT INTO farm_trip_audits ...
                          │
                          └─③ 連動改活動狀態：
                                APPROVED → trip.setStatus(ACTIVE)
                                REJECTED → trip.setStatus(REJECTED)
                                farmTripRepository.save(trip)
                                → DB: UPDATE farm_trip SET status=? WHERE farm_trip_id=5001
                          ▲
                     Controller: ResponseEntity.ok().build() → 200
```

**② Service**
```java
@Transactional
public void auditTrip(Integer tripId, FarmTripAudit audit) {
    FarmTrip trip = getOrThrow(farmTripRepository.findById(tripId), "體驗活動不存在: " + tripId);

    // 1) 寫審核紀錄
    LocalDateTime now = LocalDateTime.now();
    audit.setFarmTripId(tripId);
    audit.setCreatedAt(now);
    audit.setUpdatedAt(now);
    farmTripAuditRepository.save(audit);

    // 2) 連動更新活動狀態
    if (audit.getStatus() == FarmTripAuditStatus.APPROVED) {
        trip.setStatus(FarmTripStatus.ACTIVE);
    } else if (audit.getStatus() == FarmTripAuditStatus.REJECTED) {
        trip.setStatus(FarmTripStatus.REJECTED);
    }
    farmTripRepository.save(trip);
}
```
- **`@Transactional`**：寫紀錄、改狀態兩步綁一起，不會發生「紀錄寫了、活動狀態沒變」。

### 白話總結
審核做兩件事：存一筆審核紀錄 + 依結果連動把活動改成 ACTIVE 或 REJECTED。兩步用交易綁在一起。

---

<a id="20"></a>
## 20. `GET /admin/farm-trips/{tripId}/audits` — 審核歷史

**前端送出**：`GET /admin/farm-trips/5001/audits`

### 流程
```
前端 ──▶ AdminFarmTripController.getAuditHistory(tripId=5001)
            │
            ▼ farmTripService.getAuditHistory(5001)
            ▼ auditRepository.findByFarmTripIdOrderByCreatedAtDesc(5001)
        DB: SELECT * FROM farm_trip_audits
            WHERE farm_trip_id = 5001 ORDER BY created_at DESC
            │ 回傳 List<FarmTripAudit>（最新在前）
            ▲
        Controller: ResponseEntity.ok(list) → 200
```

### 白話總結
查某活動的所有審核紀錄，最新排前面。跟第 3 條「查評論」是同一個套路。

---

## 附：你會一直看到的幾個固定動作

| 你看到 | 它在做什麼 |
|---|---|
| `@RequestBody Xxx x` | 把前端送來的 JSON 轉成 Java 物件 |
| `@PathVariable Integer id` | 把網址裡的 `{id}` 取出來 |
| `@RequestParam Integer farmerId` | 把網址 `?farmerId=2` 取出來 |
| `ResponseEntity.ok(資料)` | 包成「200 OK + 內容」 |
| `ResponseEntity.notFound().build()` | 回 404 |
| `ResponseEntity.noContent().build()` | 回 204（成功但沒內容，常用於刪除） |
| `findById(id).orElse(null)` | 撈一筆，沒有就給 null |
| `getOrThrow(...)` | 撈一筆，沒有就丟例外（自訂小工具） |
| `findByXxx(...)` | 方法名自動生 SQL 的查詢 |
| `@Query("...")` | 手寫的 JPQL（複雜查詢才用） |
| `@Transactional` | 多步寫入綁成一個交易，全成功或全失敗 |
| `save(x)` | 沒 id 就 INSERT，有 id 就 UPDATE |
```
