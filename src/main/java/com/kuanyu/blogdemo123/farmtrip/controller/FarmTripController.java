package com.kuanyu.blogdemo123.farmtrip.controller;

import com.kuanyu.blogdemo123.farmtrip.dto.*;
import com.kuanyu.blogdemo123.farmtrip.entity.FarmTrip;
import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripComment;
import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripOrder;
import com.kuanyu.blogdemo123.farmtrip.service.FarmTripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 體驗活動 - 會員 / 公開瀏覽
 * base path: /farm-trips
 *
 * 進出一律用 DTO：
 *   - 送進來的 body 收 XxxRequest（前端只能給該給的欄位）
 *   - 回出去一律 XxxResponse（不外露 entity）
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
        List<FarmTripResponse> body = farmTripService.getActiveTrips().stream()
                .map(FarmTripResponse::from)
                .toList();
        return ResponseEntity.ok(body);
    }

    /** 單個體驗活動頁面 (查無回 404) */
    @GetMapping("/{id}")
    public ResponseEntity<FarmTripResponse> getOne(@PathVariable Integer id) {
        FarmTrip trip = farmTripService.getTripById(id);
        if (trip != null) {
            return ResponseEntity.ok(FarmTripResponse.from(trip));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /** 該活動的評論列表 */
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<FarmTripCommentResponse>> getComments(@PathVariable Integer id) {
        List<FarmTripCommentResponse> body = farmTripService.getCommentsByTrip(id).stream()
                .map(FarmTripCommentResponse::from)
                .toList();
        return ResponseEntity.ok(body);
    }

    // ===== 會員預約 =====

    /** 預約場次 (對某場次建立預約訂單) */
    @PostMapping("/sessions/{sessionId}/orders")
    public ResponseEntity<FarmTripOrderResponse> bookSession(@PathVariable Integer sessionId,
                                                             @RequestBody FarmTripOrderRequest request) {
        // DTO → entity（只搬前端該給的；farmSessionId/status/bookedAt 由 service 決定）
        FarmTripOrder order = new FarmTripOrder();
        order.setUserId(request.getUserId());
        order.setNumPeople(request.getNumPeople());
        order.setUserName(request.getUserName());
        order.setUserPhoneNum(request.getUserPhoneNum());
        order.setNote(request.getNote());

        FarmTripOrder saved = farmTripService.bookSession(sessionId, order);
        return ResponseEntity.ok(FarmTripOrderResponse.from(saved));
    }

    /** 查看所有預約 (目前登入會員的預約清單) */
    @GetMapping("/orders/mine")
    public ResponseEntity<List<FarmTripOrderResponse>> getMyOrders(@RequestParam Integer userId) {
        List<FarmTripOrderResponse> body = farmTripService.getOrdersByUser(userId).stream()
                .map(FarmTripOrderResponse::from)
                .toList();
        return ResponseEntity.ok(body);
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
                                                              @RequestBody FarmTripCommentRequest request) {
        // DTO → entity（farmTripId 由網址帶、createdAt 由 service 給）
        FarmTripComment comment = new FarmTripComment();
        comment.setUserId(request.getUserId());
        comment.setStar(request.getStar());
        comment.setContent(request.getContent());

        FarmTripComment saved = farmTripService.addComment(id, comment);
        return ResponseEntity.ok(FarmTripCommentResponse.from(saved));
    }
}
