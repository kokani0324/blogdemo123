package com.kuanyu.blogdemo123.farmtrip.service.impl;

import com.kuanyu.blogdemo123.farmtrip.dto.FarmTripAuditRequest;
import com.kuanyu.blogdemo123.farmtrip.dto.FarmTripOrderRequest;
import com.kuanyu.blogdemo123.farmtrip.dto.FarmTripRequest;
import com.kuanyu.blogdemo123.farmtrip.dto.FarmTripResponse;
import com.kuanyu.blogdemo123.farmtrip.dto.FarmTripSessionRequest;
import com.kuanyu.blogdemo123.farmtrip.dto.FarmTripSessionResponse;
import com.kuanyu.blogdemo123.farmtrip.entity.FarmTrip;
import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripAudit;
import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripAuditStatus;
import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripOrder;
import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripOrderStatus;
import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripSession;
import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripSessionStatus;
import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripStatus;
import com.kuanyu.blogdemo123.farmtrip.entity.TripType;
import com.kuanyu.blogdemo123.farmtrip.repository.FarmTripAuditRepository;
import com.kuanyu.blogdemo123.farmtrip.repository.FarmTripOrderRepository;
import com.kuanyu.blogdemo123.farmtrip.repository.FarmTripRepository;
import com.kuanyu.blogdemo123.farmtrip.repository.FarmTripSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarmTripServiceImplTest {

    @Mock
    private FarmTripSessionRepository farmTripSessionRepository;

    @Mock
    private FarmTripOrderRepository farmTripOrderRepository;

    @Mock
    private FarmTripRepository farmTripRepository;

    @Mock
    private FarmTripAuditRepository farmTripAuditRepository;

    @InjectMocks
    private FarmTripServiceImpl farmTripService;

    @Test
    void createTripCreatesPendingAuditWithoutAdmin() {
        FarmTripRequest request = newTripRequest();
        when(farmTripRepository.save(any(FarmTrip.class)))
                .thenAnswer(invocation -> {
                    FarmTrip trip = invocation.getArgument(0);
                    trip.setFarmTripId(10);
                    return trip;
                });
        ArgumentCaptor<FarmTripAudit> auditCaptor = ArgumentCaptor.forClass(FarmTripAudit.class);

        FarmTripResponse response = farmTripService.createTrip(request);

        verify(farmTripAuditRepository).save(auditCaptor.capture());
        FarmTripAudit audit = auditCaptor.getValue();
        assertEquals(FarmTripStatus.PENDING, response.getStatus());
        assertEquals(10, audit.getFarmTripId());
        assertEquals(FarmTripAuditStatus.PENDING, audit.getStatus());
        assertNull(audit.getAdminId());
        assertNull(audit.getReason());
        assertNotNull(audit.getCreatedAt());
    }

    @Test
    void updateRejectedTripCreatesNewPendingAudit() {
        FarmTrip trip = new FarmTrip();
        trip.setFarmTripId(10);
        trip.setStatus(FarmTripStatus.REJECTED);
        when(farmTripRepository.findById(10)).thenReturn(Optional.of(trip));
        when(farmTripRepository.save(any(FarmTrip.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<FarmTripAudit> auditCaptor = ArgumentCaptor.forClass(FarmTripAudit.class);

        FarmTripResponse response = farmTripService.updateTrip(10, newTripRequest());

        verify(farmTripAuditRepository).save(auditCaptor.capture());
        assertEquals(FarmTripStatus.PENDING, response.getStatus());
        assertEquals(FarmTripAuditStatus.PENDING, auditCaptor.getValue().getStatus());
        assertNull(auditCaptor.getValue().getAdminId());
    }

    @Test
    void updatePendingTripDoesNotCreateDuplicateAudit() {
        FarmTrip trip = new FarmTrip();
        trip.setFarmTripId(10);
        trip.setStatus(FarmTripStatus.PENDING);
        when(farmTripRepository.findById(10)).thenReturn(Optional.of(trip));

        assertThrows(IllegalStateException.class,
                () -> farmTripService.updateTrip(10, newTripRequest()));
        verify(farmTripAuditRepository, never()).save(any(FarmTripAudit.class));
    }

    @Test
    void auditTripUpdatesExistingPendingAudit() {
        FarmTrip trip = new FarmTrip();
        trip.setFarmTripId(10);
        trip.setStatus(FarmTripStatus.PENDING);
        FarmTripAudit audit = new FarmTripAudit();
        audit.setFarmTripId(10);
        audit.setStatus(FarmTripAuditStatus.PENDING);
        audit.setCreatedAt(LocalDateTime.now().minusDays(1));
        FarmTripAuditRequest request = new FarmTripAuditRequest();
        request.setAdminId(3);
        request.setStatus(FarmTripAuditStatus.APPROVED);
        request.setReason("資料完整");

        when(farmTripRepository.findById(10)).thenReturn(Optional.of(trip));
        when(farmTripAuditRepository.findFirstByFarmTripIdAndStatusOrderByCreatedAtDesc(
                10, FarmTripAuditStatus.PENDING)).thenReturn(Optional.of(audit));

        farmTripService.auditTrip(10, request);

        assertEquals(FarmTripStatus.ACTIVE, trip.getStatus());
        assertEquals(FarmTripAuditStatus.APPROVED, audit.getStatus());
        assertEquals(3, audit.getAdminId());
        assertEquals("資料完整", audit.getReason());
        assertNotNull(audit.getUpdatedAt());
        verify(farmTripAuditRepository).save(audit);
    }

    @Test
    void createSessionInitializesAttendanceToZero() {
        FarmTripSessionRequest request = new FarmTripSessionRequest();
        when(farmTripSessionRepository.save(any(FarmTripSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FarmTripSessionResponse response = farmTripService.createSession(10, request);

        assertEquals(0, response.getAttendance());
    }

    @Test
    void bookSessionAddsNumPeopleToAttendance() {
        FarmTripSession session = new FarmTripSession();
        session.setFarmSessionId(20);
        session.setSessionStatus(FarmTripSessionStatus.ACTIVE);
        session.setAttendance(3);

        FarmTripOrderRequest request = new FarmTripOrderRequest();
        request.setUserId(1);
        request.setNumPeople(2);
        request.setUserName("測試會員");
        request.setUserPhoneNum("0912345678");

        when(farmTripSessionRepository.findById(20)).thenReturn(Optional.of(session));
        when(farmTripOrderRepository.save(any(FarmTripOrder.class)))
                .thenAnswer(invocation -> {
                    FarmTripOrder order = invocation.getArgument(0);
                    order.setFarmTripOrderId(30);
                    return order;
                });

        var response = farmTripService.bookSession(20, request);

        assertEquals(5, session.getAttendance());
        assertTrue(response.getFarmTripOrderBookingNo().matches("FT\\d{8}-00000030"));
    }

    @Test
    void cancelOrderSubtractsNumPeopleFromAttendance() {
        FarmTripOrder order = new FarmTripOrder();
        order.setFarmTripOrderId(30);
        order.setFarmSessionId(20);
        order.setNumPeople(2);
        order.setStatus(FarmTripOrderStatus.CONFIRMED);

        FarmTripSession session = new FarmTripSession();
        session.setFarmSessionId(20);
        session.setAttendance(5);

        when(farmTripOrderRepository.findById(30)).thenReturn(Optional.of(order));
        when(farmTripSessionRepository.findById(20)).thenReturn(Optional.of(session));

        farmTripService.cancelOrder(30);

        assertEquals(FarmTripOrderStatus.CANCELLED, order.getStatus());
        assertEquals(3, session.getAttendance());
    }

    private FarmTripRequest newTripRequest() {
        FarmTripRequest request = new FarmTripRequest();
        request.setFarmerId(1);
        request.setFarmTripType(TripType.FARM_EXPERIENCE);
        request.setFarmTripTitle("採果體驗");
        request.setFarmTripIntro("活動介紹");
        request.setLocation("台中");
        request.setReferPrice(500);
        return request;
    }
}
