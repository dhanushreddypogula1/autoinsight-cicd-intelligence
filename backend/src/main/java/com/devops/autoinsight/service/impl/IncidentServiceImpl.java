package com.devops.autoinsight.service.impl;

import com.devops.autoinsight.dto.response.IncidentResponse;
import com.devops.autoinsight.entity.Incident;
import com.devops.autoinsight.entity.enums.SeverityLevel;
import com.devops.autoinsight.exception.ResourceNotFoundException;
import com.devops.autoinsight.repository.IncidentRepository;
import com.devops.autoinsight.service.IncidentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of IncidentService. Handles retrieval and mapping of Incident entities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;

    @Override
    public List<IncidentResponse> getAllIncidents() {
        log.debug("Fetching all incidents ordered by creation date desc");
        return incidentRepository.findAllWithLogOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public IncidentResponse getIncidentById(Long id) {
        log.debug("Fetching incident by ID: {}", id);
        Incident incident = incidentRepository.findByIdWithLog(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", id));
        return toResponse(incident);
    }

    @Override
    public List<IncidentResponse> getIncidentsBySeverity(String severityLevel) {
        SeverityLevel level = parseSeverityLevel(severityLevel);
        log.debug("Fetching incidents by severity: {}", level);
        return incidentRepository.findBySeverityLevelOrderByCreatedAtDesc(level)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteIncident(Long id) {
        log.info("Deleting incident with ID:{}",id);
        
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", id));    
        incidentRepository.delete(incident);
        
        log.info("Incident with ID:{} deleted successfully",id);
    }
    // ── Mapping ───────────────────────────────────────────────────────────────

    private IncidentResponse toResponse(Incident incident) {
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
                .probableRootCause(incident.getProbableRootCause())
                .suggestedFix(incident.getSuggestedFix())
                .errorCount(incident.getErrorCount())
                .warningCount(incident.getWarningCount())
                .exceptionCount(incident.getExceptionCount())
                .stackTraceCount(incident.getStackTraceCount())
                .rawErrorLines(incident.getRawErrorLines())
                .rawExceptionLines(incident.getRawExceptionLines())
                .pipelineStage(incident.getPipelineStage())
                .pipelineName(incident.getUploadedLog().getPipelineName())
                .branchName(incident.getUploadedLog().getBranchName())
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .build();
    }

    private SeverityLevel parseSeverityLevel(String severityLevel) {
        try {
            return SeverityLevel.valueOf(severityLevel.toUpperCase());
        } catch (IllegalArgumentException ex) {
            String validValues = Arrays.stream(SeverityLevel.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException(
                    "Invalid severity level: '" + severityLevel + "'. Valid values are: " + validValues
            );
        }
    }
}
