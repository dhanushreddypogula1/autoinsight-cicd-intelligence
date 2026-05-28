package com.devops.autoinsight;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AutoInsight – CI/CD Failure Intelligence Platform
 *
 * <p>Entry point for the Spring Boot application.
 * Provides backend REST APIs for uploading CI/CD pipeline logs,
 * parsing failures, categorizing incidents, and serving dashboard metrics.
 *
 * <p>Author: AutoInsight Engineering Team
 * <p>Version: 1.0.0
 */
@Slf4j
@SpringBootApplication
public class AutoInsightApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoInsightApplication.class, args);
        log.info("══════════════════════════════════════════════════");
        log.info("  AutoInsight CI/CD Intelligence Platform Started  ");
        log.info("  API Base: https://autoinsight-backend-a2ai.onrender.com/api              ");
        log.info("══════════════════════════════════════════════════");
    }
}
