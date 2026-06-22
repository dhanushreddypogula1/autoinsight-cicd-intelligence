package com.devops.autoinsight.dto.request;

import lombok.Data;

@Data
public class AIAnalysisRequest {

    private String title;
    private String summary;
    private String rawLogs;
}