package com.kuanyu.blogdemo123.farmtrip.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 發表評論的「請求」DTO（會員送進來）。
 *
 * 只放前端該給的：星數、內容。
 * 刻意不放 farmTripId（由網址帶）、createdAt（後端給）。
 *
 * 註：userId 目前暫放，之後有登入機制後改從登入身分取得。
 */
public class FarmTripCommentRequest {

    @NotNull
    private Integer userId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer star;

    @Size(max = 255)
    private String content;

    public FarmTripCommentRequest() {
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
}
