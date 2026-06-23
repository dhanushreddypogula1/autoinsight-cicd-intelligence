# AutoInsight 🚀

### AI-Powered Engineering Operations Intelligence Platform

AutoInsight is an AI-powered DevOps incident intelligence platform that automatically analyzes CI/CD pipeline failures, identifies probable root causes, assesses operational risk, and generates actionable remediation plans using Large Language Models (LLMs).

The platform transforms raw CI/CD logs into engineering insights, helping teams reduce troubleshooting time, accelerate incident resolution, and improve software delivery reliability.

---

## Overview

Modern software systems rely heavily on CI/CD pipelines for continuous integration and deployment. When failures occur, engineers often spend significant time manually reviewing logs, identifying root causes, and planning remediation steps.

AutoInsight automates this process by combining:

* Rule-based log parsing
* Incident classification
* Severity assessment
* AI-powered root cause analysis
* Business impact evaluation
* Automated action plan generation

The result is a centralized engineering intelligence platform capable of converting thousands of log lines into actionable operational insights.

---

## Key Features

### Intelligent Log Processing

* Upload CI/CD pipeline logs
* Parse build, test, deployment, and infrastructure failures
* Detect errors, warnings, exceptions, and stack traces
* Extract critical failure information automatically

### Incident Detection Engine

AutoInsight automatically classifies incidents into categories such as:

* Build Failure
* Dependency Failure
* Deployment Failure
* Test Failure
* Infrastructure Failure
* Unknown Failure

### Severity Classification

Each incident is categorized as:

* Critical
* High
* Medium
* Low

Based on:

* Failure type
* Error density
* Exception frequency
* Operational impact

### AI Copilot Analysis

Using Google Gemini, AutoInsight generates:

#### Root Cause Analysis

Identifies likely causes of failure.

#### Business Impact Assessment

Explains how the incident affects software delivery and operations.

#### Recommended Fixes

Provides remediation suggestions.

#### Confidence Scoring

Measures AI confidence in generated insights.

#### Risk Assessment

Classifies incidents by operational risk level.

#### Resolution Time Estimation

Predicts expected resolution effort.

#### Affected Component Detection

Highlights impacted services and systems.

#### Action Plan Generation

Produces step-by-step remediation guidance.

---

## Dashboard Analytics

The AutoInsight dashboard provides:

* Total Logs Uploaded
* Total Incidents
* Error Statistics
* Exception Statistics
* Severity Distribution
* Failure Category Distribution
* Recent Incident Activity

These metrics provide engineering teams with operational visibility into CI/CD health.

---

## System Architecture

```text
                 ┌────────────────────┐
                 │   CI/CD Log Files  │
                 └──────────┬─────────┘
                            │
                            ▼
                 ┌────────────────────┐
                 │ Log Parsing Engine │
                 └──────────┬─────────┘
                            │
                            ▼
                 ┌────────────────────┐
                 │ Incident Detection │
                 └──────────┬─────────┘
                            │
                            ▼
                 ┌────────────────────┐
                 │ Severity Analysis  │
                 └──────────┬─────────┘
                            │
                            ▼
                 ┌────────────────────┐
                 │ Gemini AI Copilot  │
                 └──────────┬─────────┘
                            │
                            ▼
                 ┌────────────────────┐
                 │ Risk Assessment    │
                 └──────────┬─────────┘
                            │
                            ▼
                 ┌────────────────────┐
                 │ Dashboard Insights │
                 └────────────────────┘
```

---

## Application Workflow

### Step 1 – Upload Log

Upload a CI/CD pipeline log file.

### Step 2 – Parse Logs

AutoInsight scans the log and extracts:

* Errors
* Warnings
* Exceptions
* Stack traces

### Step 3 – Generate Incident

An incident is created automatically with:

* Failure category
* Severity level
* Detection summary

### Step 4 – AI Copilot Analysis

The AI engine generates:

* Root cause analysis
* Business impact
* Recommended fixes
* Action plan

### Step 5 – Dashboard Update

Analytics and operational metrics are updated automatically.

---

## Technology Stack

### Frontend

* React
* Vite
* Tailwind CSS
* Axios
* Recharts
* Lucide React

### Backend

* Java 17
* Spring Boot 3
* Spring Data JPA
* Hibernate
* Maven
* Lombok

### Database

* PostgreSQL

### AI Layer

* Google Gemini API

### Deployment

* Vercel (Frontend)
* Render (Backend)

---

## Project Structure

```text
AutoInsight
│
├── frontend/
│   ├── src/
│   ├── public/
│   └── README.md
│
├── backend/
│   ├── src/
│   ├── sample-logs/
│   ├── uploads/
│   └── README.md
│
└── README.md
```

---

## Sample Incident Analysis

### Input

```log
[ERROR] Failed to execute goal on project autoinsight
```

### AutoInsight Output

**Category:** Dependency Failure

**Severity:** High

**Root Cause:** Dependency resolution failed during build execution.

**Business Impact:** CI/CD pipeline blocked, preventing deployment.

**Recommended Fix:** Validate dependency versions and repository configuration.

**Risk Level:** High

**Estimated Resolution Time:** 15–30 Minutes

**Affected Component:** Maven Dependency Resolver

---

## Local Development Setup

### Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend:

```text
http://localhost:8080
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend:

```text
http://localhost:5173
```

---

## Environment Variables

Backend configuration:

```properties
gemini.api.key=YOUR_GEMINI_API_KEY
gemini.api.url=YOUR_GEMINI_API_URL
```

---

## Future Enhancements

* GitHub Actions Integration
* Jenkins Integration
* Jira Integration
* Slack Notifications
* Predictive Failure Detection
* Multi-Agent Incident Investigation
* Kubernetes Monitoring
* MLOps Pipeline Intelligence
* Real-Time Log Streaming

---

## Research Potential

AutoInsight can serve as a foundation for research in:

* AIOps
* DevOps Intelligence
* Root Cause Analysis
* Log Analytics
* LLM-Assisted Incident Investigation
* Engineering Operations Intelligence

---

## Contributors

### Dhanush Reddy Pogula

B.Tech AIML
Malla Reddy University

---

## License

MIT License

---

**AutoInsight transforms CI/CD failures into actionable engineering intelligence through automated incident detection and AI-powered analysis.**
