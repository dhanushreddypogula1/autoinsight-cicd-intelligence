package com.devops.autoinsight.repository;

import com.devops.autoinsight.entity.ErrorPattern;
import com.devops.autoinsight.entity.enums.FailureCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ErrorPattern entities.
 * Loads active patterns ordered by priority for use during log parsing.
 */
@Repository
public interface ErrorPatternRepository extends JpaRepository<ErrorPattern, Long> {

    List<ErrorPattern> findByIsActiveTrueOrderByPriorityAsc();

    List<ErrorPattern> findByFailureCategoryAndIsActiveTrue(FailureCategory category);

    boolean existsByPatternName(String patternName);
}
