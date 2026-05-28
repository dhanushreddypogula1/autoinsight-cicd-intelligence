package com.devops.autoinsight.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Response DTO for the dashboard statistics endpoint.
 * Provides aggregated metrics for the frontend dashboard.
 */
@Data
@Builder
public class DashboardStatsResponse {

    // Log upload stats
    private long totalLogsUploaded;
    private long totalLogsProcessed;
    private long totalLogsPending;

    // Incident stats
    private long totalIncidents;
    private long criticalIncidents;
    private long highIncidents;
    private long mediumIncidents;
    private long lowIncidents;

    // Category breakdown
    private Map<String, Long> incidentsByCategory;

    // Severity breakdown
    private Map<String, Long> incidentsBySeverity;

    // Top failure indicators
    private long totalErrorsDetected;
    private long totalWarningsDetected;
    private long totalExceptionsDetected;

    // Most recent incident summary
    private IncidentResponse mostRecentIncident;
}
