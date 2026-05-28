package com.devops.autoinsight.repository;

import com.devops.autoinsight.entity.Incident;
import com.devops.autoinsight.entity.enums.FailureCategory;
import com.devops.autoinsight.entity.enums.SeverityLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing Incident entities.
 * Provides queries for severity filtering, category aggregation, and dashboard stats.
 */
@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findBySeverityLevelOrderByCreatedAtDesc(SeverityLevel severityLevel);

    List<Incident> findByUploadedLogIdOrderByCreatedAtDesc(Long logId);

    List<Incident> findByFailureCategoryOrderByCreatedAtDesc(FailureCategory category);

    @Query("SELECT i FROM Incident i JOIN FETCH i.uploadedLog ORDER BY i.createdAt DESC")
    List<Incident> findAllWithLogOrderByCreatedAtDesc();

    @Query("SELECT i FROM Incident i JOIN FETCH i.uploadedLog WHERE i.id = :id")
    Optional<Incident> findByIdWithLog(@Param("id") Long id);

    long countBySeverityLevel(SeverityLevel severityLevel);

    long countByFailureCategory(FailureCategory category);

    @Query("SELECT SUM(i.errorCount) FROM Incident i")
    Long sumAllErrorCounts();

    @Query("SELECT SUM(i.warningCount) FROM Incident i")
    Long sumAllWarningCounts();

    @Query("SELECT SUM(i.exceptionCount) FROM Incident i")
    Long sumAllExceptionCounts();

    @Query("SELECT i FROM Incident i JOIN FETCH i.uploadedLog ORDER BY i.createdAt DESC LIMIT 1")
    Optional<Incident> findMostRecentIncident();
}
