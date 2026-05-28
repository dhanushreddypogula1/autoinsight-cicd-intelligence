package com.devops.autoinsight.entity.enums;

/**
 * Represents the processing lifecycle status of an uploaded log file.
 */
public enum UploadStatus {

    PENDING("Pending", "Log file uploaded but not yet analyzed"),
    PROCESSING("Processing", "Log file is currently being analyzed"),
    PROCESSED("Processed", "Log file has been fully analyzed and incidents generated"),
    FAILED("Failed", "Log file processing encountered an error");

    private final String displayName;
    private final String description;

    UploadStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
