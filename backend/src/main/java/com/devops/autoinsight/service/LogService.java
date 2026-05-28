package com.devops.autoinsight.service;

import com.devops.autoinsight.dto.request.LogUploadRequest;
import com.devops.autoinsight.dto.response.LogUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service contract for CI/CD log file upload and processing operations.
 */
public interface LogService {

    /**
     * Accepts an uploaded log file, stores it, triggers parsing, and creates an incident.
     *
     * @param file    the uploaded .txt log file
     * @param request optional metadata (pipeline name, branch)
     * @return upload confirmation with incident count and status
     */
    LogUploadResponse uploadAndProcessLog(MultipartFile file, LogUploadRequest request);
}
