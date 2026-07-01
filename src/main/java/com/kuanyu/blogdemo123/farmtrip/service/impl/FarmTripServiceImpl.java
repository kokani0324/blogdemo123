package com.kuanyu.blogdemo123.farmtrip.service.impl;

import com.kuanyu.blogdemo123.farmtrip.entity.*;
import com.kuanyu.blogdemo123.farmtrip.repository.*;
import com.kuanyu.blogdemo123.farmtrip.service.FarmTripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FarmTripServiceImpl implements FarmTripService {

    @Autowired
    private FarmTripRepository farmTripRepository;
    @Autowired
    private FarmTripSessionRepository farmTripSessionRepository;
    @Autowired
    private FarmTripAuditRepository farmTripAuditRepository;
    @Autowired
    private FarmTripOrderRepository farmTripOrderRepository;
    @Autowired
    private FarmTripCommentRepository farmTripCommentRepository;

    // ===== 活動本體 =====

    @Override
    public List<FarmTrip> getActiveTrips() {
        return farmTripRepository.findByStatus(FarmTripStatus.ACTIVE);
    }

    @Override
    public FarmTrip getTripById(Integer tripId) {
        // 查無回 null，由 controller 決定回 404
        return farmTripRepository.findById(tripId).orElse(null);
    }

    @Override
    public List<FarmTrip> getTripsByFarmer(Integer farmerId) {
        return farmTripRepository.findByFarmerId(farmerId);
    }

    @Override
    public FarmTrip createTrip(FarmTrip trip) {
        // 新活動一律待審核，統計歸零，避免前端亂帶
        trip.setStatus(FarmTripStatus.PENDING);
        trip.setCommentNumbers(0);
        trip.setStarNumbers(0);
        return farmTripRepository.save(trip);
    }

    @Override
    public FarmTrip updateTrip(Integer tripId, FarmTrip trip) {
        FarmTrip db = getOrThrow(farmTripRepository.findById(tripId), "體驗活動不存在: " + tripId);
        // 只開放可編輯欄位；status / farmerId / 評論統計 不讓前端覆蓋
        db.setFarmTripType(trip.getFarmTripType());
        db.setFarmTripTitle(trip.getFarmTripTitle());
        db.setFarmTripPic(trip.getFarmTripPic());
        db.setFarmTripIntro(trip.getFarmTripIntro());
        db.setLocation(trip.getLocation());
        db.setReferPrice(trip.getReferPrice());
        return farmTripRepository.save(db);
    }

    @Override
    public void deleteTrip(Integer tripId) {
        farmTripRepository.deleteById(tripId);
    }

    // ===== 審核 =====

    @Override
    public List<FarmTrip> getPendingTrips() {
        return farmTripRepository.findByStatus(FarmTripStatus.PENDING);
    }

    @Override
    @Transactional
    public void auditTrip(Integer tripId, FarmTripAudit audit) {
        FarmTrip trip = getOrThrow(farmTripRepository.findById(tripId), "體驗活動不存在: " + tripId);

        // 1) 寫一筆審核紀錄
        LocalDateTime now = LocalDateTime.now();
        audit.setFarmTripId(tripId);
        audit.setCreatedAt(now);
        audit.setUpdatedAt(now);
        farmTripAuditRepository.save(audit);

        // 2) 連動更新活動狀態：通過→上架(ACTIVE)，退回→REJECTED
        if (audit.getStatus() == FarmTripAuditStatus.APPROVED) {
            trip.setStatus(FarmTripStatus.ACTIVE);
        } else if (audit.getStatus() == FarmTripAuditStatus.REJECTED) {
            trip.setStatus(FarmTripStatus.REJECTED);
        }
        farmTripRepository.save(trip);
    }

    @Override
    public List<FarmTripAudit> getAuditHistory(Integer tripId) {
        return farmTripAuditRepository.findByFarmTripIdOrderByCreatedAtDesc(tripId);
    }

    // ===== 場次 =====

    @Override
    public List<FarmTripSession> getSessionsByTrip(Integer tripId) {
        return farmTripSessionRepository.findByFarmTripId(tripId);
    }

    @Override
    public FarmTripSession createSession(Integer tripId, FarmTripSession session) {
        session.setFarmTripId(tripId);
        if (session.getSessionStatus() == null) {
            session.setSessionStatus(FarmTripSessionStatus.ACTIVE);
        }
        return farmTripSessionRepository.save(session);
    }

    @Override
    @Transactional
    public List<FarmTripSession> createSessions(Integer tripId, List<FarmTripSession> sessions) {
        for (FarmTripSession s : sessions) {
            s.setFarmTripId(tripId);
            if (s.getSessionStatus() == null) {
                s.setSessionStatus(FarmTripSessionStatus.ACTIVE);
            }
        }
        return farmTripSessionRepository.saveAll(sessions);
    }

    @Override
    public FarmTripSession updateSession(Integer sessionId, FarmTripSession session) {
        FarmTripSession db = getOrThrow(farmTripSessionRepository.findById(sessionId), "場次不存在: " + sessionId);
        db.setFarmTripStart(session.getFarmTripStart());
        db.setFarmTripEnd(session.getFarmTripEnd());
        db.setTripBookStart(session.getTripBookStart());
        db.setTripBookEnd(session.getTripBookEnd());
        db.setAttendance(session.getAttendance());
        db.setSessionStatus(session.getSessionStatus());
        return farmTripSessionRepository.save(db);
    }

    @Override
    public void deleteSession(Integer sessionId) {
        farmTripSessionRepository.deleteById(sessionId);
    }

    // ===== 訂單 =====

    @Override
    @Transactional
    public FarmTripOrder bookSession(Integer sessionId, FarmTripOrder order) {
        FarmTripSession session = getOrThrow(farmTripSessionRepository.findById(sessionId), "場次不存在: " + sessionId);

        // 只有開放中的場次能預約
        if (session.getSessionStatus() != FarmTripSessionStatus.ACTIVE) {
            throw new IllegalStateException("此場次無法預約: " + sessionId);
        }

        // 名額檢查：已確認(CONFIRMED)訂單的人數加總，不可超過場次上限
        Integer cap = session.getAttendance();
        if (cap != null) {
            int booked = farmTripOrderRepository.findByFarmSessionId(sessionId).stream()
                    .filter(o -> o.getStatus() == FarmTripOrderStatus.CONFIRMED)
                    .mapToInt(o -> o.getNumPeople() == null ? 0 : o.getNumPeople())
                    .sum();
            int incoming = order.getNumPeople() == null ? 0 : order.getNumPeople();
            if (booked + incoming > cap) {
                throw new IllegalStateException("名額不足，剩餘 " + (cap - booked) + " 位");
            }
        }

        order.setFarmSessionId(sessionId);
        order.setStatus(FarmTripOrderStatus.CONFIRMED);
        order.setBookedAt(LocalDateTime.now());
        return farmTripOrderRepository.save(order);
    }

    @Override
    public List<FarmTripOrder> getOrdersByUser(Integer userId) {
        return farmTripOrderRepository.findByUserIdOrderByBookedAtDesc(userId);
    }

    @Override
    public List<FarmTripOrder> getOrdersByFarmer(Integer farmerId) {
        // B 方案：跨 訂單→場次→活動 查回小農的所有訂單
        return farmTripOrderRepository.findOrdersByFarmerId(farmerId);
    }

    @Override
    public void cancelOrder(Integer orderId) {
        FarmTripOrder order = getOrThrow(farmTripOrderRepository.findById(orderId), "訂單不存在: " + orderId);
        if (order.getStatus() != FarmTripOrderStatus.CONFIRMED) {
            throw new IllegalStateException("只有已確認的訂單可以取消: " + orderId);
        }
        order.setStatus(FarmTripOrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        farmTripOrderRepository.save(order);
        // 取消後名額自然釋出：因為名額只計算 CONFIRMED 的訂單
    }

    @Override
    public void completeOrder(Integer orderId) {
        FarmTripOrder order = getOrThrow(farmTripOrderRepository.findById(orderId), "訂單不存在: " + orderId);
        order.setStatus(FarmTripOrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        farmTripOrderRepository.save(order);
    }

    // ===== 評論 =====

    @Override
    public List<FarmTripComment> getCommentsByTrip(Integer tripId) {
        return farmTripCommentRepository.findByFarmTripIdOrderByCreatedAtDesc(tripId);
    }

    @Override
    @Transactional
    public FarmTripComment addComment(Integer tripId, FarmTripComment comment) {
        FarmTrip trip = getOrThrow(farmTripRepository.findById(tripId), "體驗活動不存在: " + tripId);

        // 1) 寫評論
        comment.setFarmTripId(tripId);
        comment.setCreatedAt(LocalDateTime.now());
        FarmTripComment saved = farmTripCommentRepository.save(comment);

        // 2) 回寫活動統計：評論數 +1、星數累加 (平均由前端用 星數總和 / 評論數 算)
        int count = trip.getCommentNumbers() == null ? 0 : trip.getCommentNumbers();
        int stars = trip.getStarNumbers() == null ? 0 : trip.getStarNumbers();
        int addStar = comment.getStar() == null ? 0 : comment.getStar();
        trip.setCommentNumbers(count + 1);
        trip.setStarNumbers(stars + addStar);
        farmTripRepository.save(trip);

        return saved;
    }

    // ===== 小工具 =====

    /** 統一處理「查無資料」：Optional 為空就丟例外，避免每個方法重複寫 */
    private <T> T getOrThrow(java.util.Optional<T> opt, String message) {
        return opt.orElseThrow(() -> new IllegalArgumentException(message));
    }
}
