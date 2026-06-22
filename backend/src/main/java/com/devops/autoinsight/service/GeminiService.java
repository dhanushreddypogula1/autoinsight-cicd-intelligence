````java
package com.devops.autoinsight.service;

import com.devops.autoinsight.dto.request.AIAnalysisRequest;
import com.devops.autoinsight.dto.response.AIAnalysisResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiService {

    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AIAnalysisResponse analyzeIncident(AIAnalysisRequest request) {

        try {

            String prompt = """
                You are a senior DevOps engineer.

                Analyze this CI/CD failure.

                Title:
                %s

                Summary:
                %s

                Logs:
                %s

                Return ONLY valid JSON.

                {
                  "rootCause": "",
                  "businessImpact": "",
                  "recommendedFix": "",
                  "confidence": 0
                }

                Example:

                {
                  "rootCause":"Dependency version conflict",
                  "businessImpact":"Deployment blocked",
                  "recommendedFix":"Update dependency version and rebuild pipeline",
                  "confidence":92
                }
                """.formatted(
                    request.getTitle(),
                    request.getSummary(),
                    request.getRawLogs()
            );

            Map<String, Object> body = Map.of(
                    "contents",
                    new Object[]{
                            Map.of(
                                    "parts",
                                    new Object[]{
                                            Map.of("text", prompt)
                                    }
                            )
                    }
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(body, headers);

            String url = apiUrl + "?key=" + apiKey;

            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            url,
                            entity,
                            String.class
                    );

            JsonNode root =
                    objectMapper.readTree(response.getBody());

            String text =
                    root.path("candidates")
                            .get(0)
                            .path("content")
                            .path("parts")
                            .get(0)
                            .path("text")
                            .asText();

            text = text.replace("```json", "")
                       .replace("```", "")
                       .trim();

            JsonNode aiJson = objectMapper.readTree(text);

            return new AIAnalysisResponse(
                    aiJson.path("rootCause").asText(),
                    aiJson.path("businessImpact").asText(),
                    aiJson.path("recommendedFix").asText(),
                    aiJson.path("confidence").asInt()
            );

        } catch (Exception e) {

            return new AIAnalysisResponse(
                    "AI analysis unavailable",
                    "Unable to determine business impact",
                    "Retry analysis",
                    0
            );
        }
    }
}
````
