# AutoInsight – CI/CD Failure Intelligence Platform

> **Backend Service** | Java 17 · Spring Boot 3.2 · PostgreSQL · Maven

AutoInsight is an enterprise-grade backend that accepts uploaded CI/CD pipeline log files, intelligently parses them for failures, categorizes incidents, generates root cause analysis, and exposes REST APIs to power a dashboard frontend.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Prerequisites](#prerequisites)
3. [Project Setup](#project-setup)
4. [Database Setup](#database-setup)
5. [Running the Application](#running-the-application)
6. [REST API Reference](#rest-api-reference)
7. [Testing the APIs](#testing-the-apis)
8. [Project Structure](#project-structure)
9. [Design Decisions](#design-decisions)

---

## Architecture Overview

```
┌─────────────────────────────────────────────────┐
│              AutoInsight Backend                 │
│                                                  │
│  Controller Layer   → REST endpoints             │
│  Service Layer      → Business logic             │
│  Repository Layer   → Spring Data JPA            │
│  Utility Layer      → Parser, File Storage       │
│  Entity Layer       → JPA Entities + Enums       │
│  Exception Layer    → Global error handling      │
└─────────────┬───────────────────────────────────┘
              │
              ▼
      PostgreSQL Database
     (uploaded_logs, incidents, error_patterns)
```

### Core Log Processing Pipeline

```
POST /api/logs/upload
        │
        ▼
  FileStorageUtil.saveFile()          ← Save .txt to disk
        │
        ▼
  UploadedLog persisted (PENDING)     ← DB: uploaded_logs
        │
        ▼
  LogParserUtil.parse()               ← 5-phase parsing engine
  ├─ Phase 1: Classify lines (ERROR/WARN/Exception/StackTrace)
  ├─ Phase 2: Detect failure category (Build/Test/Dependency/Deployment)
  ├─ Phase 3: Compute severity (CRITICAL/HIGH/MEDIUM/LOW)
  ├─ Phase 4: Identify pipeline stage
  └─ Phase 5: Generate summary, root cause, fix suggestion
        │
        ▼
  Incident persisted                  ← DB: incidents
        │
        ▼
  UploadedLog updated (PROCESSED)
        │
        ▼
  LogUploadResponse returned to client
```

---

## Prerequisites

| Tool       | Version   | Notes                          |
|------------|-----------|--------------------------------|
| Java       | 17+       | OpenJDK or Oracle JDK          |
| Maven      | 3.8+      | Or use `./mvnw` wrapper        |
| PostgreSQL | 14+       | Running locally or via Docker  |
| Git        | Any       |                                |

---

## Project Setup

### 1. Clone the Repository

```bash
git clone https://github.com/your-org/autoinsight.git
cd autoinsight
```

### 2. Configure Database Connection

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/autoinsight_db
spring.datasource.username=your_postgres_user
spring.datasource.password=your_postgres_password
```

### 3. Configure Upload Directory (Optional)

By default, logs are saved to `uploads/logs/` relative to the working directory.
Override with:

```properties
autoinsight.upload.dir=/absolute/path/to/log/storage
```

---

## Database Setup

### Option A: PostgreSQL via Docker (Recommended for Dev)

```bash
docker run --name autoinsight-pg \
  -e POSTGRES_DB=autoinsight_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:15
```

### Option B: Local PostgreSQL

```bash
# Connect to PostgreSQL
psql -U postgres

# Create the database
CREATE DATABASE autoinsight_db;
\q
```

### Run the Schema

```bash
psql -U postgres -d autoinsight_db -f src/main/resources/schema.sql
```

This creates:
- `uploaded_logs` — stores file metadata and status
- `incidents` — stores parsed failure incidents
- `error_patterns` — pre-seeded regex patterns for categorization

---

## Running the Application

```bash
# Build the project
mvn clean install -DskipTests

# Run the application
mvn spring-boot:run
```

Or run the JAR directly:

```bash
java -jar target/autoinsight-1.0.0.jar
```

The application starts on **https://autoinsight-backend-a2ai.onrender.com**

---

## REST API Reference

### Base URL: `https://autoinsight-backend-a2ai.onrender.com/api`

---

### POST /api/logs/upload

Upload a CI/CD log file (.txt) for analysis.

**Request:** `multipart/form-data`

| Parameter    | Type   | Required | Description                    |
|-------------|--------|----------|-------------------------------|
| file        | File   | ✅       | The .txt log file (max 10MB)  |
| pipelineName | String | ❌      | Name of the CI/CD pipeline    |
| branchName   | String | ❌      | Git branch name               |

**Response: 201 Created**
```json
{
  "success": true,
  "message": "Log file uploaded and analyzed successfully",
  "data": {
    "logId": 1,
    "originalFileName": "build-failure-sample.txt",
    "uploadStatus": "PROCESSED",
    "pipelineName": "backend-ci",
    "branchName": "feature/auth",
    "fileSizeBytes": 2048,
    "incidentsDetected": 1,
    "uploadedAt": "2025-03-15T14:22:05",
    "processedAt": "2025-03-15T14:22:06",
    "message": "Log file processed successfully. 1 incident generated."
  },
  "timestamp": "2025-03-15T14:22:06"
}
```

---

### GET /api/incidents

Returns all incidents ordered by creation date (newest first).

**Response: 200 OK**
```json
{
  "success": true,
  "message": "Retrieved 2 incidents",
  "data": [
    {
      "id": 1,
      "logId": 1,
      "logFileName": "build-failure-sample.txt",
      "title": "Build Failure: NullPointerException: Cannot invoke method getBytes()",
      "failureCategory": "BUILD_FAILURE",
      "failureCategoryDisplay": "Build Failure",
      "severityLevel": "HIGH",
      "severityLevelDisplay": "High",
      "summary": "AutoInsight detected a Build Failure in the CI/CD pipeline log...",
      "probableRootCause": "A NullPointerException during compilation...",
      "suggestedFix": "1. Run `mvn clean compile` locally...",
      "errorCount": 6,
      "warningCount": 2,
      "exceptionCount": 1,
      "stackTraceCount": 6,
      "pipelineStage": "Compile",
      "pipelineName": "backend-ci",
      "branchName": "feature/auth",
      "createdAt": "2025-03-15T14:22:06"
    }
  ]
}
```

---

### GET /api/incidents/{id}

Returns the full detail of a single incident.

**Response: 200 OK** (same structure as list item, with rawErrorLines, rawExceptionLines)

**Response: 404 Not Found**
```json
{
  "success": false,
  "message": "Incident not found with ID: 99",
  "errorCode": "RESOURCE_NOT_FOUND"
}
```

---

### GET /api/incidents/severity/{level}

Filters incidents by severity level.

**Path parameter:** `level` = `CRITICAL` | `HIGH` | `MEDIUM` | `LOW` (case-insensitive)

**Response: 200 OK** — list of incidents filtered by the given severity

**Response: 400 Bad Request** — if level is invalid
```json
{
  "success": false,
  "message": "Invalid severity level: 'EXTREME'. Valid values are: CRITICAL, HIGH, MEDIUM, LOW",
  "errorCode": "INVALID_REQUEST"
}
```

---

### GET /api/dashboard/stats

Returns aggregated metrics for the dashboard.

**Response: 200 OK**
```json
{
  "success": true,
  "message": "Dashboard statistics retrieved",
  "data": {
    "totalLogsUploaded": 15,
    "totalLogsProcessed": 14,
    "totalLogsPending": 1,
    "totalIncidents": 14,
    "criticalIncidents": 3,
    "highIncidents": 5,
    "mediumIncidents": 4,
    "lowIncidents": 2,
    "incidentsByCategory": {
      "Build Failure": 5,
      "Test Failure": 3,
      "Dependency Failure": 2,
      "Deployment Failure": 3,
      "Unknown Failure": 1
    },
    "incidentsBySeverity": {
      "Critical": 3,
      "High": 5,
      "Medium": 4,
      "Low": 2
    },
    "totalErrorsDetected": 87,
    "totalWarningsDetected": 34,
    "totalExceptionsDetected": 22,
    "mostRecentIncident": { ... }
  }
}
```

---

## Testing the APIs

### Using cURL

```bash
# Upload a log file
curl -X POST https://autoinsight-backend-a2ai.onrender.com/api/logs/upload \
  -F "file=@sample-logs/build-failure-sample.txt" \
  -F "pipelineName=backend-ci" \
  -F "branchName=feature/auth"

# Get all incidents
curl https://autoinsight-backend-a2ai.onrender.com/api/incidents | jq

# Get incident by ID
curl https://autoinsight-backend-a2ai.onrender.com/api/incidents/1 | jq

# Filter by severity
curl https://autoinsight-backend-a2ai.onrender.com/api/incidents/severity/CRITICAL | jq

# Dashboard stats
curl https://autoinsight-backend-a2ai.onrender.com/api/dashboard/stats | jq
```

### Using Postman

1. Import the following collection base URL: `https://autoinsight-backend-a2ai.onrender.com`
2. For upload: Set method to POST, URL to `/api/logs/upload`, Body → form-data → key `file` (type: File)
3. Select one of the sample log files from the `sample-logs/` directory

---

## Project Structure

```
autoinsight/
├── pom.xml
├── README.md
├── sample-logs/
│   ├── build-failure-sample.txt
│   └── deployment-failure-oom.txt
└── src/
    └── main/
        ├── java/com/devops/autoinsight/
        │   ├── AutoInsightApplication.java
        │   ├── config/
        │   │   └── WebConfig.java
        │   ├── controller/
        │   │   ├── LogController.java
        │   │   ├── IncidentController.java
        │   │   └── DashboardController.java
        │   ├── dto/
        │   │   ├── request/
        │   │   │   └── LogUploadRequest.java
        │   │   └── response/
        │   │       ├── ApiResponse.java
        │   │       ├── LogUploadResponse.java
        │   │       ├── IncidentResponse.java
        │   │       └── DashboardStatsResponse.java
        │   ├── entity/
        │   │   ├── UploadedLog.java
        │   │   ├── Incident.java
        │   │   ├── ErrorPattern.java
        │   │   └── enums/
        │   │       ├── FailureCategory.java
        │   │       ├── SeverityLevel.java
        │   │       └── UploadStatus.java
        │   ├── exception/
        │   │   ├── GlobalExceptionHandler.java
        │   │   ├── ResourceNotFoundException.java
        │   │   ├── LogProcessingException.java
        │   │   └── InvalidFileException.java
        │   ├── repository/
        │   │   ├── UploadedLogRepository.java
        │   │   ├── IncidentRepository.java
        │   │   └── ErrorPatternRepository.java
        │   ├── service/
        │   │   ├── LogService.java
        │   │   ├── IncidentService.java
        │   │   ├── DashboardService.java
        │   │   └── impl/
        │   │       ├── LogServiceImpl.java
        │   │       ├── IncidentServiceImpl.java
        │   │       └── DashboardServiceImpl.java
        │   └── util/
        │       ├── LogParserUtil.java
        │       ├── FileStorageUtil.java
        │       └── ParsedLogResult.java
        └── resources/
            ├── application.properties
            └── schema.sql
```

---

## Design Decisions

| Decision | Rationale |
|----------|-----------|
| Synchronous processing | Keeps the architecture simple and internship-appropriate. Async (queues) can be added later. |
| DTOs at every boundary | Prevents entity exposure and decouples API contract from DB schema |
| Interface + Impl services | Follows enterprise patterns, enables mocking in tests |
| `@RestControllerAdvice` | Centralizes all error responses — consistent JSON structure across every endpoint |
| `ParsedLogResult` value object | Separates parsing logic from persistence concerns cleanly |
| Date-partitioned file storage | Avoids filesystem bloat in a single directory as upload volume grows |
| Schema-first DB setup | Explicit schema.sql over `ddl-auto=create` gives full control in production |

---

## Next Steps (Frontend Integration)

When building the React/Vue frontend:

1. Call `POST /api/logs/upload` with a file input and display the incident summary
2. Poll `GET /api/incidents` to show the incidents table
3. Use `GET /api/dashboard/stats` to render metric cards and category pie charts
4. Navigate to `GET /api/incidents/{id}` for incident detail with the suggested fix panel
5. Add a severity filter dropdown wired to `GET /api/incidents/severity/{level}`
