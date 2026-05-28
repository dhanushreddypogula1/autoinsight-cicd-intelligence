package com.devops.autoinsight.util;

import com.devops.autoinsight.entity.enums.FailureCategory;
import com.devops.autoinsight.entity.enums.SeverityLevel;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Immutable result object produced by LogParserUtil after processing a raw log file.
 * Contains all extracted artifacts needed to generate an Incident.
 */
@Data
@Builder
public class ParsedLogResult {

    private List<String> errorLines;
    private List<String> warnLines;
    private List<String> exceptionLines;
    private List<String> stackTraceLines;

    private int errorCount;
    private int warningCount;
    private int exceptionCount;
    private int stackTraceCount;

    private FailureCategory detectedCategory;
    private SeverityLevel detectedSeverity;

    private String incidentTitle;
    private String incidentSummary;
    private String probableRootCause;
    private String suggestedFix;
    private String pipelineStage;

    private int totalLines;
}
