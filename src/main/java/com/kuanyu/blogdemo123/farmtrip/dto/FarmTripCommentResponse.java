package com.kuanyu.blogdemo123.farmtrip.dto;

import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripComment;

import java.time.LocalDateTime;

/**
 * 評論的「回應」DTO。
 */
public class FarmTripCommentResponse {

    private Integer farmTripCommentId;
    private Integer farmTripId;
    private Integer userId;
    private Integer star;
    private String content;
    private LocalDateTime createdAt;

    public FarmTripCommentResponse() {
    }

    /** entity → DTO */
    public static FarmTripCommentResponse from(FarmTripComment c) {
        FarmTripCommentResponse r = new FarmTripCommentResponse();
        r.farmTripCommentId = c.getFarmTripCommentId();
        r.farmTripId = c.getFarmTripId();
        r.userId = c.getUserId();
        r.star = c.getStar();
        r.content = c.getContent();
        r.createdAt = c.getCreatedAt();
        return r;
    }

    public Integer getFarmTripCommentId() {
        return farmTripCommentId;
    }

    public Integer getFarmTripId() {
        return farmTripId;
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getStar() {
        return star;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
