package com.kuanyu.blogdemo123.farmtrip.dto;

import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripAuditStatus;

/**
 * 審核的「請求」DTO（管理員送進來）。
 *
 * 只放前端該給的：審核結果、理由。
 * 刻意不放 farmTripId（由網址帶）、createdAt/updatedAt（後端給）。
 *
 * 註：adminId 目前暫放，之後有登入機制後改從登入身分取得。
 */
public class FarmTripAuditRequest {

    private Integer adminId;
    private FarmTripAuditStatus status;
    private String reason;

    public FarmTripAuditRequest() {
    }

    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public FarmTripAuditStatus getStatus() {
        return status;
    }

    public void setStatus(FarmTripAuditStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
