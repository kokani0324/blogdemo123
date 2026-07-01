package com.kuanyu.blogdemo123.farmtrip.dto;

import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripOrder;
import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripOrderStatus;

import java.time.LocalDateTime;

/**
 * 預約訂單的「回應」DTO。
 */
public class FarmTripOrderResponse {

    private Integer farmTripOrderId;
    private Integer farmSessionId;
    private Integer userId;
    private String farmTripOrderBookingNo;
    private Integer numPeople;
    private FarmTripOrderStatus status;
    private LocalDateTime bookedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime completedAt;
    private String userName;
    private String userPhoneNum;
    private String note;

    public FarmTripOrderResponse() {
    }

    /** entity → DTO */
    public static FarmTripOrderResponse from(FarmTripOrder o) {
        FarmTripOrderResponse r = new FarmTripOrderResponse();
        r.farmTripOrderId = o.getFarmTripOrderId();
        r.farmSessionId = o.getFarmSessionId();
        r.userId = o.getUserId();
        r.farmTripOrderBookingNo = o.getFarmTripOrderBookingNo();
        r.numPeople = o.getNumPeople();
        r.status = o.getStatus();
        r.bookedAt = o.getBookedAt();
        r.cancelledAt = o.getCancelledAt();
        r.completedAt = o.getCompletedAt();
        r.userName = o.getUserName();
        r.userPhoneNum = o.getUserPhoneNum();
        r.note = o.getNote();
        return r;
    }

    public Integer getFarmTripOrderId() {
        return farmTripOrderId;
    }

    public Integer getFarmSessionId() {
        return farmSessionId;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getFarmTripOrderBookingNo() {
        return farmTripOrderBookingNo;
    }

    public Integer getNumPeople() {
        return numPeople;
    }

    public FarmTripOrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getBookedAt() {
        return bookedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPhoneNum() {
        return userPhoneNum;
    }

    public String getNote() {
        return note;
    }
}
