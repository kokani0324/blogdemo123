package com.kuanyu.blogdemo123.farmtrip.dto;

import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripSession;
import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripSessionStatus;

import java.time.LocalDateTime;

/**
 * 場次的「回應」DTO。
 */
public class FarmTripSessionResponse {

    private Integer farmSessionId;
    private Integer farmTripId;
    private LocalDateTime farmTripStart;
    private LocalDateTime farmTripEnd;
    private LocalDateTime tripBookStart;
    private LocalDateTime tripBookEnd;
    private Integer attendance;
    private FarmTripSessionStatus sessionStatus;

    public FarmTripSessionResponse() {
    }

    /** entity → DTO */
    public static FarmTripSessionResponse from(FarmTripSession s) {
        FarmTripSessionResponse r = new FarmTripSessionResponse();
        r.farmSessionId = s.getFarmSessionId();
        r.farmTripId = s.getFarmTripId();
        r.farmTripStart = s.getFarmTripStart();
        r.farmTripEnd = s.getFarmTripEnd();
        r.tripBookStart = s.getTripBookStart();
        r.tripBookEnd = s.getTripBookEnd();
        r.attendance = s.getAttendance();
        r.sessionStatus = s.getSessionStatus();
        return r;
    }

    public Integer getFarmSessionId() {
        return farmSessionId;
    }

    public Integer getFarmTripId() {
        return farmTripId;
    }

    public LocalDateTime getFarmTripStart() {
        return farmTripStart;
    }

    public LocalDateTime getFarmTripEnd() {
        return farmTripEnd;
    }

    public LocalDateTime getTripBookStart() {
        return tripBookStart;
    }

    public LocalDateTime getTripBookEnd() {
        return tripBookEnd;
    }

    public Integer getAttendance() {
        return attendance;
    }

    public FarmTripSessionStatus getSessionStatus() {
        return sessionStatus;
    }
}
