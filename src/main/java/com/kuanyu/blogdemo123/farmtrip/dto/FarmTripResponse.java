package com.kuanyu.blogdemo123.farmtrip.dto;

import com.kuanyu.blogdemo123.farmtrip.entity.FarmTrip;
import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripStatus;
import com.kuanyu.blogdemo123.farmtrip.entity.TripType;

/**
 * 體驗活動的「回應」DTO。
 *
 * 只放前端要顯示的欄位（不吐 farmTripPic 那包 byte[] 圖片）。
 * 而且直接幫前端算好平均星數 avgStar，前端不用自己算。
 */
public class FarmTripResponse {

    private Integer farmTripId;
    private Integer farmerId;
    private TripType farmTripType;
    private String farmTripTitle;
    private String farmTripIntro;
    private String location;
    private Integer referPrice;
    private FarmTripStatus status;
    private Integer commentNumbers;
    private Double avgStar;   // 幫前端算好：星數總和 / 評論數

    public FarmTripResponse() {
    }

    /** entity → DTO 的轉換集中在這裡，controller 只要呼叫 from(...) */
    public static FarmTripResponse from(FarmTrip trip) {
        FarmTripResponse r = new FarmTripResponse();
        r.farmTripId = trip.getFarmTripId();
        r.farmerId = trip.getFarmerId();
        r.farmTripType = trip.getFarmTripType();
        r.farmTripTitle = trip.getFarmTripTitle();
        r.farmTripIntro = trip.getFarmTripIntro();
        r.location = trip.getLocation();
        r.referPrice = trip.getReferPrice();
        r.status = trip.getStatus();
        r.commentNumbers = trip.getCommentNumbers();

        // 平均星數：沒有評論就給 0，避免除以 0
        int count = trip.getCommentNumbers() == null ? 0 : trip.getCommentNumbers();
        int stars = trip.getStarNumbers() == null ? 0 : trip.getStarNumbers();
        r.avgStar = (count == 0) ? 0.0 : (double) stars / count;

        return r;
    }

    public Integer getFarmTripId() {
        return farmTripId;
    }

    public Integer getFarmerId() {
        return farmerId;
    }

    public TripType getFarmTripType() {
        return farmTripType;
    }

    public String getFarmTripTitle() {
        return farmTripTitle;
    }

    public String getFarmTripIntro() {
        return farmTripIntro;
    }

    public String getLocation() {
        return location;
    }

    public Integer getReferPrice() {
        return referPrice;
    }

    public FarmTripStatus getStatus() {
        return status;
    }

    public Integer getCommentNumbers() {
        return commentNumbers;
    }

    public Double getAvgStar() {
        return avgStar;
    }
}
