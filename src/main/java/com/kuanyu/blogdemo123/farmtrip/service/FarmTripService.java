package com.kuanyu.blogdemo123.farmtrip.service;

import com.kuanyu.blogdemo123.farmtrip.dto.*;

import java.util.List;

/**
 * 體驗活動模組 service 介面。
 *
 * 對齊參考專案(farm-platform)的做法：
 *   - 吃 XxxRequest、回 XxxResponse
 *   - DTO ↔ entity 的轉換都在 service 內部完成
 *   - controller 只負責轉發，不碰 entity
 */
public interface FarmTripService {

    // ===== 活動本體 =====

    /** 公開瀏覽：上架中 (ACTIVE) 的活動 */
    List<FarmTripResponse> getActiveTrips();

    /** 單個活動 (查無回 null) */
    FarmTripResponse getTripById(Integer tripId);

    /** 小農查自己的活動 */
    List<FarmTripResponse> getTripsByFarmer(Integer farmerId);

    /** 新增活動 (預設 PENDING，統計歸零) */
    FarmTripResponse createTrip(FarmTripRequest request);

    /** 修改活動 */
    FarmTripResponse updateTrip(Integer tripId, FarmTripRequest request);

    /** 刪除活動 */
    void deleteTrip(Integer tripId);

    // ===== 審核 =====

    /** 管理員：待審核 (PENDING) 清單 */
    List<FarmTripResponse> getPendingTrips();

    /** 審核單個活動 (寫審核紀錄 + 連動更新活動狀態) */
    void auditTrip(Integer tripId, FarmTripAuditRequest request);

    /** 某活動的審核歷史 */
    List<FarmTripAuditResponse> getAuditHistory(Integer tripId);

    // ===== 場次 =====

    /** 某活動的所有場次 */
    List<FarmTripSessionResponse> getSessionsByTrip(Integer tripId);

    /** 場次單筆新增 */
    FarmTripSessionResponse createSession(Integer tripId, FarmTripSessionRequest request);

    /** 場次批次新增 */
    List<FarmTripSessionResponse> createSessions(Integer tripId, List<FarmTripSessionRequest> requests);

    /** 場次修改 */
    FarmTripSessionResponse updateSession(Integer sessionId, FarmTripSessionRequest request);

    /** 場次刪除 */
    void deleteSession(Integer sessionId);

    // ===== 訂單 =====

    /** 會員預約場次 (檢查名額後建立訂單) */
    FarmTripOrderResponse bookSession(Integer sessionId, FarmTripOrderRequest request);

    /** 會員查自己的所有預約 */
    List<FarmTripOrderResponse> getOrdersByUser(Integer userId);

    /** 小農查自己活動的所有訂單 (跨表查詢) */
    List<FarmTripOrderResponse> getOrdersByFarmer(Integer farmerId);

    /** 會員取消預約 */
    void cancelOrder(Integer orderId);

    /** 小農完成訂單 */
    void completeOrder(Integer orderId);

    // ===== 評論 =====

    /** 某活動的評論列表 */
    List<FarmTripCommentResponse> getCommentsByTrip(Integer tripId);

    /** 發表評論 (新增 + 回寫活動的評論數與星數總和) */
    FarmTripCommentResponse addComment(Integer tripId, FarmTripCommentRequest request);
}
