package com.kuanyu.blogdemo123.farmtrip.dto;

import com.kuanyu.blogdemo123.farmtrip.entity.TripType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 新增/修改體驗活動的「請求」DTO。
 *
 * 只放前端「該給」的欄位。
 * 刻意不放 status / commentNumbers / starNumbers / farmTripId ——
 * 這些由後端決定，前端連傳的機會都沒有（從源頭防止亂帶）。
 *
 * 欄位上的 @NotNull/@Size 等是 Bean Validation，配合 controller 的 @Valid，
 * 前端傳錯（缺欄位、超長）會自動回 400。
 */
public class FarmTripRequest {

    @NotNull
    private Integer farmerId;

    @NotNull
    private TripType farmTripType;

    @NotBlank
    @Size(max = 30)
    private String farmTripTitle;

    @Size(max = 500)
    private String farmTripIntro;

    @Size(max = 100)
    private String location;

    @NotNull
    @Min(0)
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
