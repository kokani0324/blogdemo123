package com.kuanyu.blogdemo123.farmtrip.repository;

import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 體驗活動評論 (FARM_TRIP_COMMENT)
 */
public interface FarmTripCommentRepository extends JpaRepository<FarmTripComment, Integer> {

    /** 某活動的所有評論，最新的排前面 */
    List<FarmTripComment> findByFarmTripIdOrderByCreatedAtDesc(Integer farmTripId);
}
