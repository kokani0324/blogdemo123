package com.kuanyu.blogdemo123.farmtrip.repository;

import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripAudit;
import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripAuditStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 體驗活動審核 (FARM_TRIP_AUDITS)
 */
public interface FarmTripAuditRepository extends JpaRepository<FarmTripAudit, Integer> {

    /** 某活動的審核歷史，最新的排前面 */
    List<FarmTripAudit> findByFarmTripIdOrderByCreatedAtDesc(Integer farmTripId);

    /** 找出該活動最新一筆待審核紀錄，供管理員寫入審核結果 */
    Optional<FarmTripAudit> findFirstByFarmTripIdAndStatusOrderByCreatedAtDesc(
            Integer farmTripId, FarmTripAuditStatus status);
}
