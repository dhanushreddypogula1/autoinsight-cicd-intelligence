package com.devops.autoinsight.service.impl;

import com.devops.autoinsight.dto.request.LogUploadRequest;
import com.devops.autoinsight.dto.response.LogUploadResponse;
import com.devops.autoinsight.entity.Incident;
import com.devops.autoinsight.entity.UploadedLog;
import com.devops.autoinsight.entity.enums.UploadStatus;
import com.devops.autoinsight.exception.InvalidFileException;
import com.devops.autoinsight.exception.LogProcessingException;
import com.devops.autoinsight.repository.IncidentRepository;
import com.devops.autoinsight.repository.UploadedLogRepository;
import com.devops.autoinsight.service.LogService;
import com.devops.autoinsight.util.FileStorageUtil;
import com.devops.autoinsight.util.LogParserUtil;
import com.devops.autoinsight.util.ParsedLogResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Implementation of LogService. Orchestrates the full log processing pipeline:
 * <ol>
 *   <li>Validate and save uploaded file</li>
 *   <li>Persist UploadedLog metadata with PENDING status</li>
 *   <li>Read file content and invoke LogParserUtil</li>
 *   <li>Persist the generated Incident</li>
 *   <li>Update UploadedLog status to PROCESSED</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final UploadedLogRepository uploadedLogRepository;
    private final IncidentRepository    incidentRepository;
    private final FileStorageUtil       fileStorageUtil;
    private final LogParserUtil         logParserUtil;

    @Override
    @Transactional
    public LogUploadResponse uploadAndProcessLog(MultipartFile file, LogUploadRequest request) {
        log.info("Processing log upload: file='{}', size={} bytes",
                file.getOriginalFilename(), file.getSize());

        // ── Step 1: Save file to disk ──────────────────────────────────────────
        String storedFilePath;
        try {
            storedFilePath = fileStorageUtil.saveFile(file);
        } catch (IllegalArgumentException ex) {
            throw new InvalidFileException(ex.getMessage());
        } catch (IOException ex) {
            log.error("Failed to save uploaded file: {}", ex.getMessage(), ex);
            throw new LogProcessingException("Failed to store the uploaded log file. Please try again.", ex);
        }

        // ── Step 2: Persist UploadedLog (PENDING) ────────────────────────────
        UploadedLog uploadedLog = UploadedLog.builder()
                .fileName(buildStoredFileName(file.getOriginalFilename()))
                .originalFileName(file.getOriginalFilename())
                .fileSizeBytes(file.getSize())
                .contentType(file.getContentType())
                .filePath(storedFilePath)
                .uploadStatus(UploadStatus.PROCESSING)
                .pipelineName(request != null ? request.getPipelineName() : null)
                .branchName(request != null ? request.getBranchName() : null)
                .build();

        uploadedLog = uploadedLogRepository.save(uploadedLog);
        log.debug("UploadedLog persisted with ID: {}", uploadedLog.getId());

        // ── Step 3: Read and Parse ────────────────────────────────────────────
        String logContent;
        try {
            logContent = fileStorageUtil.readFileContent(storedFilePath);
        } catch (IOException ex) {
            markLogAsFailed(uploadedLog);
            throw new LogProcessingException("Failed to read the uploaded log file content.", ex);
        }

        ParsedLogResult parseResult;
        try {
            parseResult = logParserUtil.parse(logContent);
        } catch (Exception ex) {
            markLogAsFailed(uploadedLog);
            throw new LogProcessingException("Log parsing encountered an unexpected error: " + ex.getMessage(), ex);
        }

        log.info("Parse complete → category={}, severity={}, errors={}, exceptions={}",
                parseResult.getDetectedCategory(), parseResult.getDetectedSeverity(),
                parseResult.getErrorCount(), parseResult.getExceptionCount());

        // ── Step 4: Persist Incident ──────────────────────────────────────────
        Incident incident = buildIncident(uploadedLog, parseResult);
        incidentRepository.save(incident);
        log.debug("Incident persisted with ID: {}", incident.getId());

        // ── Step 5: Mark log as PROCESSED ─────────────────────────────────────
        uploadedLog.setUploadStatus(UploadStatus.PROCESSED);
        uploadedLog.setProcessedAt(LocalDateTime.now());
        uploadedLogRepository.save(uploadedLog);

        return LogUploadResponse.builder()
                .logId(uploadedLog.getId())
                .originalFileName(uploadedLog.getOriginalFileName())
                .uploadStatus(uploadedLog.getUploadStatus().name())
                .pipelineName(uploadedLog.getPipelineName())
                .branchName(uploadedLog.getBranchName())
                .fileSizeBytes(uploadedLog.getFileSizeBytes())
                .incidentsDetected(1)
                .uploadedAt(uploadedLog.getUploadedAt())
                .processedAt(uploadedLog.getProcessedAt())
                .message("Log file processed successfully. 1 incident generated.")
                .build();
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private Incident buildIncident(UploadedLog log, ParsedLogResult result) {
        String rawErrorLines = result.getErrorLines().stream()
                .limit(10)
                .collect(Collectors.joining("\n"));

        String rawExceptionLines = result.getExceptionLines().stream()
                .limit(10)
                .collect(Collectors.joining("\n"));

        return Incident.builder()
                .uploadedLog(log)
                .title(result.getIncidentTitle())
                .failureCategory(result.getDetectedCategory())
                .severityLevel(result.getDetectedSeverity())
                .summary(result.getIncidentSummary())
                .probableRootCause(result.getProbableRootCause())
                .suggestedFix(result.getSuggestedFix())
                .errorCount(result.getErrorCount())
                .warningCount(result.getWarningCount())
                .exceptionCount(result.getExceptionCount())
                .stackTraceCount(result.getStackTraceCount())
                .rawErrorLines(rawErrorLines)
                .rawExceptionLines(rawExceptionLines)
                .pipelineStage(result.getPipelineStage())
                .build();
    }

    private void markLogAsFailed(UploadedLog uploadedLog) {
        uploadedLog.setUploadStatus(UploadStatus.FAILED);
        uploadedLogRepository.save(uploadedLog);
    }

    private String buildStoredFileName(String originalFileName) {
        if (originalFileName == null) return "log.txt";
        return originalFileName;
    }
}
