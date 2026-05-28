package com.devops.autoinsight.entity;

import com.devops.autoinsight.entity.enums.FailureCategory;
import com.devops.autoinsight.entity.enums.SeverityLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a parsed failure incident detected from a CI/CD log file.
 * Contains categorized failure details, severity assessment, root cause, and fix suggestions.
 */
@Entity
@Table(name = "incidents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_id", nullable = false)
    private UploadedLog uploadedLog;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_category", nullable = false, length = 100)
    private FailureCategory failureCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity_level", nullable = false, length = 50)
    private SeverityLevel severityLevel;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "probable_root_cause", columnDefinition = "TEXT")
    private String probableRootCause;

    @Column(name = "suggested_fix", columnDefinition = "TEXT")
    private String suggestedFix;

    @Column(name = "error_count", nullable = false)
    @Builder.Default
    private Integer errorCount = 0;

    @Column(name = "warning_count", nullable = false)
    @Builder.Default
    private Integer warningCount = 0;

    @Column(name = "exception_count", nullable = false)
    @Builder.Default
    private Integer exceptionCount = 0;

    @Column(name = "stack_trace_count", nullable = false)
    @Builder.Default
    private Integer stackTraceCount = 0;

    @Column(name = "raw_error_lines", columnDefinition = "TEXT")
    private String rawErrorLines;

    @Column(name = "raw_exception_lines", columnDefinition = "TEXT")
    private String rawExceptionLines;

    @Column(name = "pipeline_stage", length = 255)
    private String pipelineStage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
