package com.kuanyu.blogdemo123.farmtrip.dto;

/**
 * 預約場次的「請求」DTO（會員送進來）。
 *
 * 只放前端該給的：預約人數、聯絡資訊。
 * 刻意不放 status / bookedAt / bookingNo / farmSessionId ——
 * 場次 id 由網址帶、狀態與時間由後端決定。
 *
 * 註：userId 目前暫放，之後有登入機制後改從登入身分取得。
 */
public class FarmTripOrderRequest {

    private Integer userId;
    private Integer numPeople;
    private String userName;
    private String userPhoneNum;
    private String note;

    public FarmTripOrderRequest() {
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getNumPeople() {
        return numPeople;
    }

    public void setNumPeople(Integer numPeople) {
        this.numPeople = numPeople;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhoneNum() {
        return userPhoneNum;
    }

    public void setUserPhoneNum(String userPhoneNum) {
        this.userPhoneNum = userPhoneNum;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
