package com.devops.autoinsight.entity.enums;

/**
 * Defines the severity level of a detected CI/CD incident.
 * Rules:
 *  - CRITICAL → deployment failures, crashes, OOM
 *  - HIGH     → repeated exceptions or multiple errors
 *  - MEDIUM   → warnings present, minor exceptions
 *  - LOW      → informational anomalies, single minor issues
 */
public enum SeverityLevel {

    CRITICAL(4, "Critical", "Immediate action required – pipeline is down or crashing"),
    HIGH(3, "High", "Significant failure requiring prompt investigation"),
    MEDIUM(2, "Medium", "Non-blocking issue that should be reviewed"),
    LOW(1, "Low", "Minor issue with minimal impact on pipeline");

    private final int rank;
    private final String displayName;
    private final String description;

    SeverityLevel(int rank, String displayName, String description) {
        this.rank = rank;
        this.displayName = displayName;
        this.description = description;
    }

    public int getRank() {
        return rank;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
