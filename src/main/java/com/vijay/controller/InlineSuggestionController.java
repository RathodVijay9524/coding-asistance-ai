package com.vijay.controller;

import com.vijay.service.InlineSuggestionEngineService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 💡 INLINE SUGGESTION CONTROLLER
 * 
 * REST endpoints for real-time inline code suggestions.
 * Provides context-aware, personalized, and quick-fix suggestions.
 * 
 * ✅ PHASE 3.3: InlineSuggestionEngine - Week 13
 */
@RestController
@RequestMapping("/api/suggestions")
@RequiredArgsConstructor
public class InlineSuggestionController {
    
    private static final Logger logger = LoggerFactory.getLogger(InlineSuggestionController.class);
    private final InlineSuggestionEngineService suggestionService;
    
    /**
     * Generate inline suggestions for code
     * POST /api/suggestions/inline
     */
    @PostMapping("/inline")
    public ResponseEntity<Map<String, Object>> generateInlineSuggestions(
            @RequestBody GenerateSuggestionsRequest request) {
        
        logger.info("💡 Generating inline suggestions for user: {}", request.getUserId());
        
        try {
            List<InlineSuggestionEngineService.InlineSuggestion> suggestions = 
                    suggestionService.generateInlineSuggestions(
                            request.getUserId(),
                            request.getCode(),
                            request.getLanguage(),
                            request.getCursorPosition(),
                            request.getContext()
                    );
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("userId", request.getUserId());
            response.put("suggestionCount", suggestions.size());
            response.put("suggestions", suggestions);
            response.put("timestamp", new java.util.Date());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to generate suggestions: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to generate suggestions: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get context-aware suggestions
     * POST /api/suggestions/context
     */
    @PostMapping("/context")
    public ResponseEntity<Map<String, Object>> getContextAwareSuggestions(
            @RequestBody ContextSuggestionsRequest request) {
        
        logger.info("🎯 Getting context-aware suggestions for user: {}", request.getUserId());
        
        try {
            List<InlineSuggestionEngineService.InlineSuggestion> suggestions = 
                    suggestionService.getContextAwareSuggestions(
                            request.getUserId(),
                            request.getCode(),
                            request.getCurrentMethod(),
                            request.getCurrentClass()
                    );
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("userId", request.getUserId());
            response.put("suggestionCount", suggestions.size());
            response.put("suggestions", suggestions);
            response.put("timestamp", new java.util.Date());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to get context-aware suggestions: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to get context-aware suggestions: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get personalized suggestions
     * POST /api/suggestions/personalized
     */
    @PostMapping("/personalized")
    public ResponseEntity<Map<String, Object>> getPersonalizedSuggestions(
            @RequestBody PersonalizedSuggestionsRequest request) {
        
        logger.info("👤 Getting personalized suggestions for user: {}", request.getUserId());
        
        try {
            List<InlineSuggestionEngineService.InlineSuggestion> suggestions = 
                    suggestionService.getPersonalizedSuggestions(
                            request.getUserId(),
                            request.getCode()
                    );
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("userId", request.getUserId());
            response.put("suggestionCount", suggestions.size());
            response.put("suggestions", suggestions);
            response.put("timestamp", new java.util.Date());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to get personalized suggestions: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to get personalized suggestions: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get quick fix suggestions
     * POST /api/suggestions/quick-fix
     */
    @PostMapping("/quick-fix")
    public ResponseEntity<Map<String, Object>> getQuickFixSuggestions(
            @RequestBody QuickFixRequest request) {
        
        logger.info("🔧 Getting quick fix suggestions for user: {}", request.getUserId());
        
        try {
            List<InlineSuggestionEngineService.InlineSuggestion> suggestions = 
                    suggestionService.getQuickFixSuggestions(
                            request.getUserId(),
                            request.getCode(),
                            request.getErrorMessage()
                    );
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("userId", request.getUserId());
            response.put("suggestionCount", suggestions.size());
            response.put("suggestions", suggestions);
            response.put("timestamp", new java.util.Date());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to get quick fix suggestions: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to get quick fix suggestions: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get suggestion history
     * GET /api/suggestions/history/{userId}?limit=10
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<Map<String, Object>> getSuggestionHistory(
            @PathVariable String userId,
            @RequestParam(defaultValue = "10") int limit) {
        
        logger.info("📜 Fetching suggestion history for user: {}", userId);
        
        try {
            List<InlineSuggestionEngineService.InlineSuggestion> history = 
                    suggestionService.getSuggestionHistory(userId, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("userId", userId);
            response.put("limit", limit);
            response.put("count", history.size());
            response.put("history", history);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch suggestion history: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to fetch suggestion history: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Apply suggestion
     * POST /api/suggestions/apply
     */
    @PostMapping("/apply")
    public ResponseEntity<Map<String, Object>> applySuggestion(
            @RequestBody ApplySuggestionRequest request) {
        
        logger.info("✅ Applying suggestion for user: {}", request.getUserId());
        
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Suggestion applied successfully");
            response.put("userId", request.getUserId());
            response.put("suggestionId", request.getSuggestionId());
            response.put("appliedCode", request.getAppliedCode());
            response.put("timestamp", new java.util.Date());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to apply suggestion: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to apply suggestion: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Reject suggestion
     * POST /api/suggestions/reject
     */
    @PostMapping("/reject")
    public ResponseEntity<Map<String, Object>> rejectSuggestion(
            @RequestBody RejectSuggestionRequest request) {
        
        logger.info("❌ Rejecting suggestion for user: {}", request.getUserId());
        
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Suggestion rejected");
            response.put("userId", request.getUserId());
            response.put("suggestionId", request.getSuggestionId());
            response.put("reason", request.getReason());
            response.put("timestamp", new java.util.Date());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to reject suggestion: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to reject suggestion: " + e.getMessage()
            ));
        }
    }
    
    // Request DTOs
    @lombok.Data
    public static class GenerateSuggestionsRequest {
        private String userId;
        private String code;
        private String language;
        private Integer cursorPosition;
        private String context;
    }
    
    @lombok.Data
    public static class ContextSuggestionsRequest {
        private String userId;
        private String code;
        private String currentMethod;
        private String currentClass;
    }
    
    @lombok.Data
    public static class PersonalizedSuggestionsRequest {
        private String userId;
        private String code;
    }
    
    @lombok.Data
    public static class QuickFixRequest {
        private String userId;
        private String code;
        private String errorMessage;
    }
    
    @lombok.Data
    public static class ApplySuggestionRequest {
        private String userId;
        private String suggestionId;
        private String appliedCode;
    }
    
    @lombok.Data
    public static class RejectSuggestionRequest {
        private String userId;
        private String suggestionId;
        private String reason;
    }
}
