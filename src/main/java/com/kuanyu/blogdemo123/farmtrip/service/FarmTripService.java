package com.kuanyu.blogdemo123.farmtrip.service;

import com.kuanyu.blogdemo123.farmtrip.entity.*;

import java.util.List;

/**
 * 體驗活動模組 service 介面 (活動 / 場次 / 審核 / 訂單 / 評論)
 */
public interface FarmTripService {

    // ===== 活動本體 =====

    /** 公開瀏覽：上架中 (ACTIVE) 的活動 */
    List<FarmTrip> getActiveTrips();

    /** 單個活動 (查無回 null) */
    FarmTrip getTripById(Integer tripId);

    /** 小農查自己的活動 */
    List<FarmTrip> getTripsByFarmer(Integer farmerId);

    /** 新增活動 (預設 PENDING，統計歸零) */
    FarmTrip createTrip(FarmTrip trip);

    /** 修改活動 */
    FarmTrip updateTrip(Integer tripId, FarmTrip trip);

    /** 刪除活動 */
    void deleteTrip(Integer tripId);

    // ===== 審核 =====

    /** 管理員：待審核 (PENDING) 清單 */
    List<FarmTrip> getPendingTrips();

    /** 審核單個活動 (寫審核紀錄 + 連動更新活動狀態) */
    void auditTrip(Integer tripId, FarmTripAudit audit);

    /** 某活動的審核歷史 */
    List<FarmTripAudit> getAuditHistory(Integer tripId);

    // ===== 場次 =====

    /** 某活動的所有場次 */
    List<FarmTripSession> getSessionsByTrip(Integer tripId);

    /** 場次單筆新增 */
    FarmTripSession createSession(Integer tripId, FarmTripSession session);

    /** 場次批次新增 */
    List<FarmTripSession> createSessions(Integer tripId, List<FarmTripSession> sessions);

    /** 場次修改 */
    FarmTripSession updateSession(Integer sessionId, FarmTripSession session);

    /** 場次刪除 */
    void deleteSession(Integer sessionId);

    // ===== 訂單 =====

    /** 會員預約場次 (檢查名額後建立訂單) */
    FarmTripOrder bookSession(Integer sessionId, FarmTripOrder order);

    /** 會員查自己的所有預約 */
    List<FarmTripOrder> getOrdersByUser(Integer userId);

    /** 小農查自己活動的所有訂單 (跨表查詢) */
    List<FarmTripOrder> getOrdersByFarmer(Integer farmerId);

    /** 會員取消預約 */
    void cancelOrder(Integer orderId);

    /** 小農完成訂單 */
    void completeOrder(Integer orderId);

    // ===== 評論 =====

    /** 某活動的評論列表 */
    List<FarmTripComment> getCommentsByTrip(Integer tripId);

    /** 發表評論 (新增 + 回寫活動的評論數與星數總和) */
    FarmTripComment addComment(Integer tripId, FarmTripComment comment);
}
