package com.devops.autoinsight.service;

import com.devops.autoinsight.dto.response.IncidentResponse;
import com.devops.autoinsight.entity.enums.SeverityLevel;

import java.util.List;

/**
 * Service contract for incident retrieval operations.
 */
public interface IncidentService {

    List<IncidentResponse> getAllIncidents();

    IncidentResponse getIncidentById(Long id);

    List<IncidentResponse> getIncidentsBySeverity(String severityLevel);
}
