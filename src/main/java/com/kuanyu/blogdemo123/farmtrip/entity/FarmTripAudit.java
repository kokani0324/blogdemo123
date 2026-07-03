package com.kuanyu.blogdemo123.farmtrip.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 體驗活動審核 (FARM_TRIP_AUDITS)
 */
@Entity
@Table(name = "farm_trip_audits")
public class FarmTripAudit implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "farm_trip_audits_id", updatable = false)
    private Integer farmTripAuditsId;

    @Column(name = "farm_trip_id", nullable = false)
    private Integer farmTripId;

    /** 待審核時尚未指派管理員，因此允許為 null；做出審核決定後才寫入 */
    @Column(name = "admin_id")
    private Integer adminId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private FarmTripAuditStatus status;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public FarmTripAudit() {
        super();
    }

    public Integer getFarmTripAuditsId() {
        return farmTripAuditsId;
    }

    public void setFarmTripAuditsId(Integer farmTripAuditsId) {
        this.farmTripAuditsId = farmTripAuditsId;
    }

    public Integer getFarmTripId() {
        return farmTripId;
    }

    public void setFarmTripId(Integer farmTripId) {
        this.farmTripId = farmTripId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
