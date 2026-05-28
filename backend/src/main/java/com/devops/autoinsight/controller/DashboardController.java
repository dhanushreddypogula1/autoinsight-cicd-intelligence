package com.devops.autoinsight.controller;

import com.devops.autoinsight.dto.response.ApiResponse;
import com.devops.autoinsight.dto.response.DashboardStatsResponse;
import com.devops.autoinsight.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the AutoInsight analytics dashboard.
 *
 * <p>Base path: {@code /api/dashboard}
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * GET /api/dashboard/stats
     *
     * <p>Returns aggregated statistics across all logs and incidents.
     * Powers the frontend metrics tiles, pie charts, and severity indicators.
     *
     * @return dashboard statistics payload
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        log.debug("GET /api/dashboard/stats - aggregating dashboard metrics");
        DashboardStatsResponse stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Dashboard statistics retrieved"));
    }
}
