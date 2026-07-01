package com.kuanyu.blogdemo123.farmtrip.repository;

import com.kuanyu.blogdemo123.farmtrip.entity.FarmTrip;
import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 體驗活動 (FARM_TRIP)
 * 繼承 JpaRepository 後，CRUD (findAll / findById / save / deleteById) 免費取得。
 */
public interface FarmTripRepository extends JpaRepository<FarmTrip, Integer> {

    /** 依狀態查：公開瀏覽用 ACTIVE、管理員審核清單用 PENDING */
    List<FarmTrip> findByStatus(FarmTripStatus status); //依照status

    /** 小農查自己的所有體驗活動 */
    List<FarmTrip> findByFarmerId(Integer farmerId);
}
