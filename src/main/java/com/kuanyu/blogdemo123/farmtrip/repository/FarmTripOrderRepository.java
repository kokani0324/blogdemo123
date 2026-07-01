package com.kuanyu.blogdemo123.farmtrip.repository;

import com.kuanyu.blogdemo123.farmtrip.entity.FarmTripOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 體驗活動預約訂單 (FARM_TRIP_ORDER)
 */
public interface FarmTripOrderRepository extends JpaRepository<FarmTripOrder, Integer> {

    /** 會員查自己的所有預約 (derived query) */
    List<FarmTripOrder> findByUserIdOrderByBookedAtDesc(Integer userId);

    /** 查某場次的所有訂單 (derived query) */
    List<FarmTripOrder> findByFarmSessionId(Integer farmSessionId);

    /**
     * B 方案：小農查訂單。
     * 訂單只記到 farm_session_id，沒有 farmer_id，
     * 所以靠 訂單→場次→活動 串回 farmer_id，一條 JPQL 查完。
     *
     * 註：entity 用的是原始 Integer 外鍵 (非 JPA 物件關聯)，
     * 所以這裡用「多表 FROM + WHERE 對齊外鍵」的寫法 (theta join)，
     * 而不是 o.session.trip 這種關聯導覽。
     */
    @Query("SELECT o FROM FarmTripOrder o, FarmTripSession s, FarmTrip t " +
           "WHERE o.farmSessionId = s.farmSessionId " +
           "AND s.farmTripId = t.farmTripId " +
           "AND t.farmerId = :farmerId " +
           "ORDER BY o.bookedAt DESC")
    List<FarmTripOrder> findOrdersByFarmerId(@Param("farmerId") Integer farmerId);
}
