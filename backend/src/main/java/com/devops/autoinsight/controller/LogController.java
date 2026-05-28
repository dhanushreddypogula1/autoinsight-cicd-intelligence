package com.devops.autoinsight.controller;

import com.devops.autoinsight.dto.request.LogUploadRequest;
import com.devops.autoinsight.dto.response.ApiResponse;
import com.devops.autoinsight.dto.response.LogUploadResponse;
import com.devops.autoinsight.service.LogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for log file upload operations.
 *
 * <p>Base path: {@code /api/logs}
 */
@Slf4j
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LogController {

    private final LogService logService;

    /**
     * POST /api/logs/upload
     *
     * <p>Accepts a .txt CI/CD log file via multipart form upload.
     * Optionally accepts metadata (pipeline name, branch) as form parameters.
     * Triggers full parsing and incident generation synchronously.
     *
     * @param file         the log file (.txt, max 10MB)
     * @param pipelineName optional name of the CI/CD pipeline (e.g., "backend-ci")
     * @param branchName   optional git branch name (e.g., "feature/auth")
     * @return 201 Created with upload confirmation and incident summary
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<LogUploadResponse>> uploadLog(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "pipelineName", required = false) String pipelineName,
            @RequestParam(value = "branchName",   required = false) String branchName) {

        log.info("Received log upload request: file='{}', pipeline='{}', branch='{}'",
                file.getOriginalFilename(), pipelineName, branchName);

        LogUploadRequest request = new LogUploadRequest();
        request.setPipelineName(pipelineName);
        request.setBranchName(branchName);

        LogUploadResponse response = logService.uploadAndProcessLog(file, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Log file uploaded and analyzed successfully"));
    }
}
