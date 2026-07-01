package com.kuanyu.blogdemo123.farmtrip.service.impl;

import com.kuanyu.blogdemo123.farmtrip.dto.*;
import com.kuanyu.blogdemo123.farmtrip.entity.*;
import com.kuanyu.blogdemo123.farmtrip.repository.*;
import com.kuanyu.blogdemo123.farmtrip.service.FarmTripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 體驗活動模組 service 實作。
 *
 * DTO ↔ entity 的轉換都在這一層：
 *   - 進來的 XxxRequest → entity（私有 toEntity/apply 方法）
 *   - 出去的 entity → XxxResponse（呼叫各 Response DTO 的 from()）
 */
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
    public List<FarmTripResponse> getActiveTrips() {
        return farmTripRepository.findByStatus(FarmTripStatus.ACTIVE).stream()
                .map(FarmTripResponse::from)
                .toList();
    }

    @Override
    public FarmTripResponse getTripById(Integer tripId) {
        // 查無回 null，由 controller 決定回 404
        return farmTripRepository.findById(tripId)
                .map(FarmTripResponse::from)
                .orElse(null);
    }

    @Override
    public List<FarmTripResponse> getTripsByFarmer(Integer farmerId) {
        return farmTripRepository.findByFarmerId(farmerId).stream()
                .map(FarmTripResponse::from)
                .toList();
    }

    @Override
    public FarmTripResponse createTrip(FarmTripRequest request) {
        FarmTrip trip = new FarmTrip();
        // DTO → entity：只搬前端該給的欄位
        trip.setFarmerId(request.getFarmerId());
        trip.setFarmTripType(request.getFarmTripType());
        trip.setFarmTripTitle(request.getFarmTripTitle());
        trip.setFarmTripIntro(request.getFarmTripIntro());
        trip.setLocation(request.getLocation());
        trip.setReferPrice(request.getReferPrice());
        // 後端強制：新活動一律待審核、統計歸零
        trip.setStatus(FarmTripStatus.PENDING);
        trip.setCommentNumbers(0);
        trip.setStarNumbers(0);
        return FarmTripResponse.from(farmTripRepository.save(trip));
    }

    @Override
    public FarmTripResponse updateTrip(Integer tripId, FarmTripRequest request) {
        FarmTrip db = getOrThrow(farmTripRepository.findById(tripId), "體驗活動不存在: " + tripId);
        // 只開放可編輯欄位；status / farmerId / 評論統計 不讓前端覆蓋
        db.setFarmTripType(request.getFarmTripType());
        db.setFarmTripTitle(request.getFarmTripTitle());
        db.setFarmTripIntro(request.getFarmTripIntro());
        db.setLocation(request.getLocation());
        db.setReferPrice(request.getReferPrice());
        return FarmTripResponse.from(farmTripRepository.save(db));
    }

    @Override
    public void deleteTrip(Integer tripId) {
        farmTripRepository.deleteById(tripId);
    }

    // ===== 審核 =====

    @Override
    public List<FarmTripResponse> getPendingTrips() {
        return farmTripRepository.findByStatus(FarmTripStatus.PENDING).stream()
                .map(FarmTripResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void auditTrip(Integer tripId, FarmTripAuditRequest request) {
        FarmTrip trip = getOrThrow(farmTripRepository.findById(tripId), "體驗活動不存在: " + tripId);

        // 1) 寫一筆審核紀錄
        LocalDateTime now = LocalDateTime.now();
        FarmTripAudit audit = new FarmTripAudit();
        audit.setFarmTripId(tripId);
        audit.setAdminId(request.getAdminId());
        audit.setStatus(request.getStatus());
        audit.setReason(request.getReason());
        audit.setCreatedAt(now);
        audit.setUpdatedAt(now);
        farmTripAuditRepository.save(audit);

        // 2) 連動更新活動狀態：通過→上架(ACTIVE)，退回→REJECTED
        if (request.getStatus() == FarmTripAuditStatus.APPROVED) {
            trip.setStatus(FarmTripStatus.ACTIVE);
        } else if (request.getStatus() == FarmTripAuditStatus.REJECTED) {
            trip.setStatus(FarmTripStatus.REJECTED);
        }
        farmTripRepository.save(trip);
    }

    @Override
    public List<FarmTripAuditResponse> getAuditHistory(Integer tripId) {
        return farmTripAuditRepository.findByFarmTripIdOrderByCreatedAtDesc(tripId).stream()
                .map(FarmTripAuditResponse::from)
                .toList();
    }

    // ===== 場次 =====

    @Override
    public List<FarmTripSessionResponse> getSessionsByTrip(Integer tripId) {
        return farmTripSessionRepository.findByFarmTripId(tripId).stream()
                .map(FarmTripSessionResponse::from)
                .toList();
    }

    @Override
    public FarmTripSessionResponse createSession(Integer tripId, FarmTripSessionRequest request) {
        FarmTripSession session = toSessionEntity(tripId, request);
        return FarmTripSessionResponse.from(farmTripSessionRepository.save(session));
    }

    @Override
    @Transactional
    public List<FarmTripSessionResponse> createSessions(Integer tripId, List<FarmTripSessionRequest> requests) {
        List<FarmTripSession> sessions = requests.stream()
                .map(r -> toSessionEntity(tripId, r))
                .toList();
        return farmTripSessionRepository.saveAll(sessions).stream()
                .map(FarmTripSessionResponse::from)
                .toList();
    }

    @Override
    public FarmTripSessionResponse updateSession(Integer sessionId, FarmTripSessionRequest request) {
        FarmTripSession db = getOrThrow(farmTripSessionRepository.findById(sessionId), "場次不存在: " + sessionId);
        db.setFarmTripStart(request.getFarmTripStart());
        db.setFarmTripEnd(request.getFarmTripEnd());
        db.setTripBookStart(request.getTripBookStart());
        db.setTripBookEnd(request.getTripBookEnd());
        db.setAttendance(request.getAttendance());
        if (request.getSessionStatus() != null) {
            db.setSessionStatus(request.getSessionStatus());
        }
        return FarmTripSessionResponse.from(farmTripSessionRepository.save(db));
    }

    @Override
    public void deleteSession(Integer sessionId) {
        farmTripSessionRepository.deleteById(sessionId);
    }

    // ===== 訂單 =====

    @Override
    @Transactional
    public FarmTripOrderResponse bookSession(Integer sessionId, FarmTripOrderRequest request) {
        FarmTripSession session = getOrThrow(farmTripSessionRepository.findById(sessionId), "場次不存在: " + sessionId);

        // 只有開放中的場次能預約
        if (session.getSessionStatus() != FarmTripSessionStatus.ACTIVE) {
            throw new IllegalStateException("此場次無法預約: " + sessionId);
        }

        // 名額檢查：已確認(CONFIRMED)訂單的人數加總，不可超過場次上限
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

        // DTO → entity
        FarmTripOrder order = new FarmTripOrder();
        order.setFarmSessionId(sessionId);
        order.setUserId(request.getUserId());
        order.setNumPeople(request.getNumPeople());
        order.setUserName(request.getUserName());
        order.setUserPhoneNum(request.getUserPhoneNum());
        order.setNote(request.getNote());
        order.setStatus(FarmTripOrderStatus.CONFIRMED);
        order.setBookedAt(LocalDateTime.now());
        return FarmTripOrderResponse.from(farmTripOrderRepository.save(order));
    }

    @Override
    public List<FarmTripOrderResponse> getOrdersByUser(Integer userId) {
        return farmTripOrderRepository.findByUserIdOrderByBookedAtDesc(userId).stream()
                .map(FarmTripOrderResponse::from)
                .toList();
    }

    @Override
    public List<FarmTripOrderResponse> getOrdersByFarmer(Integer farmerId) {
        // B 方案：跨 訂單→場次→活動 查回小農的所有訂單
        return farmTripOrderRepository.findOrdersByFarmerId(farmerId).stream()
                .map(FarmTripOrderResponse::from)
                .toList();
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
    public List<FarmTripCommentResponse> getCommentsByTrip(Integer tripId) {
        return farmTripCommentRepository.findByFarmTripIdOrderByCreatedAtDesc(tripId).stream()
                .map(FarmTripCommentResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public FarmTripCommentResponse addComment(Integer tripId, FarmTripCommentRequest request) {
        FarmTrip trip = getOrThrow(farmTripRepository.findById(tripId), "體驗活動不存在: " + tripId);

        // 1) 寫評論（DTO → entity）
        FarmTripComment comment = new FarmTripComment();
        comment.setFarmTripId(tripId);
        comment.setUserId(request.getUserId());
        comment.setStar(request.getStar());
        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        FarmTripComment saved = farmTripCommentRepository.save(comment);

        // 2) 回寫活動統計：評論數 +1、星數累加
        int count = trip.getCommentNumbers() == null ? 0 : trip.getCommentNumbers();
        int stars = trip.getStarNumbers() == null ? 0 : trip.getStarNumbers();
        int addStar = request.getStar() == null ? 0 : request.getStar();
        trip.setCommentNumbers(count + 1);
        trip.setStarNumbers(stars + addStar);
        farmTripRepository.save(trip);

        return FarmTripCommentResponse.from(saved);
    }

    // ===== 小工具 =====

    /** SessionRequest DTO → entity（新增/批次共用；新增時沒帶狀態則預設 ACTIVE） */
    private FarmTripSession toSessionEntity(Integer tripId, FarmTripSessionRequest request) {
        FarmTripSession session = new FarmTripSession();
        session.setFarmTripId(tripId);
        session.setFarmTripStart(request.getFarmTripStart());
        session.setFarmTripEnd(request.getFarmTripEnd());
        session.setTripBookStart(request.getTripBookStart());
        session.setTripBookEnd(request.getTripBookEnd());
        session.setAttendance(request.getAttendance());
        session.setSessionStatus(request.getSessionStatus() == null
                ? FarmTripSessionStatus.ACTIVE : request.getSessionStatus());
        return session;
    }

    /** 統一處理「查無資料」：Optional 為空就丟例外，避免每個方法重複寫 */
    private <T> T getOrThrow(Optional<T> opt, String message) {
        return opt.orElseThrow(() -> new IllegalArgumentException(message));
    }
}
