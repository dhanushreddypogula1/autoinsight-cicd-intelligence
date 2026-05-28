package com.devops.autoinsight.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for CI/CD log file upload endpoint.
 * File itself is handled via MultipartFile; this carries optional metadata.
 */
@Data
public class LogUploadRequest {

    @Size(max = 255, message = "Pipeline name must not exceed 255 characters")
    private String pipelineName;

    @Size(max = 255, message = "Branch name must not exceed 255 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9/_\\-\\.]*$",
        message = "Branch name contains invalid characters"
    )
    private String branchName;
}
