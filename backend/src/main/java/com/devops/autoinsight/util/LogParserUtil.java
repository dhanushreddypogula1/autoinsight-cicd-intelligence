package com.devops.autoinsight.util;

import com.devops.autoinsight.entity.enums.FailureCategory;
import com.devops.autoinsight.entity.enums.SeverityLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Core log parsing utility for the AutoInsight platform.
 *
 * <p>Implements multi-phase log analysis:
 * <ol>
 *   <li>Line classification (ERROR, WARN, Exception, Stack Trace)</li>
 *   <li>Failure category detection via regex pattern matching</li>
 *   <li>Severity scoring based on counts and category</li>
 *   <li>Root cause and fix suggestion generation</li>
 * </ol>
 */
@Slf4j
@Component
public class LogParserUtil {

    // ── Regex Patterns ──────────────────────────────────────────────────────

    private static final Pattern ERROR_LINE_PATTERN =
            Pattern.compile("(?i)^.*\\[?(ERROR|SEVERE|FATAL)\\]?.*$");

    private static final Pattern WARN_LINE_PATTERN =
            Pattern.compile("(?i)^.*\\[?(WARN|WARNING)\\]?.*$");

    private static final Pattern EXCEPTION_PATTERN =
            Pattern.compile("(?i)(\\w+Exception|\\w+Error):\\s*.+");

    private static final Pattern STACK_TRACE_PATTERN =
            Pattern.compile("^\\s+at\\s+[\\w.$]+\\([\\w.]+:\\d+\\)$");

    private static final Pattern BUILD_FAILURE_PATTERN =
            Pattern.compile("(?i)(BUILD FAILURE|BUILD FAILED|compilation failed|cannot find symbol|package .* does not exist)");

    private static final Pattern TEST_FAILURE_PATTERN =
            Pattern.compile("(?i)(Tests run:.*Failures:[^0]|Tests run:.*Errors:[^0]|TEST FAILED|FAILED.*test|testcase.*FAIL)");

    private static final Pattern DEPENDENCY_FAILURE_PATTERN =
            Pattern.compile("(?i)(Could not resolve|Artifact.*not found|dependency.*missing|Unresolved dependency|Could not transfer|connection.*timed out|Failed to download)");

    private static final Pattern DEPLOYMENT_FAILURE_PATTERN =
            Pattern.compile("(?i)(deploy.*fail|deployment.*error|container.*exit|OOMKilled|CrashLoopBackOff|kubectl.*error|Connection refused|Address already in use|docker.*error|pod.*failed)");

    private static final Pattern PIPELINE_STAGE_PATTERN =
            Pattern.compile("(?i)\\[?(stage|step|phase|job)\\]?\\s*[:\\-]?\\s*([\\w\\s\\-_]+)");

    private static final Pattern NULL_POINTER_PATTERN =
            Pattern.compile("(?i)(NullPointerException)");

    private static final Pattern OOM_PATTERN =
            Pattern.compile("(?i)(OutOfMemoryError|OutOfMemory)");

    private static final Pattern MAVEN_ERROR_PATTERN =
            Pattern.compile("(?i)(\\[ERROR\\]|\\[FATAL\\])");

    private static final int MAX_LINES_IN_REPORT = 20;

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Parses the full text content of a log file and returns a structured {@link ParsedLogResult}.
     *
     * @param logContent raw text content of the uploaded log file
     * @return fully populated ParsedLogResult
     */
    public ParsedLogResult parse(String logContent) {
        if (logContent == null || logContent.isBlank()) {
            log.warn("Received empty or null log content for parsing");
            return buildEmptyResult();
        }

        List<String> lines = Arrays.asList(logContent.split("\\r?\\n"));
        log.debug("Starting log parse. Total lines: {}", lines.size());

        // Phase 1: Classify lines
        List<String> errorLines      = extractMatchingLines(lines, ERROR_LINE_PATTERN);
        List<String> warnLines       = extractMatchingLines(lines, WARN_LINE_PATTERN);
        List<String> exceptionLines  = extractMatchingLines(lines, EXCEPTION_PATTERN);
        List<String> stackTraceLines = extractMatchingLines(lines, STACK_TRACE_PATTERN);

        // Deduplicate — error lines may overlap with exception lines
        exceptionLines = deduplicateAgainst(exceptionLines, errorLines);

        log.debug("Classified lines → errors={}, warns={}, exceptions={}, stackTraces={}",
                errorLines.size(), warnLines.size(), exceptionLines.size(), stackTraceLines.size());

        // Phase 2: Detect failure category
        FailureCategory category = detectCategory(logContent, errorLines, exceptionLines);

        // Phase 3: Determine severity
        SeverityLevel severity = computeSeverity(
                category, errorLines.size(), warnLines.size(),
                exceptionLines.size(), stackTraceLines.size(), logContent
        );

        // Phase 4: Identify pipeline stage
        String pipelineStage = detectPipelineStage(lines);

        // Phase 5: Generate human-readable analysis
        String title          = generateTitle(category, errorLines, exceptionLines);
        String summary        = generateSummary(category, errorLines, warnLines, exceptionLines, stackTraceLines, lines.size());
        String rootCause      = generateRootCause(category, errorLines, exceptionLines, logContent);
        String suggestedFix   = generateSuggestedFix(category, logContent);

        return ParsedLogResult.builder()
                .errorLines(limitLines(errorLines))
                .warnLines(limitLines(warnLines))
                .exceptionLines(limitLines(exceptionLines))
                .stackTraceLines(limitLines(stackTraceLines))
                .errorCount(errorLines.size())
                .warningCount(warnLines.size())
                .exceptionCount(exceptionLines.size())
                .stackTraceCount(stackTraceLines.size())
                .detectedCategory(category)
                .detectedSeverity(severity)
                .incidentTitle(title)
                .incidentSummary(summary)
                .probableRootCause(rootCause)
                .suggestedFix(suggestedFix)
                .pipelineStage(pipelineStage)
                .totalLines(lines.size())
                .build();
    }

