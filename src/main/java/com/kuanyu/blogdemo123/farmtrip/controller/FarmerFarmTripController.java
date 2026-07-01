package com.kuanyu.blogdemo123.farmtrip.controller;

import com.kuanyu.blogdemo123.farmtrip.dto.FarmTripOrderResponse;
import com.kuanyu.blogdemo123.farmtrip.dto.FarmTripRequest;
import com.kuanyu.blogdemo123.farmtrip.dto.FarmTripResponse;
import com.kuanyu.blogdemo123.farmtrip.dto.FarmTripSessionRequest;
import com.kuanyu.blogdemo123.farmtrip.dto.FarmTripSessionResponse;
import com.kuanyu.blogdemo123.farmtrip.entity.FarmTrip;
import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripSession;
import com.kuanyu.blogdemo123.farmtrip.service.FarmTripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 體驗活動 - 小農 (活動本體 / 場次 / 訂單管理)
 * base path: /farmer
 *
 * 註：farmerId 目前暫用 @RequestParam 帶入，
 *     之後有登入機制後改從登入資訊取得。
 *
 */
@RestController
@RequestMapping("/farmer")
public class FarmerFarmTripController {

    @Autowired
    private FarmTripService farmTripService;

    // ===== 活動本體 =====

    /** 查看小農自己的體驗活動 */
    @GetMapping("/farm-trips")
    public ResponseEntity<List<FarmTripResponse>> getMyFarmTrips(@RequestParam Integer farmerId) {
        List<FarmTripResponse> body = farmTripService.getTripsByFarmer(farmerId).stream()
                .map(FarmTripResponse::from)
                .toList();
        return ResponseEntity.ok(body);
    }

    /** 新增體驗活動（改用 DTO：收 FarmTripRequest，回 FarmTripResponse） */
    @PostMapping("/farm-trips")
    public ResponseEntity<FarmTripResponse> createFarmTrip(@RequestBody FarmTripRequest request) {
        // 1) DTO → entity：只搬前端「該給」的欄位
        FarmTrip trip = new FarmTrip();
        trip.setFarmerId(request.getFarmerId());
        trip.setFarmTripType(request.getFarmTripType());
        trip.setFarmTripTitle(request.getFarmTripTitle());
        trip.setFarmTripIntro(request.getFarmTripIntro());
        trip.setLocation(request.getLocation());
        trip.setReferPrice(request.getReferPrice());
        // 注意：這裡完全碰不到 status / 統計 / id，因為 request DTO 根本沒這些欄位

        // 2) 呼叫 service（service 內部仍會強制 PENDING、統計歸零）
        FarmTrip saved = farmTripService.createTrip(trip);

        // 3) entity → DTO：只回前端該看的，並附上算好的平均星數
        return ResponseEntity.ok(FarmTripResponse.from(saved));
    }

    /** 修改體驗活動（收 FarmTripRequest，回 FarmTripResponse） */
    @PutMapping("/farm-trips/{tripId}")
    public ResponseEntity<FarmTripResponse> updateFarmTrip(@PathVariable Integer tripId,
                                                           @RequestBody FarmTripRequest request) {
        // DTO → entity：只搬可編輯欄位（service 內部同樣不會覆蓋 status / 統計）
        FarmTrip farmTrip = new FarmTrip();
        farmTrip.setFarmTripType(request.getFarmTripType());
        farmTrip.setFarmTripTitle(request.getFarmTripTitle());
        farmTrip.setFarmTripIntro(request.getFarmTripIntro());
        farmTrip.setLocation(request.getLocation());
        farmTrip.setReferPrice(request.getReferPrice());

        FarmTrip saved = farmTripService.updateTrip(tripId, farmTrip);
        return ResponseEntity.ok(FarmTripResponse.from(saved));
    }

    /** 刪除體驗活動 */
    @DeleteMapping("/farm-trips/{tripId}")
    public ResponseEntity<Void> deleteFarmTrip(@PathVariable Integer tripId) {
        farmTripService.deleteTrip(tripId);
        return ResponseEntity.noContent().build();
    }

    // ===== 場次 CRUD =====

    /** 場次 - 單筆新增 */
    @PostMapping("/farm-trips/{tripId}/sessions")
    public ResponseEntity<FarmTripSessionResponse> createSession(@PathVariable Integer tripId,
                                                                 @RequestBody FarmTripSessionRequest request) {
        FarmTripSession saved = farmTripService.createSession(tripId, toEntity(request));
        return ResponseEntity.ok(FarmTripSessionResponse.from(saved));
    }

    /** 場次 - 批次新增 */
    @PostMapping("/farm-trips/{tripId}/sessions/batch")
    public ResponseEntity<List<FarmTripSessionResponse>> createSessionBatch(@PathVariable Integer tripId,
                                                                            @RequestBody List<FarmTripSessionRequest> requests) {
        // 每筆 DTO → entity
        List<FarmTripSession> sessions = requests.stream().map(this::toEntity).toList();
        List<FarmTripSessionResponse> body = farmTripService.createSessions(tripId, sessions).stream()
                .map(FarmTripSessionResponse::from)
                .toList();
        return ResponseEntity.ok(body);
    }

    /** 場次 - 修改 */
    @PutMapping("/sessions/{sessionId}")
    public ResponseEntity<FarmTripSessionResponse> updateSession(@PathVariable Integer sessionId,
                                                                 @RequestBody FarmTripSessionRequest request) {
        FarmTripSession saved = farmTripService.updateSession(sessionId, toEntity(request));
        return ResponseEntity.ok(FarmTripSessionResponse.from(saved));
    }

    /** SessionRequest DTO → entity（新增/批次/修改共用；farmTripId 由 service 用網址帶的 tripId 設定） */
    private FarmTripSession toEntity(FarmTripSessionRequest request) {
        FarmTripSession session = new FarmTripSession();
        session.setFarmTripStart(request.getFarmTripStart());
        session.setFarmTripEnd(request.getFarmTripEnd());
        session.setTripBookStart(request.getTripBookStart());
        session.setTripBookEnd(request.getTripBookEnd());
        session.setAttendance(request.getAttendance());
        session.setSessionStatus(request.getSessionStatus());
        return session;
    }

    /** 場次 - 刪除 */
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable Integer sessionId) {
        farmTripService.deleteSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    // ===== 訂單管理 =====

    /** 小農查訂單 (跨表查回自己活動的所有訂單) */
    @GetMapping("/farm-trip-orders")
    public ResponseEntity<List<FarmTripOrderResponse>> getMyOrders(@RequestParam Integer farmerId) {
        List<FarmTripOrderResponse> body = farmTripService.getOrdersByFarmer(farmerId).stream()
                .map(FarmTripOrderResponse::from)
                .toList();
        return ResponseEntity.ok(body);
    }

    /** 完成訂單 */
    @PostMapping("/farm-trip-orders/{orderId}/complete")
    public ResponseEntity<Void> completeOrder(@PathVariable Integer orderId) {
        farmTripService.completeOrder(orderId);
        return ResponseEntity.ok().build();
    }
}
