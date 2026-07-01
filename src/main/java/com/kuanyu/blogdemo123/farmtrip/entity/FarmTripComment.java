package com.kuanyu.blogdemo123.farmtrip.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 體驗活動評論 (FARM_TRIP_COMMENT)
 */
@Entity
@Table(name = "farm_trip_comment")
public class FarmTripComment implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "farm_trip_comment_id", updatable = false)
    private Integer farmTripCommentId;

    @Column(name = "farm_trip_id", nullable = false)
    private Integer farmTripId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    /** 星數 */
    @Column(name = "star")
    private Integer star;

    @Column(name = "content", length = 255)
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public FarmTripComment() {
        super();
    }

    public Integer getFarmTripCommentId() {
        return farmTripCommentId;
    }

    public void setFarmTripCommentId(Integer farmTripCommentId) {
        this.farmTripCommentId = farmTripCommentId;
    }

    public Integer getFarmTripId() {
        return farmTripId;
    }

    public void setFarmTripId(Integer farmTripId) {
        this.farmTripId = farmTripId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getStar() {
        return star;
    }

    public void setStar(Integer star) {
        this.star = star;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