    // ── Phase 1: Line Classification ─────────────────────────────────────────

    private List<String> extractMatchingLines(List<String> lines, Pattern pattern) {
        return lines.stream()
                .filter(line -> pattern.matcher(line).find())
                .collect(Collectors.toList());
    }

    private List<String> deduplicateAgainst(List<String> source, List<String> reference) {
        Set<String> referenceSet = new HashSet<>(reference);
        return source.stream()
                .filter(line -> !referenceSet.contains(line))
                .collect(Collectors.toList());
    }

    // ── Phase 2: Failure Category Detection ──────────────────────────────────

    /**
     * Determines the most specific failure category by matching regex patterns against
     * the full log content and error lines. Uses priority ordering:
     * Deployment > Dependency > Test > Build > Unknown
     */
    private FailureCategory detectCategory(String content, List<String> errorLines, List<String> exceptionLines) {
        String combinedContent = content + " " + String.join(" ", errorLines);

        if (DEPLOYMENT_FAILURE_PATTERN.matcher(combinedContent).find()) {
            return FailureCategory.DEPLOYMENT_FAILURE;
        }
        if (DEPENDENCY_FAILURE_PATTERN.matcher(combinedContent).find()) {
            return FailureCategory.DEPENDENCY_FAILURE;
        }
        if (TEST_FAILURE_PATTERN.matcher(combinedContent).find()) {
            return FailureCategory.TEST_FAILURE;
        }
        if (BUILD_FAILURE_PATTERN.matcher(combinedContent).find()) {
            return FailureCategory.BUILD_FAILURE;
        }
        if (!exceptionLines.isEmpty() || !errorLines.isEmpty()) {
            return FailureCategory.UNKNOWN_FAILURE;
        }
        return FailureCategory.UNKNOWN_FAILURE;
    }

    // ── Phase 3: Severity Computation ────────────────────────────────────────

    /**
     * Scores severity based on category, counts, and presence of critical patterns.
     *
     * <pre>
     * CRITICAL → Deployment failures, OOM errors, crashes
     * HIGH     → 5+ errors or 2+ exceptions, or build failures with exceptions
     * MEDIUM   → Warnings present, few errors, test failures
     * LOW      → Minimal issues detected
     * </pre>
     */
    private SeverityLevel computeSeverity(FailureCategory category, int errorCount,
                                          int warnCount, int exceptionCount,
                                          int stackTraceCount, String content) {

        // Deployment failures or OOM are always CRITICAL
        if (category == FailureCategory.DEPLOYMENT_FAILURE) {
            return SeverityLevel.CRITICAL;
        }
        if (OOM_PATTERN.matcher(content).find()) {
            return SeverityLevel.CRITICAL;
        }

        // High error/exception volume → HIGH
        if (errorCount >= 5 || exceptionCount >= 2) {
            return SeverityLevel.HIGH;
        }

        // Build failures with any exception → HIGH
        if (category == FailureCategory.BUILD_FAILURE && exceptionCount >= 1) {
            return SeverityLevel.HIGH;
        }

        // Dependency failures → HIGH (blocks everything downstream)
        if (category == FailureCategory.DEPENDENCY_FAILURE) {
            return SeverityLevel.HIGH;
        }

        // Test failures with errors → MEDIUM
        if (category == FailureCategory.TEST_FAILURE && errorCount >= 1) {
            return SeverityLevel.MEDIUM;
        }

        // Warnings or single errors → MEDIUM
        if (warnCount >= 3 || errorCount >= 1) {
            return SeverityLevel.MEDIUM;
        }

        // Only warnings, no errors
        if (warnCount > 0) {
            return SeverityLevel.MEDIUM;
        }

        return SeverityLevel.LOW;
    }

