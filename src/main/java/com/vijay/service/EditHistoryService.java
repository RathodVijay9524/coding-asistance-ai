package com.vijay.service;

import com.vijay.model.EditHistory;
import com.vijay.model.UserPattern;
import com.vijay.repository.EditHistoryRepository;
import com.vijay.repository.UserPatternRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 📝 EDIT HISTORY SERVICE
 * 
 * Manages edit history tracking and user pattern analysis.
 * Enables learning from past edits and improving suggestions.
 * 
 * ✅ PHASE 3: Advanced Features - Week 11
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EditHistoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(EditHistoryService.class);
    private final EditHistoryRepository editHistoryRepository;
    private final UserPatternRepository userPatternRepository;
    
    /**
     * Track a new edit
     */
    public EditHistory trackEdit(String userId, String filePath, String originalCode, 
                                 String editedCode, String editType, String suggestionSource,
                                 Boolean accepted, String description) {
        
        logger.info("📝 Tracking edit for user: {} in file: {}", userId, filePath);
        
        EditHistory edit = EditHistory.builder()
                .userId(userId)
                .filePath(filePath)
                .originalCode(originalCode)
                .editedCode(editedCode)
                .editType(editType)
                .suggestionSource(suggestionSource)
                .accepted(accepted)
                .description(description)
                .linesChanged(calculateLinesChanged(originalCode, editedCode))
                .build();
        
        EditHistory saved = editHistoryRepository.save(edit);
        
        // Update user patterns
        updateUserPattern(userId, editType, accepted);
        
        logger.info("✅ Edit tracked successfully: {}", saved.getId());
        return saved;
    }
    
    /**
     * Get edit history for user
     */
    public Page<EditHistory> getUserEditHistory(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return editHistoryRepository.findByUserId(userId, pageable);
    }
    
    /**
     * Get edit history for file
     */
    public Page<EditHistory> getFileEditHistory(String filePath, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return editHistoryRepository.findByFilePath(filePath, pageable);
    }
    
    /**
     * Get recent edits for user
     */
    public List<EditHistory> getRecentEdits(String userId, int limit) {
        return editHistoryRepository.findRecentEdits(userId, limit);
    }
    
    /**
     * Get user statistics
     */
    public Map<String, Object> getUserStatistics(String userId) {
        logger.info("📊 Calculating statistics for user: {}", userId);
        
        Long totalEdits = editHistoryRepository.countByUserId(userId);
        Long acceptedEdits = editHistoryRepository.countByUserIdAndAccepted(userId, true);
        Double acceptanceRate = totalEdits > 0 ? (acceptedEdits * 100.0 / totalEdits) : 0.0;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("userId", userId);
        stats.put("totalEdits", totalEdits);
        stats.put("acceptedEdits", acceptedEdits);
        stats.put("rejectedEdits", totalEdits - acceptedEdits);
        stats.put("acceptanceRate", String.format("%.2f%%", acceptanceRate));
        stats.put("mostCommonEditTypes", getMostCommonEditTypes(userId));
        stats.put("editsBySource", getEditsBySource(userId));
        
        return stats;
    }
    
    /**
     * Get most common edit types
     */
    private List<Map<String, Object>> getMostCommonEditTypes(String userId) {
        List<Object[]> results = editHistoryRepository.getMostCommonEditTypes(userId);
        return results.stream()
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("type", r[0]);
                    map.put("count", r[1]);
                    return map;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get edits grouped by source
     */
    private Map<String, Long> getEditsBySource(String userId) {
        Page<EditHistory> allEdits = editHistoryRepository.findByUserId(userId, PageRequest.of(0, Integer.MAX_VALUE));
        return allEdits.getContent().stream()
                .collect(Collectors.groupingBy(
                        e -> e.getSuggestionSource() != null ? e.getSuggestionSource() : "Unknown",
                        Collectors.counting()
                ));
    }
    
    /**
     * Update user pattern based on edit
     */
    private void updateUserPattern(String userId, String patternType, Boolean accepted) {
        Optional<UserPattern> existing = userPatternRepository.findByUserIdAndPatternType(userId, patternType);
        
        if (existing.isPresent()) {
            UserPattern pattern = existing.get();
            pattern.setFrequency(pattern.getFrequency() + 1);
            pattern.setTotalSuggestions(pattern.getTotalSuggestions() + 1);
            
            if (accepted) {
                pattern.setAcceptedSuggestions(pattern.getAcceptedSuggestions() + 1);
            }
            
            // Recalculate acceptance rate
            double rate = pattern.getTotalSuggestions() > 0 
                    ? (double) pattern.getAcceptedSuggestions() / pattern.getTotalSuggestions() 
                    : 0.0;
            pattern.setAcceptanceRate(rate);
            pattern.setLastUsed(LocalDateTime.now());
            
            userPatternRepository.save(pattern);
        } else {
            // Create new pattern
            UserPattern newPattern = UserPattern.builder()
                    .userId(userId)
                    .patternType(patternType)
                    .frequency(1)
                    .totalSuggestions(1)
                    .acceptedSuggestions(accepted ? 1 : 0)
                    .acceptanceRate(accepted ? 1.0 : 0.0)
                    .lastUsed(LocalDateTime.now())
                    .active(true)
                    .build();
            
            userPatternRepository.save(newPattern);
        }
    }
    
    /**
     * Get user patterns
     */
    public List<UserPattern> getUserPatterns(String userId) {
        return userPatternRepository.findByUserIdAndActive(userId, true);
    }
    
    /**
     * Get high acceptance patterns
     */
    public List<UserPattern> getHighAcceptancePatterns(String userId, Double minRate) {
        return userPatternRepository.findHighAcceptancePatterns(userId, minRate);
    }
    
    /**
     * Get most frequent patterns
     */
    public List<UserPattern> getMostFrequentPatterns(String userId) {
        return userPatternRepository.findMostFrequentPatterns(userId);
    }
    
    /**
     * Calculate lines changed
     */
    private Integer calculateLinesChanged(String original, String edited) {
        if (original == null || edited == null) return 0;
        
        int originalLines = original.split("\n").length;
        int editedLines = edited.split("\n").length;
        
        return Math.abs(editedLines - originalLines);
    }
    
    /**
     * Get edits within date range
     */
    public List<EditHistory> getEditsByDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        return editHistoryRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }
    
    /**
     * Get acceptance rate for user
     */
    public Double getAcceptanceRate(String userId) {
        return editHistoryRepository.getUserAcceptanceRate(userId);
    }
    
    /**
     * Delete old edits (for cleanup)
     */
    public void deleteOldEdits(LocalDateTime beforeDate) {
        logger.info("🗑️  Deleting edits before: {}", beforeDate);
        // This would require a custom repository method
        // For now, this is a placeholder
    }
}
