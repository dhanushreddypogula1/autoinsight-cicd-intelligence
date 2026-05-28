package com.devops.autoinsight.entity.enums;

/**
 * Categorizes the type of CI/CD pipeline failure detected in a log file.
 */
public enum FailureCategory {

    BUILD_FAILURE("Build Failure", "Failure during source code compilation or build process"),
    TEST_FAILURE("Test Failure", "Failure during unit, integration, or end-to-end test execution"),
    DEPENDENCY_FAILURE("Dependency Failure", "Failure in resolving or downloading project dependencies"),
    DEPLOYMENT_FAILURE("Deployment Failure", "Failure during application deployment or container orchestration"),
    UNKNOWN_FAILURE("Unknown Failure", "Failure that does not match any known pattern");

    private final String displayName;
    private final String description;

    FailureCategory(String displayName, String description) {
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