    // ── Phase 4: Pipeline Stage Detection ────────────────────────────────────

    private String detectPipelineStage(List<String> lines) {
        for (String line : lines) {
            Matcher m = PIPELINE_STAGE_PATTERN.matcher(line);
            if (m.find() && m.groupCount() >= 2) {
                String stage = m.group(2).trim();
                if (!stage.isBlank() && stage.length() <= 100) {
                    return stage;
                }
            }
        }
        return "Unknown Stage";
    }

    // ── Phase 5: Human-Readable Generation ───────────────────────────────────

    private String generateTitle(FailureCategory category, List<String> errorLines, List<String> exceptionLines) {
        // Try to get the most descriptive first exception name
        if (!exceptionLines.isEmpty()) {
            String firstException = exceptionLines.get(0).trim();
            // Truncate long lines
            if (firstException.length() > 120) {
                firstException = firstException.substring(0, 120) + "...";
            }
            return category.getDisplayName() + ": " + firstException;
        }
        if (!errorLines.isEmpty()) {
            String firstError = errorLines.get(0).trim();
            if (firstError.length() > 120) {
                firstError = firstError.substring(0, 120) + "...";
            }
            return category.getDisplayName() + ": " + firstError;
        }
        return category.getDisplayName() + " detected in CI/CD pipeline";
    }

    private String generateSummary(FailureCategory category, List<String> errorLines,
                                   List<String> warnLines, List<String> exceptionLines,
                                   List<String> stackTraceLines, int totalLines) {
        StringBuilder sb = new StringBuilder();
        sb.append("AutoInsight detected a ").append(category.getDisplayName())
          .append(" in the CI/CD pipeline log. ");
        sb.append("Analysis of ").append(totalLines).append(" log lines found: ");
        sb.append(errorLines.size()).append(" error(s), ");
        sb.append(warnLines.size()).append(" warning(s), ");
        sb.append(exceptionLines.size()).append(" exception(s), and ");
        sb.append(stackTraceLines.size()).append(" stack trace frame(s). ");

        if (!exceptionLines.isEmpty()) {
            sb.append("Primary exception: ").append(exceptionLines.get(0).trim()).append(". ");
        }
        if (errorLines.size() > 3) {
            sb.append("Multiple errors indicate a systemic issue rather than a one-off fault. ");
        }
        return sb.toString().trim();
    }

    private String generateRootCause(FailureCategory category, List<String> errorLines,
                                     List<String> exceptionLines, String content) {
        StringBuilder sb = new StringBuilder();

        switch (category) {
            case BUILD_FAILURE -> {
                if (NULL_POINTER_PATTERN.matcher(content).find()) {
                    sb.append("A NullPointerException during compilation or static initialization suggests an uninitialized object or missing null-check in the codebase.");
                } else if (content.contains("cannot find symbol")) {
                    sb.append("Compilation failed due to an unresolved symbol — likely a missing import, incorrect class name, or mismatched method signature.");
                } else if (content.contains("BUILD FAILURE")) {
                    sb.append("Maven/Gradle reported a BUILD FAILURE. This may be caused by compilation errors, missing resources, or plugin configuration issues.");
                } else {
                    sb.append("The build process failed. Review compiler output for type mismatches, missing classes, or annotation processing errors.");
                }
            }
            case TEST_FAILURE -> {
                if (!exceptionLines.isEmpty()) {
                    sb.append("Test execution terminated due to: ").append(exceptionLines.get(0).trim())
                      .append(". This indicates either a test logic error or an environment setup issue (e.g., missing test data, mocked dependency failure).");
                } else {
                    sb.append("One or more test cases failed assertion checks. The expected and actual values differ, suggesting a regression or incorrect test expectations.");
                }
            }
            case DEPENDENCY_FAILURE -> {
                sb.append("The build system could not resolve one or more dependencies from the configured repositories. ");
                sb.append("Root causes include: repository unavailability, network connectivity issues, incorrect artifact coordinates, or version conflicts in the dependency tree.");
            }
            case DEPLOYMENT_FAILURE -> {
                if (OOM_PATTERN.matcher(content).find()) {
                    sb.append("The JVM ran out of heap memory (OutOfMemoryError) during deployment. The application's memory requirements exceed the configured JVM heap (-Xmx) or container memory limits.");
                } else if (content.contains("Connection refused")) {
                    sb.append("Deployment failed because the target service or port refused the connection. The destination service may be down, misconfigured, or the port may be blocked by a firewall.");
                } else if (content.contains("CrashLoopBackOff") || content.contains("container")) {
                    sb.append("A container entered a crash loop during deployment. This is typically caused by application startup failure, incorrect environment variables, or a missing health check endpoint.");
                } else {
                    sb.append("The deployment pipeline encountered a fatal error. Review infrastructure configuration, Kubernetes manifests, or Docker Compose settings.");
                }
            }
            default -> {
                if (!errorLines.isEmpty()) {
                    sb.append("Unclassified error detected: ").append(errorLines.get(0).trim())
                      .append(". Manual investigation of the full log is recommended.");
                } else {
                    sb.append("The root cause could not be automatically determined. Please review the full log output for contextual clues.");
                }
            }
        }
        return sb.toString();
    }

