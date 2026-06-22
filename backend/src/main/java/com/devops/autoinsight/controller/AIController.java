package com.devops.autoinsight.controller;

import com.devops.autoinsight.dto.request.AIAnalysisRequest;
import com.devops.autoinsight.dto.response.AIAnalysisResponse;
import com.devops.autoinsight.dto.response.ApiResponse;
import com.devops.autoinsight.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AIController {

    private final GeminiService geminiService;

    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<AIAnalysisResponse>> analyze(
            @RequestBody AIAnalysisRequest request
    ) {
        AIAnalysisResponse response = geminiService.analyzeIncident(request);

        return ResponseEntity.ok(
                ApiResponse.success(response, "AI analysis generated successfully")
        );
    }
}