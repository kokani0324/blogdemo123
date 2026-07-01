package com.kuanyu.blogdemo123.farmtrip.dto;

import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripAudit;
import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripAuditStatus;

import java.time.LocalDateTime;

/**
 * 審核紀錄的「回應」DTO（審核歷史用）。
 */
public class FarmTripAuditResponse {

    private Integer farmTripAuditsId;
    private Integer farmTripId;
    private Integer adminId;
    private FarmTripAuditStatus status;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FarmTripAuditResponse() {
    }

    /** entity → DTO */
    public static FarmTripAuditResponse from(FarmTripAudit a) {
        FarmTripAuditResponse r = new FarmTripAuditResponse();
        r.farmTripAuditsId = a.getFarmTripAuditsId();
        r.farmTripId = a.getFarmTripId();
        r.adminId = a.getAdminId();
        r.status = a.getStatus();
        r.reason = a.getReason();
        r.createdAt = a.getCreatedAt();
        r.updatedAt = a.getUpdatedAt();
        return r;
    }

    public Integer getFarmTripAuditsId() {
        return farmTripAuditsId;
    }

    public Integer getFarmTripId() {
        return farmTripId;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public FarmTripAuditStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
