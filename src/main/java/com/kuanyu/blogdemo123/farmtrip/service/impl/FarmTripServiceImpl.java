package com.kuanyu.blogdemo123.farmtrip.service.impl;

import com.kuanyu.blogdemo123.farmtrip.dto.*;
import com.kuanyu.blogdemo123.farmtrip.entity.*;
import com.kuanyu.blogdemo123.farmtrip.repository.*;
import com.kuanyu.blogdemo123.farmtrip.service.FarmTripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    @Transactional
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
        FarmTrip savedTrip = farmTripRepository.save(trip);

        // 小農送出新活動時，立即建立一筆尚未指派管理員的待審核紀錄
        createPendingAudit(savedTrip.getFarmTripId());

        return FarmTripResponse.from(savedTrip);
    }

    @Override
    @Transactional
    public FarmTripResponse updateTrip(Integer tripId, FarmTripRequest request) {
        FarmTrip db = getOrThrow(farmTripRepository.findById(tripId), "體驗活動不存在: " + tripId);
        if (db.getStatus() == FarmTripStatus.PENDING) {
            throw new IllegalStateException("活動已在待審核中，不能重複送審: " + tripId);
        }

        // 只開放可編輯欄位；farmerId / 評論統計 不讓前端覆蓋
        db.setFarmTripType(request.getFarmTripType());
        db.setFarmTripTitle(request.getFarmTripTitle());
        db.setFarmTripIntro(request.getFarmTripIntro());
        db.setLocation(request.getLocation());
        db.setReferPrice(request.getReferPrice());
        db.setStatus(FarmTripStatus.PENDING);
        FarmTrip savedTrip = farmTripRepository.save(db);

        // 被拒絕或已上架的活動修改後重新送審，每次都保留一筆新的審核歷史
        createPendingAudit(savedTrip.getFarmTripId());

        return FarmTripResponse.from(savedTrip);
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
        if (trip.getStatus() != FarmTripStatus.PENDING) {
            throw new IllegalStateException("活動目前不是待審核狀態: " + tripId);
        }
        if (request.getStatus() == FarmTripAuditStatus.PENDING) {
            throw new IllegalArgumentException("管理員只能選擇 APPROVED 或 REJECTED");
        }

        // 1) 更新小農送審時建立的最新一筆 PENDING 紀錄
        FarmTripAudit audit = getOrThrow(
                farmTripAuditRepository.findFirstByFarmTripIdAndStatusOrderByCreatedAtDesc(
                        tripId, FarmTripAuditStatus.PENDING),
                "找不到待審核紀錄，活動編號: " + tripId);
        audit.setAdminId(request.getAdminId());
        audit.setStatus(request.getStatus());
        audit.setReason(request.getReason());
        audit.setUpdatedAt(LocalDateTime.now());
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
        FarmTripOrder savedOrder = farmTripOrderRepository.save(order);

        String bookingNo =
                "FT"
                        + savedOrder.getBookedAt()
                        .format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                        + "-"
                        + String.format("%08d", savedOrder.getFarmTripOrderId());

        savedOrder.setFarmTripOrderBookingNo(bookingNo);
        farmTripOrderRepository.save(savedOrder);

        // attendance 代表目前有效預約人數，由後端在預約成功後累加
        int attendance = session.getAttendance() == null ? 0 : session.getAttendance();
        session.setAttendance(attendance + request.getNumPeople());
        farmTripSessionRepository.save(session);

        return FarmTripOrderResponse.from(savedOrder);
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
    @Transactional
    public void cancelOrder(Integer orderId) {
        FarmTripOrder order = getOrThrow(farmTripOrderRepository.findById(orderId), "訂單不存在: " + orderId);
        if (order.getStatus() != FarmTripOrderStatus.CONFIRMED) {
            throw new IllegalStateException("只有已確認的訂單可以取消: " + orderId);
        }
        order.setStatus(FarmTripOrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        farmTripOrderRepository.save(order);

        // 取消已確認的訂單後，扣回該筆訂單原本計入的預約人數
        FarmTripSession session = getOrThrow(
                farmTripSessionRepository.findById(order.getFarmSessionId()),
                "場次不存在: " + order.getFarmSessionId());
        int attendance = session.getAttendance() == null ? 0 : session.getAttendance();
        int cancelledPeople = order.getNumPeople() == null ? 0 : order.getNumPeople();
        session.setAttendance(Math.max(0, attendance - cancelledPeople));
        farmTripSessionRepository.save(session);
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

    /** 小農每次送審時建立一筆新的待審核歷史；管理員審核前 adminId 保持 null */
    private void createPendingAudit(Integer tripId) {
        LocalDateTime now = LocalDateTime.now();
        FarmTripAudit audit = new FarmTripAudit();
        audit.setFarmTripId(tripId);
        audit.setAdminId(null);
        audit.setStatus(FarmTripAuditStatus.PENDING);
        audit.setReason(null);
        audit.setCreatedAt(now);
        audit.setUpdatedAt(now);
        farmTripAuditRepository.save(audit);
    }

    /** SessionRequest DTO → entity（新增/批次共用；新增時沒帶狀態則預設 ACTIVE） */
    private FarmTripSession toSessionEntity(Integer tripId, FarmTripSessionRequest request) {
        FarmTripSession session = new FarmTripSession();
        session.setFarmTripId(tripId);
        session.setFarmTripStart(request.getFarmTripStart());
        session.setFarmTripEnd(request.getFarmTripEnd());
        session.setTripBookStart(request.getTripBookStart());
        session.setTripBookEnd(request.getTripBookEnd());
        session.setAttendance(0);
        session.setSessionStatus(request.getSessionStatus() == null
                ? FarmTripSessionStatus.ACTIVE : request.getSessionStatus());
        return session;
    }

    /** 統一處理「查無資料」：Optional 為空就丟例外，避免每個方法重複寫 */
    private <T> T getOrThrow(Optional<T> opt, String message) {
        return opt.orElseThrow(() -> new IllegalArgumentException(message));
    }
}
