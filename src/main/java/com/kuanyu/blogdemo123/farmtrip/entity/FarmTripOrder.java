package com.kuanyu.blogdemo123.farmtrip.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 體驗活動預約訂單 (FARM_TRIP_ORDER)
 */
@Entity
@Table(name = "farm_trip_order")
public class FarmTripOrder implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "farm_trip_order_id", updatable = false)
    private Integer farmTripOrderId;

    @Column(name = "farm_session_id", nullable = false)
    private Integer farmSessionId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    /** 訂單編號 */
    @Column(name = "farm_trip_order_booking_no", length = 30)
    private String farmTripOrderBookingNo;

    /** 報名人數 */
    @Column(name = "num_people", nullable = false)
    private Integer numPeople;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private FarmTripOrderStatus status;

    @Column(name = "booked_at", nullable = false)
    private LocalDateTime bookedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "user_name", length = 30, nullable = false)
    private String userName;

    @Column(name = "user_phone_num", length = 15, nullable = false)
    private String userPhoneNum;

    @Column(name = "note", length = 100)
    private String note;

    public FarmTripOrder() {
        super();
    }

    public Integer getFarmTripOrderId() {
        return farmTripOrderId;
    }

    public void setFarmTripOrderId(Integer farmTripOrderId) {
        this.farmTripOrderId = farmTripOrderId;
    }

    public Integer getFarmSessionId() {
        return farmSessionId;
    }

    public void setFarmSessionId(Integer farmSessionId) {
        this.farmSessionId = farmSessionId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getFarmTripOrderBookingNo() {
        return farmTripOrderBookingNo;
    }

    public void setFarmTripOrderBookingNo(String farmTripOrderBookingNo) {
        this.farmTripOrderBookingNo = farmTripOrderBookingNo;
    }

    public Integer getNumPeople() {
        return numPeople;
    }

    public void setNumPeople(Integer numPeople) {
        this.numPeople = numPeople;
    }

    public FarmTripOrderStatus getStatus() {
        return status;
    }

    public void setStatus(FarmTripOrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(LocalDateTime bookedAt) {
        this.bookedAt = bookedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhoneNum() {
        return userPhoneNum;
    }

    public void setUserPhoneNum(String userPhoneNum) {
        this.userPhoneNum = userPhoneNum;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
