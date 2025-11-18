package com.vijay.controller;

import com.vijay.dto.EmotionalContext;
import com.vijay.dto.ResponseScenario;
import com.vijay.dto.UserMentalModel;
import com.vijay.service.EmotionalAnalyzer;
import com.vijay.service.EmotionalToneAdjuster;
import com.vijay.service.MentalSimulator;
import com.vijay.service.MentalStateInferencer;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 🎭 EMOTIONAL CONTROLLER
 *
 * Exposes an endpoint to analyze a user's emotional/mental state for a message
 * and return an adjusted response style suggestion.
 */
@RestController
@RequestMapping("/api/emotion")
@RequiredArgsConstructor
public class EmotionalController {

    private static final Logger logger = LoggerFactory.getLogger(EmotionalController.class);

    private final EmotionalAnalyzer emotionalAnalyzer;
    private final EmotionalToneAdjuster emotionalToneAdjuster;
    private final MentalStateInferencer mentalStateInferencer;
    private final MentalSimulator mentalSimulator;

    /**
     * Analyze emotional context for a user message and suggest response style.
     *
     * POST /api/emotion/analyze
     */
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyze(@RequestBody AnalyzeRequest request) {
        logger.info("🎭 Analyzing emotional context for user: {}", request.getUserId());

        try {
            String userId = request.getUserId();
            String message = request.getMessage();
            String baseResponse = request.getBaseResponse() != null && !request.getBaseResponse().isBlank()
                    ? request.getBaseResponse()
                    : "Thanks for your message. Here's how I can help.";

            // 1) Analyze emotion
            EmotionalContext context = emotionalAnalyzer.analyzeEmotion(message);

            // 2) Infer mental model
            mentalStateInferencer.inferFromQuery(userId, message);
            UserMentalModel model = mentalStateInferencer.getMentalModel(userId);

            // 3) Adjust tone based on context
            String adjustedResponse = emotionalToneAdjuster.adjustTone(baseResponse, context);

            // 4) Simulate response scenarios
            List<ResponseScenario> scenarios = mentalSimulator.simulateScenarios(message, adjustedResponse);
            ResponseScenario bestScenario = mentalSimulator.evaluateAndSelectBest(scenarios);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("timestamp", LocalDateTime.now());

            // Emotional context
            response.put("userId", userId);
            response.put("emotionalState", context.getCurrentState().name());
            response.put("emotionalIntensity", context.getEmotionalIntensity());
            response.put("triggerKeywords", context.getTriggerKeywords());
            response.put("recommendedTone", context.getRecommendedTone());
            response.put("emotionConfidence", context.getConfidence());

            // Mental model summary
            response.put("mentalModelSummary", model.getMentalStateSummary());

            // Adjusted base response
            response.put("adjustedResponse", adjustedResponse);

            // Best scenario suggestion
            response.put("bestScenarioTone", bestScenario.getTone());
            response.put("bestScenarioScore", bestScenario.getOverallScore());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Emotional analysis failed: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to analyze emotional context: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @Data
    public static class AnalyzeRequest {
        private String userId;
        private String message;
        private String baseResponse;
    }
}
