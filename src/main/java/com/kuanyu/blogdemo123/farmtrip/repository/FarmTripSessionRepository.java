package com.kuanyu.blogdemo123.farmtrip.repository;

import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 體驗活動場次 (FARM_TRIP_SESSION)
 */
public interface FarmTripSessionRepository extends JpaRepository<FarmTripSession, Integer> {

    /** 查某活動底下的所有場次 */
    List<FarmTripSession> findByFarmTripId(Integer farmTripId);
}
