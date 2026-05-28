package com.devops.autoinsight.service.impl;

import com.devops.autoinsight.dto.response.DashboardStatsResponse;
import com.devops.autoinsight.dto.response.IncidentResponse;
import com.devops.autoinsight.entity.Incident;
import com.devops.autoinsight.entity.enums.FailureCategory;
import com.devops.autoinsight.entity.enums.SeverityLevel;
import com.devops.autoinsight.entity.enums.UploadStatus;
import com.devops.autoinsight.repository.IncidentRepository;
import com.devops.autoinsight.repository.UploadedLogRepository;
import com.devops.autoinsight.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of DashboardService.
 * Aggregates counts and statistics across all incidents and logs for dashboard display.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final IncidentRepository    incidentRepository;
    private final UploadedLogRepository uploadedLogRepository;

    @Override
    public DashboardStatsResponse getDashboardStats() {
        log.debug("Aggregating dashboard statistics");

        // Log stats
        long totalLogs     = uploadedLogRepository.count();
        long processedLogs = uploadedLogRepository.countByUploadStatus(UploadStatus.PROCESSED);
        long pendingLogs   = uploadedLogRepository.countByUploadStatus(UploadStatus.PENDING)
                           + uploadedLogRepository.countByUploadStatus(UploadStatus.PROCESSING);

        // Incident severity counts
        long totalIncidents    = incidentRepository.count();
        long criticalIncidents = incidentRepository.countBySeverityLevel(SeverityLevel.CRITICAL);
        long highIncidents     = incidentRepository.countBySeverityLevel(SeverityLevel.HIGH);
        long mediumIncidents   = incidentRepository.countBySeverityLevel(SeverityLevel.MEDIUM);
        long lowIncidents      = incidentRepository.countBySeverityLevel(SeverityLevel.LOW);

        // Category breakdown
        Map<String, Long> byCategory = buildCategoryMap();

        // Severity breakdown
        Map<String, Long> bySeverity = buildSeverityMap(
                criticalIncidents, highIncidents, mediumIncidents, lowIncidents
        );

        // Totals from raw parsing counts
        Long totalErrors     = Optional.ofNullable(incidentRepository.sumAllErrorCounts()).orElse(0L);
        Long totalWarnings   = Optional.ofNullable(incidentRepository.sumAllWarningCounts()).orElse(0L);
        Long totalExceptions = Optional.ofNullable(incidentRepository.sumAllExceptionCounts()).orElse(0L);

        // Most recent incident
        IncidentResponse mostRecent = incidentRepository.findMostRecentIncident()
                .map(this::toBasicResponse)
                .orElse(null);

        return DashboardStatsResponse.builder()
                .totalLogsUploaded(totalLogs)
                .totalLogsProcessed(processedLogs)
                .totalLogsPending(pendingLogs)
                .totalIncidents(totalIncidents)
                .criticalIncidents(criticalIncidents)
                .highIncidents(highIncidents)
                .mediumIncidents(mediumIncidents)
                .lowIncidents(lowIncidents)
                .incidentsByCategory(byCategory)
                .incidentsBySeverity(bySeverity)
                .totalErrorsDetected(totalErrors)
                .totalWarningsDetected(totalWarnings)
                .totalExceptionsDetected(totalExceptions)
                .mostRecentIncident(mostRecent)
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Map<String, Long> buildCategoryMap() {
        Map<String, Long> map = new LinkedHashMap<>();
        for (FailureCategory cat : FailureCategory.values()) {
            map.put(cat.getDisplayName(), incidentRepository.countByFailureCategory(cat));
        }
        return map;
    }

    private Map<String, Long> buildSeverityMap(long critical, long high, long medium, long low) {
        Map<String, Long> map = new LinkedHashMap<>();
        map.put(SeverityLevel.CRITICAL.getDisplayName(), critical);
        map.put(SeverityLevel.HIGH.getDisplayName(), high);
        map.put(SeverityLevel.MEDIUM.getDisplayName(), medium);
        map.put(SeverityLevel.LOW.getDisplayName(), low);
        return map;
    }

    private IncidentResponse toBasicResponse(Incident incident) {
        return IncidentResponse.builder()
                .id(incident.getId())
                .logId(incident.getUploadedLog().getId())
                .logFileName(incident.getUploadedLog().getOriginalFileName())
                .title(incident.getTitle())
                .failureCategory(incident.getFailureCategory().name())
                .failureCategoryDisplay(incident.getFailureCategory().getDisplayName())
                .severityLevel(incident.getSeverityLevel().name())
                .severityLevelDisplay(incident.getSeverityLevel().getDisplayName())
                .summary(incident.getSummary())
                .pipelineName(incident.getUploadedLog().getPipelineName())
                .branchName(incident.getUploadedLog().getBranchName())
                .createdAt(incident.getCreatedAt())
                .build();
    }
}
