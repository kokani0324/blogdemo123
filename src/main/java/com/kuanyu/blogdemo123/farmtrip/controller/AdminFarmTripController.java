package com.kuanyu.blogdemo123.farmtrip.controller;

import com.kuanyu.blogdemo123.farmtrip.dto.FarmTripAuditRequest;
import com.kuanyu.blogdemo123.farmtrip.dto.FarmTripAuditResponse;
import com.kuanyu.blogdemo123.farmtrip.dto.FarmTripResponse;
import com.kuanyu.blogdemo123.farmtrip.service.FarmTripService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 體驗活動 - 管理員 (審核相關)
 * base path: /admin/farm-trips
 *
 * controller 只負責轉發，轉換與商業邏輯都在 service。
 * 註：adminId 目前暫由 DTO 帶入，之後改從登入身分取得。
 */
@RestController
@RequestMapping("/admin/farm-trips")
public class AdminFarmTripController {

    @Autowired
    private FarmTripService farmTripService;

    /** 查看審核清單 (待審核的體驗活動) */
    @GetMapping("/audits/pending")
    public ResponseEntity<List<FarmTripResponse>> getPendingAuditList() {
        return ResponseEntity.ok(farmTripService.getPendingTrips());
    }

    /** 審核單個活動 (通過 / 退回，body 帶 status、reason、adminId) */
    @PostMapping("/{tripId}/audit")
    public ResponseEntity<Void> auditTrip(@PathVariable Integer tripId,
                                          @Valid @RequestBody FarmTripAuditRequest request) {
        farmTripService.auditTrip(tripId, request);
        return ResponseEntity.ok().build();
    }

    /** 審核歷史 */
    @GetMapping("/{tripId}/audits")
    public ResponseEntity<List<FarmTripAuditResponse>> getAuditHistory(@PathVariable Integer tripId) {
        return ResponseEntity.ok(farmTripService.getAuditHistory(tripId));
    }
}
