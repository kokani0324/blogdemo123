package com.kuanyu.blogdemo123.farmtrip.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 體驗活動場次 (FARM_TRIP_SESSION)
 */
@Entity
@Table(name = "farm_trip_session")
public class FarmTripSession implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "farm_session_id", updatable = false)
    private Integer farmSessionId;

    @Column(name = "farm_trip_id", nullable = false)
    private Integer farmTripId;

    /** 活動開始 */
    @Column(name = "farm_trip_start")
    private LocalDateTime farmTripStart;

    /** 活動結束 */
    @Column(name = "farm_trip_end")
    private LocalDateTime farmTripEnd;

    /** 報名開始 */
    @Column(name = "trip_book_start")
    private LocalDateTime tripBookStart;

    /** 報名截止 */
    @Column(name = "trip_book_end")
    private LocalDateTime tripBookEnd;

    /** 名額上限 */
    @Column(name = "attendance")
    private Integer attendance;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_status")
    private FarmTripSessionStatus sessionStatus;

    public FarmTripSession() {
        super();
    }

    public Integer getFarmSessionId() {
        return farmSessionId;
    }

    public void setFarmSessionId(Integer farmSessionId) {
        this.farmSessionId = farmSessionId;
    }

    public Integer getFarmTripId() {
        return farmTripId;
    }

    public void setFarmTripId(Integer farmTripId) {
        this.farmTripId = farmTripId;
    }

    public LocalDateTime getFarmTripStart() {
        return farmTripStart;
    }

    public void setFarmTripStart(LocalDateTime farmTripStart) {
        this.farmTripStart = farmTripStart;
    }

    public LocalDateTime getFarmTripEnd() {
        return farmTripEnd;
    }

    public void setFarmTripEnd(LocalDateTime farmTripEnd) {
        this.farmTripEnd = farmTripEnd;
    }

    public LocalDateTime getTripBookStart() {
        return tripBookStart;
    }

    public void setTripBookStart(LocalDateTime tripBookStart) {
        this.tripBookStart = tripBookStart;
    }

    public LocalDateTime getTripBookEnd() {
        return tripBookEnd;
    }

    public void setTripBookEnd(LocalDateTime tripBookEnd) {
        this.tripBookEnd = tripBookEnd;
    }

    public Integer getAttendance() {
        return attendance;
    }

    public void setAttendance(Integer attendance) {
        this.attendance = attendance;
    }

    public FarmTripSessionStatus getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(FarmTripSessionStatus sessionStatus) {
        this.sessionStatus = sessionStatus;
    }
}
