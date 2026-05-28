package com.devops.autoinsight.entity;

import com.devops.autoinsight.entity.enums.FailureCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a configurable regex-based pattern for detecting failure types in logs.
 * Patterns are stored in the database and can be activated/deactivated dynamically.
 */
@Entity
@Table(name = "error_patterns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pattern_name", nullable = false, unique = true, length = 255)
    private String patternName;

    @Column(name = "regex_pattern", nullable = false, columnDefinition = "TEXT")
    private String regexPattern;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_category", nullable = false, length = 100)
    private FailureCategory failureCategory;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Integer priority = 100;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
