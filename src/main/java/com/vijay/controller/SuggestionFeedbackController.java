package com.vijay.controller;

import com.vijay.model.SuggestionFeedback;
import com.vijay.service.SuggestionFeedbackService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ⭐ SUGGESTION FEEDBACK CONTROLLER
 * 
 * REST endpoints for collecting and analyzing user feedback on suggestions.
 * Enables learning which suggestions are most helpful.
 * 
 * ✅ PHASE 3.2: User Feedback System - Week 12
 */
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class SuggestionFeedbackController {
    
    private static final Logger logger = LoggerFactory.getLogger(SuggestionFeedbackController.class);
    private final SuggestionFeedbackService feedbackService;
    
    /**
     * Record feedback on a suggestion
     * POST /api/feedback/record
     */
    @PostMapping("/record")
    public ResponseEntity<Map<String, Object>> recordFeedback(@RequestBody RecordFeedbackRequest request) {
        logger.info("⭐ Recording feedback for user: {}", request.getUserId());
        
        try {
            SuggestionFeedback feedback = feedbackService.recordFeedback(
                    request.getUserId(),
                    request.getSuggestionId(),
                    request.getSuggestionType(),
                    request.getSuggestionContent(),
                    request.getRating(),
                    request.getAction(),
                    request.getFeedback(),
                    request.getUserModification(),
                    request.getReason(),
                    request.getHelpful(),
                    request.getRelevant(),
                    request.getAccurate(),
                    request.getSentiment()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Feedback recorded successfully");
            response.put("feedbackId", feedback.getId());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to record feedback: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to record feedback: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get user's feedback history
     * GET /api/feedback/history/{userId}?page=0&size=10
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<Map<String, Object>> getUserFeedback(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        logger.info("📊 Fetching feedback history for user: {}", userId);
        
        try {
            Page<SuggestionFeedback> feedback = feedbackService.getUserFeedback(userId, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("userId", userId);
            response.put("totalFeedback", feedback.getTotalElements());
            response.put("page", page);
            response.put("size", size);
            response.put("totalPages", feedback.getTotalPages());
            response.put("feedback", feedback.getContent());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch feedback history: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to fetch feedback history: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get feedback for a specific suggestion
     * GET /api/feedback/suggestion/{suggestionId}
     */
    @GetMapping("/suggestion/{suggestionId}")
    public ResponseEntity<Map<String, Object>> getSuggestionFeedback(@PathVariable Long suggestionId) {
        logger.info("📝 Fetching feedback for suggestion: {}", suggestionId);
        
        try {
            List<SuggestionFeedback> feedback = feedbackService.getSuggestionFeedback(suggestionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("suggestionId", suggestionId);
            response.put("feedbackCount", feedback.size());
            response.put("feedback", feedback);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch suggestion feedback: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to fetch suggestion feedback: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get feedback by rating
     * GET /api/feedback/rating/{userId}?rating=5
     */
    @GetMapping("/rating/{userId}")
    public ResponseEntity<Map<String, Object>> getFeedbackByRating(
            @PathVariable String userId,
            @RequestParam Integer rating) {
        
        logger.info("⭐ Fetching feedback with rating {} for user: {}", rating, userId);
        
        try {
            List<SuggestionFeedback> feedback = feedbackService.getFeedbackByRating(userId, rating);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("userId", userId);
            response.put("rating", rating);
            response.put("feedbackCount", feedback.size());
            response.put("feedback", feedback);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch feedback by rating: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to fetch feedback by rating: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get feedback by action
     * GET /api/feedback/action/{userId}?action=accepted
     */
    @GetMapping("/action/{userId}")
    public ResponseEntity<Map<String, Object>> getFeedbackByAction(
            @PathVariable String userId,
            @RequestParam String action) {
        
        logger.info("📋 Fetching feedback with action {} for user: {}", action, userId);
        
        try {
            List<SuggestionFeedback> feedback = feedbackService.getFeedbackByAction(userId, action);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("userId", userId);
            response.put("action", action);
            response.put("feedbackCount", feedback.size());
            response.put("feedback", feedback);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch feedback by action: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to fetch feedback by action: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get helpful feedback
     * GET /api/feedback/helpful/{userId}
     */
    @GetMapping("/helpful/{userId}")
    public ResponseEntity<Map<String, Object>> getHelpfulFeedback(@PathVariable String userId) {
        logger.info("👍 Fetching helpful feedback for user: {}", userId);
        
        try {
            List<SuggestionFeedback> feedback = feedbackService.getHelpfulFeedback(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("userId", userId);
            response.put("feedbackCount", feedback.size());
            response.put("feedback", feedback);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch helpful feedback: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to fetch helpful feedback: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get not helpful feedback
     * GET /api/feedback/not-helpful/{userId}
     */
    @GetMapping("/not-helpful/{userId}")
    public ResponseEntity<Map<String, Object>> getNotHelpfulFeedback(@PathVariable String userId) {
        logger.info("👎 Fetching not helpful feedback for user: {}", userId);
        
        try {
            List<SuggestionFeedback> feedback = feedbackService.getNotHelpfulFeedback(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("userId", userId);
            response.put("feedbackCount", feedback.size());
            response.put("feedback", feedback);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch not helpful feedback: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to fetch not helpful feedback: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get feedback by sentiment
     * GET /api/feedback/sentiment/{userId}?sentiment=positive
     */
    @GetMapping("/sentiment/{userId}")
    public ResponseEntity<Map<String, Object>> getFeedbackBySentiment(
            @PathVariable String userId,
            @RequestParam String sentiment) {
        
        logger.info("😊 Fetching feedback with sentiment {} for user: {}", sentiment, userId);
        
        try {
            List<SuggestionFeedback> feedback = feedbackService.getFeedbackBySentiment(userId, sentiment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("userId", userId);
            response.put("sentiment", sentiment);
            response.put("feedbackCount", feedback.size());
            response.put("feedback", feedback);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch feedback by sentiment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to fetch feedback by sentiment: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get feedback statistics
     * GET /api/feedback/stats/{userId}
     */
    @GetMapping("/stats/{userId}")
    public ResponseEntity<Map<String, Object>> getUserFeedbackStatistics(@PathVariable String userId) {
        logger.info("📊 Calculating feedback statistics for user: {}", userId);
        
        try {
            Map<String, Object> stats = feedbackService.getUserFeedbackStatistics(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.putAll(stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to calculate statistics: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to calculate statistics: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get suggestion effectiveness
     * GET /api/feedback/effectiveness/{userId}
     */
    @GetMapping("/effectiveness/{userId}")
    public ResponseEntity<Map<String, Object>> getSuggestionEffectiveness(@PathVariable String userId) {
        logger.info("📈 Calculating suggestion effectiveness for user: {}", userId);
        
        try {
            Map<String, Object> effectiveness = feedbackService.getSuggestionEffectiveness(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("userId", userId);
            response.put("effectiveness", effectiveness);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to calculate effectiveness: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to calculate effectiveness: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get most helpful suggestion types
     * GET /api/feedback/most-helpful/{userId}
     */
    @GetMapping("/most-helpful/{userId}")
    public ResponseEntity<Map<String, Object>> getMostHelpfulSuggestionTypes(@PathVariable String userId) {
        logger.info("⭐ Finding most helpful suggestion types for user: {}", userId);
        
        try {
            List<Map<String, Object>> types = feedbackService.getMostHelpfulSuggestionTypes(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("userId", userId);
            response.put("count", types.size());
            response.put("suggestionTypes", types);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch most helpful types: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to fetch most helpful types: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get least helpful suggestion types
     * GET /api/feedback/least-helpful/{userId}
     */
    @GetMapping("/least-helpful/{userId}")
    public ResponseEntity<Map<String, Object>> getLeastHelpfulSuggestionTypes(@PathVariable String userId) {
        logger.info("📉 Finding least helpful suggestion types for user: {}", userId);
        
        try {
            List<Map<String, Object>> types = feedbackService.getLeastHelpfulSuggestionTypes(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("userId", userId);
            response.put("count", types.size());
            response.put("suggestionTypes", types);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch least helpful types: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to fetch least helpful types: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get recent feedback
     * GET /api/feedback/recent/{userId}?limit=5
     */
    @GetMapping("/recent/{userId}")
    public ResponseEntity<Map<String, Object>> getRecentFeedback(
            @PathVariable String userId,
            @RequestParam(defaultValue = "5") int limit) {
        
        logger.info("⏱️  Fetching recent {} feedback for user: {}", limit, userId);
        
        try {
            List<SuggestionFeedback> feedback = feedbackService.getRecentFeedback(userId, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("userId", userId);
            response.put("count", feedback.size());
            response.put("feedback", feedback);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch recent feedback: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to fetch recent feedback: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get feedback by date range
     * GET /api/feedback/range/{userId}?startDate=2024-01-01&endDate=2024-12-31
     */
    @GetMapping("/range/{userId}")
    public ResponseEntity<Map<String, Object>> getFeedbackByDateRange(
            @PathVariable String userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        logger.info("📅 Fetching feedback for user: {} between {} and {}", userId, startDate, endDate);
        
        try {
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
            
            List<SuggestionFeedback> feedback = feedbackService.getFeedbackByDateRange(userId, start, end);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("userId", userId);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("feedbackCount", feedback.size());
            response.put("feedback", feedback);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch feedback by date range: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to fetch feedback by date range: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Update feedback
     * PUT /api/feedback/update/{feedbackId}
     */
    @PutMapping("/update/{feedbackId}")
    public ResponseEntity<Map<String, Object>> updateFeedback(
            @PathVariable Long feedbackId,
            @RequestBody UpdateFeedbackRequest request) {
        
        logger.info("✏️  Updating feedback: {}", feedbackId);
        
        try {
            SuggestionFeedback updated = feedbackService.updateFeedback(
                    feedbackId,
                    request.getRating(),
                    request.getAction(),
                    request.getFeedback(),
                    request.getHelpful(),
                    request.getRelevant(),
                    request.getAccurate(),
                    request.getSentiment()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Feedback updated successfully");
            response.put("feedbackId", updated.getId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to update feedback: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to update feedback: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Delete feedback
     * DELETE /api/feedback/delete/{feedbackId}
     */
    @DeleteMapping("/delete/{feedbackId}")
    public ResponseEntity<Map<String, Object>> deleteFeedback(@PathVariable Long feedbackId) {
        logger.info("🗑️  Deleting feedback: {}", feedbackId);
        
        try {
            feedbackService.deleteFeedback(feedbackId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Feedback deleted successfully");
            response.put("feedbackId", feedbackId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to delete feedback: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to delete feedback: " + e.getMessage()
            ));
        }
    }
    
    // Request DTOs
    @lombok.Data
    public static class RecordFeedbackRequest {
        private String userId;
        private Long suggestionId;
        private String suggestionType;
        private String suggestionContent;
        private Integer rating;
        private String action;
        private String feedback;
        private String userModification;
        private String reason;
        private Boolean helpful;
        private Boolean relevant;
        private Boolean accurate;
        private String sentiment;
    }
    
    @lombok.Data
    public static class UpdateFeedbackRequest {
        private Integer rating;
        private String action;
        private String feedback;
        private Boolean helpful;
        private Boolean relevant;
        private Boolean accurate;
        private String sentiment;
    }
}
