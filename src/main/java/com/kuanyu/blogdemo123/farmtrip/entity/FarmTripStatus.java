package com.kuanyu.blogdemo123.farmtrip.entity;

/** 體驗活動狀態 (FARM_TRIP.status) */
public enum FarmTripStatus {
    PENDING,  //待審核
    REJECTED, //審核未通過
    ACTIVE,   //上架中
    CLOSED    //已下架
}
