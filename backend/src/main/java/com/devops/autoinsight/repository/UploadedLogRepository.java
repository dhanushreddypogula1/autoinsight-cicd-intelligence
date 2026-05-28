package com.devops.autoinsight.repository;

import com.devops.autoinsight.entity.UploadedLog;
import com.devops.autoinsight.entity.enums.UploadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing UploadedLog entities.
 */
@Repository
public interface UploadedLogRepository extends JpaRepository<UploadedLog, Long> {

    List<UploadedLog> findByUploadStatusOrderByUploadedAtDesc(UploadStatus status);

    long countByUploadStatus(UploadStatus status);

    boolean existsByOriginalFileName(String originalFileName);

    @Query("SELECT ul FROM UploadedLog ul ORDER BY ul.uploadedAt DESC")
    List<UploadedLog> findAllOrderByUploadedAtDesc();
}
