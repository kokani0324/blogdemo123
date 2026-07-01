package com.kuanyu.blogdemo123.farmtrip.controller;

import com.kuanyu.blogdemo123.farmtrip.dto.*;
import com.kuanyu.blogdemo123.farmtrip.service.FarmTripService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 體驗活動 - 會員 / 公開瀏覽
 * base path: /farm-trips
 *
 * controller 只負責轉發：收 Request DTO / 參數 → 丟給 service → 回 service 給的 Response DTO。
 * 轉換與商業邏輯都在 service。
 *
 * 註：userId 目前暫用 @RequestParam / DTO 帶入，之後改從登入身分取得。
 */
@RestController
@RequestMapping("/farm-trips")
public class FarmTripController {

    @Autowired
    private FarmTripService farmTripService;

    // ===== 公開瀏覽 =====

    /** 查全部 (上架中的體驗活動) */
    @GetMapping
    public ResponseEntity<List<FarmTripResponse>> getAll() {
        return ResponseEntity.ok(farmTripService.getActiveTrips());
    }

    /** 單個體驗活動頁面 (查無回 404) */
    @GetMapping("/{id}")
    public ResponseEntity<FarmTripResponse> getOne(@PathVariable Integer id) {
        FarmTripResponse trip = farmTripService.getTripById(id);
        return (trip != null) ? ResponseEntity.ok(trip) : ResponseEntity.notFound().build();
    }

    /** 該活動的評論列表 */
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<FarmTripCommentResponse>> getComments(@PathVariable Integer id) {
        return ResponseEntity.ok(farmTripService.getCommentsByTrip(id));
    }

    // ===== 會員預約 =====

    /** 預約場次 (對某場次建立預約訂單) */
    @PostMapping("/sessions/{sessionId}/orders")
    public ResponseEntity<FarmTripOrderResponse> bookSession(@PathVariable Integer sessionId,
                                                             @Valid @RequestBody FarmTripOrderRequest request) {
        return ResponseEntity.ok(farmTripService.bookSession(sessionId, request));
    }

    /** 查看所有預約 (目前登入會員的預約清單) */
    @GetMapping("/orders/mine")
    public ResponseEntity<List<FarmTripOrderResponse>> getMyOrders(@RequestParam Integer userId) {
        return ResponseEntity.ok(farmTripService.getOrdersByUser(userId));
    }

    /** 取消預約 */
    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Integer orderId) {
        farmTripService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }

    // ===== 會員評論 =====

    /** 對活動發表評論 */
    @PostMapping("/{id}/comments")
    public ResponseEntity<FarmTripCommentResponse> addComment(@PathVariable Integer id,
                                                              @Valid @RequestBody FarmTripCommentRequest request) {
        return ResponseEntity.ok(farmTripService.addComment(id, request));
    }
}
