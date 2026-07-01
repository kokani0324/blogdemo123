package com.kuanyu.blogdemo123.farmtrip.dto;

import com.kuanyu.blogdemo123.farmtrip.entity.TripType;

/**
 * 新增/修改體驗活動的「請求」DTO。
 *
 * 只放前端「該給」的欄位。
 * 刻意不放 status / commentNumbers / starNumbers / farmTripId ——
 * 這些由後端決定，前端連傳的機會都沒有（從源頭防止亂帶）。
 */
public class FarmTripRequest {

    private Integer farmerId;
    private TripType farmTripType;
    private String farmTripTitle;
    private String farmTripIntro;
    private String location;
    private Integer referPrice;

    public FarmTripRequest() {
    }

    public Integer getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(Integer farmerId) {
        this.farmerId = farmerId;
    }

    public TripType getFarmTripType() {
        return farmTripType;
    }

    public void setFarmTripType(TripType farmTripType) {
        this.farmTripType = farmTripType;
    }

    public String getFarmTripTitle() {
        return farmTripTitle;
    }

    public void setFarmTripTitle(String farmTripTitle) {
        this.farmTripTitle = farmTripTitle;
    }

    public String getFarmTripIntro() {
        return farmTripIntro;
    }

    public void setFarmTripIntro(String farmTripIntro) {
        this.farmTripIntro = farmTripIntro;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getReferPrice() {
        return referPrice;
    }

    public void setReferPrice(Integer referPrice) {
        this.referPrice = referPrice;
    }
}
