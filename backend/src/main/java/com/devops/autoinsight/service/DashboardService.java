package com.devops.autoinsight.service;

import com.devops.autoinsight.dto.response.DashboardStatsResponse;

/**
 * Service contract for dashboard aggregated statistics.
 */
public interface DashboardService {

    DashboardStatsResponse getDashboardStats();
}