    private String generateSuggestedFix(FailureCategory category, String content) {
        StringBuilder sb = new StringBuilder();
        switch (category) {
            case BUILD_FAILURE -> {
                sb.append("1. Run `mvn clean compile` locally and review compiler errors.\n");
                sb.append("2. Ensure all required classes and interfaces are correctly imported.\n");
                sb.append("3. Check for missing or conflicting @Annotations (especially Lombok or MapStruct).\n");
                sb.append("4. Validate Maven plugin versions are compatible with your JDK version.\n");
                sb.append("5. Inspect the full `[ERROR]` lines in the Maven output for specific file:line references.");
            }
            case TEST_FAILURE -> {
                sb.append("1. Run `mvn test -Dtest=FailingTestClass` locally to reproduce the failure.\n");
                sb.append("2. Review the test assertion: check if expected values match the current logic.\n");
                sb.append("3. Check if mock/stub setup in @BeforeEach is correctly initializing dependencies.\n");
                sb.append("4. Verify test database/in-memory H2 schema is up to date with entity changes.\n");
                sb.append("5. If it's a flaky test, add retry logic or fix the underlying timing issue.");
            }
            case DEPENDENCY_FAILURE -> {
                sb.append("1. Run `mvn dependency:resolve` to identify unresolvable artifacts.\n");
                sb.append("2. Verify repository URLs in pom.xml or settings.xml are reachable.\n");
                sb.append("3. Check if the artifact version exists in Maven Central or your private Nexus/Artifactory.\n");
                sb.append("4. Clear the local cache: `rm -rf ~/.m2/repository` and retry the build.\n");
                sb.append("5. Confirm network connectivity from the CI agent to the repository host.");
            }
            case DEPLOYMENT_FAILURE -> {
                if (OOM_PATTERN.matcher(content).find()) {
                    sb.append("1. Increase JVM heap size: add `-Xmx1g` or higher to JVM startup args.\n");
                    sb.append("2. Increase container memory limits in Kubernetes Pod spec or Docker Compose.\n");
                    sb.append("3. Profile heap usage with Java Flight Recorder or VisualVM to find memory leaks.\n");
                    sb.append("4. Implement pagination or streaming for large data processing operations.");
                } else {
                    sb.append("1. Verify the target deployment environment is up and accessible.\n");
                    sb.append("2. Check Kubernetes events: `kubectl describe pod <pod-name>`.\n");
                    sb.append("3. Review container logs: `kubectl logs <pod-name> --previous`.\n");
                    sb.append("4. Validate environment variables, ConfigMaps, and Secrets are correctly mounted.\n");
                    sb.append("5. Confirm image tag exists in the container registry.");
                }
            }
            default -> {
                sb.append("1. Review the full log file for contextual error messages.\n");
                sb.append("2. Search for the specific exception class name in your codebase.\n");
                sb.append("3. Check recent commits that may have introduced regressions.\n");
                sb.append("4. Enable DEBUG logging in the relevant pipeline stage for more detail.");
            }
        }
        return sb.toString();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<String> limitLines(List<String> lines) {
        if (lines.size() <= MAX_LINES_IN_REPORT) return lines;
        return lines.subList(0, MAX_LINES_IN_REPORT);
    }

    private ParsedLogResult buildEmptyResult() {
        return ParsedLogResult.builder()
                .errorLines(Collections.emptyList())
                .warnLines(Collections.emptyList())
                .exceptionLines(Collections.emptyList())
                .stackTraceLines(Collections.emptyList())
                .errorCount(0).warningCount(0).exceptionCount(0).stackTraceCount(0)
                .detectedCategory(FailureCategory.UNKNOWN_FAILURE)
                .detectedSeverity(SeverityLevel.LOW)
                .incidentTitle("Empty or Unreadable Log File")
                .incidentSummary("The uploaded log file was empty or could not be read.")
                .probableRootCause("No content was found in the log file.")
                .suggestedFix("Ensure the correct log file is being uploaded and it is not empty.")
                .pipelineStage("Unknown")
                .totalLines(0)
                .build();
    }
}
