package com.kuanyu.blogdemo123.farmtrip.repository;

import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 體驗活動審核 (FARM_TRIP_AUDITS)
 */
public interface FarmTripAuditRepository extends JpaRepository<FarmTripAudit, Integer> {

    /** 某活動的審核歷史，最新的排前面 */
    List<FarmTripAudit> findByFarmTripIdOrderByCreatedAtDesc(Integer farmTripId);
}
