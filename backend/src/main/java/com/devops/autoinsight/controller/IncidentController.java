package com.devops.autoinsight.controller;

import com.devops.autoinsight.dto.response.ApiResponse;
import com.devops.autoinsight.dto.response.IncidentResponse;
import com.devops.autoinsight.service.IncidentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for incident retrieval and filtering.
 *
 * <p>Base path: {@code /api/incidents}
 */
@Slf4j
@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class IncidentController {

    private final IncidentService incidentService;

    /**
     * GET /api/incidents
     *
     * <p>Returns all incidents ordered by creation date descending.
     *
     * @return list of all incident summaries
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<IncidentResponse>>> getAllIncidents() {
        log.debug("GET /api/incidents - fetching all incidents");
        List<IncidentResponse> incidents = incidentService.getAllIncidents();
        return ResponseEntity.ok(
                ApiResponse.success(incidents, "Retrieved " + incidents.size() + " incidents")
        );
    }

    /**
     * GET /api/incidents/{id}
     *
     * <p>Returns the full detail of a single incident including raw log excerpts,
     * root cause analysis, and fix suggestions.
     *
     * @param id the incident ID
     * @return the incident detail
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IncidentResponse>> getIncidentById(@PathVariable Long id) {
        log.debug("GET /api/incidents/{} - fetching incident by ID", id);
        IncidentResponse incident = incidentService.getIncidentById(id);
        return ResponseEntity.ok(ApiResponse.success(incident));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteIncident(@PathVariable Long id) {
        log.info("DELETE /api/incidents/{} - deleting incident", id);


        incidentService.deleteIncident(id);

        return ResponseEntity.ok(
            ApiResponse.success("Incident deleted successfully")
     );


    }


    /**
     * GET /api/incidents/severity/{level}
     *
     * <p>Filters incidents by severity level.
     * Valid values: {@code CRITICAL}, {@code HIGH}, {@code MEDIUM}, {@code LOW} (case-insensitive)
     *
     * @param level the severity level to filter by
     * @return filtered list of incidents
     */
    @GetMapping("/severity/{level}")
    public ResponseEntity<ApiResponse<List<IncidentResponse>>> getIncidentsBySeverity(
            @PathVariable String level) {
        log.debug("GET /api/incidents/severity/{} - fetching incidents by severity", level);
        List<IncidentResponse> incidents = incidentService.getIncidentsBySeverity(level);
        return ResponseEntity.ok(
                ApiResponse.success(incidents,
                        "Retrieved " + incidents.size() + " incidents with severity: " + level.toUpperCase())
        );
    }
}
