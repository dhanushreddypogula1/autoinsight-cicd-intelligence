
package com.devops.autoinsight.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisResponse {

    private String rootCause;
    private String businessImpact;
    private String recommendedFix;
    private Integer confidence;

    // New Fields
    private String riskLevel;
    private String estimatedResolutionTime;
    private String affectedComponent;
    private String actionPlan;
}
