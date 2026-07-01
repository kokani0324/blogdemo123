package com.kuanyu.blogdemo123.farmtrip.controller;

import com.kuanyu.blogdemo123.farmtrip.dto.*;
import com.kuanyu.blogdemo123.farmtrip.service.FarmTripService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 體驗活動 - 小農 (活動本體 / 場次 / 訂單管理)
 * base path: /farmer
 *
 * controller 只負責轉發，轉換與商業邏輯都在 service。
 * 註：farmerId 目前暫用 @RequestParam 帶入，之後改從登入身分取得。
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
        return ResponseEntity.ok(farmTripService.getTripsByFarmer(farmerId));
    }

    /** 新增體驗活動 */
    @PostMapping("/farm-trips")
    public ResponseEntity<FarmTripResponse> createFarmTrip(@Valid @RequestBody FarmTripRequest request) {
        return ResponseEntity.ok(farmTripService.createTrip(request));
    }

    /** 修改體驗活動 */
    @PutMapping("/farm-trips/{tripId}")
    public ResponseEntity<FarmTripResponse> updateFarmTrip(@PathVariable Integer tripId,
                                                           @Valid @RequestBody FarmTripRequest request) {
        return ResponseEntity.ok(farmTripService.updateTrip(tripId, request));
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
                                                                 @Valid @RequestBody FarmTripSessionRequest request) {
        return ResponseEntity.ok(farmTripService.createSession(tripId, request));
    }

    /** 場次 - 批次新增 */
    @PostMapping("/farm-trips/{tripId}/sessions/batch")
    public ResponseEntity<List<FarmTripSessionResponse>> createSessionBatch(@PathVariable Integer tripId,
                                                                            @Valid @RequestBody List<FarmTripSessionRequest> requests) {
        return ResponseEntity.ok(farmTripService.createSessions(tripId, requests));
    }

    /** 場次 - 修改 */
    @PutMapping("/sessions/{sessionId}")
    public ResponseEntity<FarmTripSessionResponse> updateSession(@PathVariable Integer sessionId,
                                                                 @Valid @RequestBody FarmTripSessionRequest request) {
        return ResponseEntity.ok(farmTripService.updateSession(sessionId, request));
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
        return ResponseEntity.ok(farmTripService.getOrdersByFarmer(farmerId));
    }

    /** 完成訂單 */
    @PostMapping("/farm-trip-orders/{orderId}/complete")
    public ResponseEntity<Void> completeOrder(@PathVariable Integer orderId) {
        farmTripService.completeOrder(orderId);
        return ResponseEntity.ok().build();
    }
}
