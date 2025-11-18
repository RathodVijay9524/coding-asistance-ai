package com.vijay.controller;

import com.vijay.model.EditHistory;
import com.vijay.model.UserPattern;
import com.vijay.service.EditHistoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 📝 EDIT HISTORY CONTROLLER
 * 
 * REST endpoints for tracking and retrieving edit history.
 * Provides analytics and pattern analysis endpoints.
 * 
 * ✅ PHASE 3: Advanced Features - Week 11
 */
@RestController
@RequestMapping("/api/edits")
@RequiredArgsConstructor
public class EditHistoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(EditHistoryController.class);
    private final EditHistoryService editHistoryService;
    
    /**
     * Track a new edit
     * POST /api/edits/track
     */
    @PostMapping("/track")
    public ResponseEntity<Map<String, Object>> trackEdit(@RequestBody TrackEditRequest request) {
        logger.info("📝 Tracking edit for user: {}", request.getUserId());
        
        try {
            EditHistory edit = editHistoryService.trackEdit(
                    request.getUserId(),
                    request.getFilePath(),
                    request.getOriginalCode(),
                    request.getEditedCode(),
                    request.getEditType(),
                    request.getSuggestionSource(),
                    request.getAccepted(),
                    request.getDescription()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Edit tracked successfully");
            response.put("editId", edit.getId());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to track edit: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to track edit: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get user edit history
     * GET /api/edits/history/{userId}?page=0&size=10
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<Map<String, Object>> getUserEditHistory(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        logger.info("📊 Fetching edit history for user: {}", userId);
        
        try {
            Page<EditHistory> history = editHistoryService.getUserEditHistory(userId, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("userId", userId);
            response.put("totalEdits", history.getTotalElements());
            response.put("page", page);
            response.put("size", size);
            response.put("totalPages", history.getTotalPages());
            response.put("edits", history.getContent());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch edit history: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to fetch edit history: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get recent edits for user
     * GET /api/edits/recent/{userId}?limit=5
     */
    @GetMapping("/recent/{userId}")
    public ResponseEntity<Map<String, Object>> getRecentEdits(
            @PathVariable String userId,
            @RequestParam(defaultValue = "5") int limit) {
        
        logger.info("⏱️  Fetching recent edits for user: {}", userId);
        
        try {
            List<EditHistory> recentEdits = editHistoryService.getRecentEdits(userId, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("userId", userId);
            response.put("count", recentEdits.size());
            response.put("edits", recentEdits);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch recent edits: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to fetch recent edits: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get user statistics
     * GET /api/edits/stats/{userId}
     */
    @GetMapping("/stats/{userId}")
    public ResponseEntity<Map<String, Object>> getUserStatistics(@PathVariable String userId) {
        logger.info("📊 Calculating statistics for user: {}", userId);
        
        try {
            Map<String, Object> stats = editHistoryService.getUserStatistics(userId);
            
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
     * Get user patterns
     * GET /api/edits/patterns/{userId}
     */
    @GetMapping("/patterns/{userId}")
    public ResponseEntity<Map<String, Object>> getUserPatterns(@PathVariable String userId) {
        logger.info("👤 Fetching patterns for user: {}", userId);
        
        try {
            List<UserPattern> patterns = editHistoryService.getUserPatterns(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("userId", userId);
            response.put("patternCount", patterns.size());
            response.put("patterns", patterns);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch patterns: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to fetch patterns: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get high acceptance patterns
     * GET /api/edits/patterns/high-acceptance/{userId}?minRate=0.7
     */
    @GetMapping("/patterns/high-acceptance/{userId}")
    public ResponseEntity<Map<String, Object>> getHighAcceptancePatterns(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0.7") Double minRate) {
        
        logger.info("⭐ Fetching high acceptance patterns for user: {}", userId);
        
        try {
            List<UserPattern> patterns = editHistoryService.getHighAcceptancePatterns(userId, minRate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("userId", userId);
            response.put("minAcceptanceRate", minRate);
            response.put("patternCount", patterns.size());
            response.put("patterns", patterns);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch high acceptance patterns: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to fetch high acceptance patterns: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get most frequent patterns
     * GET /api/edits/patterns/frequent/{userId}
     */
    @GetMapping("/patterns/frequent/{userId}")
    public ResponseEntity<Map<String, Object>> getMostFrequentPatterns(@PathVariable String userId) {
        logger.info("📈 Fetching most frequent patterns for user: {}", userId);
        
        try {
            List<UserPattern> patterns = editHistoryService.getMostFrequentPatterns(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("userId", userId);
            response.put("patternCount", patterns.size());
            response.put("patterns", patterns);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch frequent patterns: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to fetch frequent patterns: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get acceptance rate
     * GET /api/edits/acceptance-rate/{userId}
     */
    @GetMapping("/acceptance-rate/{userId}")
    public ResponseEntity<Map<String, Object>> getAcceptanceRate(@PathVariable String userId) {
        logger.info("📊 Calculating acceptance rate for user: {}", userId);
        
        try {
            Double rate = editHistoryService.getAcceptanceRate(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("userId", userId);
            response.put("acceptanceRate", rate != null ? String.format("%.2f%%", rate) : "N/A");
            response.put("rawRate", rate);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to calculate acceptance rate: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to calculate acceptance rate: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get edits by date range
     * GET /api/edits/range/{userId}?startDate=2024-01-01&endDate=2024-12-31
     */
    @GetMapping("/range/{userId}")
    public ResponseEntity<Map<String, Object>> getEditsByDateRange(
            @PathVariable String userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        logger.info("📅 Fetching edits for user: {} between {} and {}", userId, startDate, endDate);
        
        try {
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
            
            List<EditHistory> edits = editHistoryService.getEditsByDateRange(userId, start, end);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("userId", userId);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("editCount", edits.size());
            response.put("edits", edits);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch edits by date range: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to fetch edits by date range: " + e.getMessage()
            ));
        }
    }
    
    // Request DTO
    @lombok.Data
    public static class TrackEditRequest {
        private String userId;
        private String filePath;
        private String originalCode;
        private String editedCode;
        private String editType;
        private String suggestionSource;
        private Boolean accepted;
        private String description;
    }
}
