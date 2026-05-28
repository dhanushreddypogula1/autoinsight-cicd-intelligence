package com.devops.autoinsight.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Utility component for file storage operations.
 * Handles saving uploaded log files to the local filesystem and reading their content.
 */
@Slf4j
@Component
public class FileStorageUtil {

    private static final DateTimeFormatter DIR_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024L; // 10 MB

    @Value("${autoinsight.upload.dir:uploads/logs}")
    private String uploadBaseDir;

    /**
     * Saves the uploaded multipart file to the configured storage directory.
     *
     * @param file the uploaded file
     * @return the relative path where the file was stored
     * @throws IOException if the file cannot be saved
     */
    public String saveFile(MultipartFile file) throws IOException {
        validateFile(file);

        // Build date-partitioned sub-directory structure: uploads/logs/2025/01/15/
        String dateSubDir = LocalDateTime.now().format(DIR_FORMATTER);
        Path targetDir = Paths.get(uploadBaseDir, dateSubDir).toAbsolutePath().normalize();
        Files.createDirectories(targetDir);

        String storedFileName = buildStoredFileName(file.getOriginalFilename());
        Path targetPath = targetDir.resolve(storedFileName);

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Log file saved to: {}", targetPath);

        // Return relative path for storage in DB
        return Paths.get(uploadBaseDir, dateSubDir, storedFileName).toString();
    }

    /**
     * Reads the content of a previously stored log file.
     *
     * @param filePath relative path from the DB
     * @return file content as a UTF-8 string
     * @throws IOException if the file cannot be read
     */
    public String readFileContent(String filePath) throws IOException {
        Path path = Paths.get(filePath).toAbsolutePath().normalize();
        if (!Files.exists(path)) {
            throw new IOException("Log file not found at path: " + filePath);
        }
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    /**
     * Deletes a stored log file from the filesystem.
     *
     * @param filePath relative path from the DB
     */
    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath).toAbsolutePath().normalize();
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.info("Deleted log file: {}", filePath);
            } else {
                log.warn("Attempted to delete non-existent file: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
        }
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Upload file must not be empty");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".txt")) {
            throw new IllegalArgumentException("Only .txt log files are supported. Received: " + originalName);
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "File size exceeds the 10MB limit. File size: " + file.getSize() + " bytes");
        }
        // Prevent path traversal attacks
        if (originalName.contains("..") || originalName.contains("/") || originalName.contains("\\")) {
            throw new IllegalArgumentException("File name contains invalid characters: " + originalName);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String buildStoredFileName(String originalFileName) {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String baseName = sanitizeFileName(originalFileName);
        return uuid + "_" + baseName;
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null) return "log.txt";
        return fileName.replaceAll("[^a-zA-Z0-9._\\-]", "_");
    }
}
