package com.kuanyu.blogdemo123.farmtrip.dto;

import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripSessionStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * 場次的「請求」DTO（小農送進來，新增/修改共用）。
 *
 * 只放前端該給的：活動時間、報名時間、狀態。
 * 刻意不放 farmSessionId（新增時後端產生）、farmTripId（由網址帶）。
 * attendance 是目前有效預約人數，由後端依訂單維護，不讓前端指定。
 *
 * 註：sessionStatus 保留給前端 —— 場次的開放/取消是小農自己能決定的
 *     （跟活動 status 要管理員審核不同）。新增時不給則預設 ACTIVE。
 */
public class FarmTripSessionRequest {

    @NotNull
    private LocalDateTime farmTripStart;

    @NotNull
    private LocalDateTime farmTripEnd;

    private LocalDateTime tripBookStart;
    private LocalDateTime tripBookEnd;

    private FarmTripSessionStatus sessionStatus;

    public FarmTripSessionRequest() {
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

    public FarmTripSessionStatus getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(FarmTripSessionStatus sessionStatus) {
        this.sessionStatus = sessionStatus;
    }
}
