package com.devops.autoinsight.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO returned after a successful log file upload and analysis.
 */
@Data
@Builder
public class LogUploadResponse {

    private Long logId;
    private String originalFileName;
    private String uploadStatus;
    private String pipelineName;
    private String branchName;
    private Long fileSizeBytes;
    private int incidentsDetected;
    private LocalDateTime uploadedAt;
    private LocalDateTime processedAt;
    private String message;
}
