package com.devops.autoinsight.entity;

import com.devops.autoinsight.entity.enums.UploadStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a CI/CD pipeline log file uploaded to the system.
 * Stores file metadata and processing status.
 */
@Entity
@Table(name = "uploaded_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "incidents")
public class UploadedLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", nullable = false, length = 50)
    @Builder.Default
    private UploadStatus uploadStatus = UploadStatus.PENDING;

    @Column(name = "pipeline_name", length = 255)
    private String pipelineName;

    @Column(name = "branch_name", length = 255)
    private String branchName;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @OneToMany(mappedBy = "uploadedLog", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Incident> incidents = new ArrayList<>();
}
