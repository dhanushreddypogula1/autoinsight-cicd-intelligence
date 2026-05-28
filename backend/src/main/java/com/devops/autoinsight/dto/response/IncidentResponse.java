package com.devops.autoinsight.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for a single incident record — used in list and detail views.
 */
@Data
@Builder
public class IncidentResponse {

    private Long id;
    private Long logId;
    private String logFileName;

    private String title;
    private String failureCategory;
    private String failureCategoryDisplay;
    private String severityLevel;
    private String severityLevelDisplay;

    private String summary;
    private String probableRootCause;
    private String suggestedFix;

    private Integer errorCount;
    private Integer warningCount;
    private Integer exceptionCount;
    private Integer stackTraceCount;

    private String rawErrorLines;
    private String rawExceptionLines;

    private String pipelineStage;
    private String pipelineName;
    private String branchName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
