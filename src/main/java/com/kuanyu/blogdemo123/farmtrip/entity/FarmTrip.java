package com.kuanyu.blogdemo123.farmtrip.entity;

import jakarta.persistence.*;

/**
 * 體驗活動 (FARM_TRIP)
 */
@Entity
@Table(name = "farm_trip")
public class FarmTrip implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "farm_trip_id", updatable = false)
    private Integer farmTripId;

    @Column(name = "farmer_id", nullable = false)
    private Integer farmerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "farm_trip_type")
    private TripType farmTripType;

    @Column(name = "farm_trip_title", length = 30)
    private String farmTripTitle;

    /** farm_trip_pic LONGBLOB */
    @Lob
    @Column(name = "farm_trip_pic")
    private byte[] farmTripPic;

    @Column(name = "farm_trip_intro", length = 500)
    private String farmTripIntro;

    @Column(name = "location", length = 100)
    private String location;

    /** 參考價 (每人) */
    @Column(name = "refer_price")
    private Integer referPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private FarmTripStatus status;

    /** 評論則數 */
    @Column(name = "comment_numbers")
    private Integer commentNumbers;

    /** 星星總數 (平均 = star_numbers / comment_numbers) */
    @Column(name = "star_numbers")
    private Integer starNumbers;

    public FarmTrip() {
        super();
    }

    public Integer getFarmTripId() {
        return farmTripId;
    }

    public void setFarmTripId(Integer farmTripId) {
        this.farmTripId = farmTripId;
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

    public byte[] getFarmTripPic() {
        return farmTripPic;
    }

    public void setFarmTripPic(byte[] farmTripPic) {
        this.farmTripPic = farmTripPic;
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

    public FarmTripStatus getStatus() {
        return status;
    }

    public void setStatus(FarmTripStatus status) {
        this.status = status;
    }

    public Integer getCommentNumbers() {
        return commentNumbers;
    }

    public void setCommentNumbers(Integer commentNumbers) {
        this.commentNumbers = commentNumbers;
    }

    public Integer getStarNumbers() {
        return starNumbers;
    }

    public void setStarNumbers(Integer starNumbers) {
        this.starNumbers = starNumbers;
    }
}
