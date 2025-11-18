package com.vijay.service;

import com.vijay.model.SuggestionFeedback;
import com.vijay.repository.SuggestionFeedbackRepository;
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
 * ⭐ SUGGESTION FEEDBACK SERVICE
 * 
 * Manages user feedback on AI suggestions.
 * Tracks ratings, actions, and sentiment to improve suggestion quality.
 * Provides analytics on suggestion effectiveness.
 * 
 * ✅ PHASE 3.2: User Feedback System - Week 12
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SuggestionFeedbackService {
    
    private static final Logger logger = LoggerFactory.getLogger(SuggestionFeedbackService.class);
    private final SuggestionFeedbackRepository feedbackRepository;
    
    /**
     * Record feedback on a suggestion
     */
    public SuggestionFeedback recordFeedback(
            String userId,
            Long suggestionId,
            String suggestionType,
            String suggestionContent,
            Integer rating,
            String action,
            String feedback,
            String userModification,
            String reason,
            Boolean helpful,
            Boolean relevant,
            Boolean accurate,
            String sentiment) {
        
        logger.info("⭐ Recording feedback for user: {} on suggestion: {}", userId, suggestionId);
        
        SuggestionFeedback feedbackEntity = SuggestionFeedback.builder()
                .userId(userId)
                .suggestionId(suggestionId)
                .suggestionType(suggestionType)
                .suggestionContent(suggestionContent)
                .rating(rating)
                .action(action)
                .feedback(feedback)
                .userModification(userModification)
                .reason(reason)
                .helpful(helpful)
                .relevant(relevant)
                .accurate(accurate)
                .sentiment(sentiment)
                .createdAt(LocalDateTime.now())
                .build();
        
        SuggestionFeedback saved = feedbackRepository.save(feedbackEntity);
        logger.info("✅ Feedback recorded with ID: {}", saved.getId());
        
        return saved;
    }
    
    /**
     * Get user's feedback history
     */
    @Transactional(readOnly = true)
    public Page<SuggestionFeedback> getUserFeedback(String userId, int page, int size) {
        logger.info("📊 Fetching feedback for user: {}", userId);
        Pageable pageable = PageRequest.of(page, size);
        return feedbackRepository.findByUserId(userId, pageable);
    }
    
    /**
     * Get feedback for a specific suggestion
     */
    @Transactional(readOnly = true)
    public List<SuggestionFeedback> getSuggestionFeedback(Long suggestionId) {
        logger.info("📝 Fetching feedback for suggestion: {}", suggestionId);
        return feedbackRepository.findBySuggestionIdOrderByCreatedAtDesc(suggestionId);
    }
    
    /**
     * Get feedback by rating
     */
    @Transactional(readOnly = true)
    public List<SuggestionFeedback> getFeedbackByRating(String userId, Integer rating) {
        logger.info("⭐ Fetching feedback with rating {} for user: {}", rating, userId);
        return feedbackRepository.findByUserIdAndRating(userId, rating);
    }
    
    /**
     * Get feedback by action
     */
    @Transactional(readOnly = true)
    public List<SuggestionFeedback> getFeedbackByAction(String userId, String action) {
        logger.info("📋 Fetching feedback with action {} for user: {}", action, userId);
        return feedbackRepository.findByUserIdAndAction(userId, action);
    }
    
    /**
     * Get helpful feedback
     */
    @Transactional(readOnly = true)
    public List<SuggestionFeedback> getHelpfulFeedback(String userId) {
        logger.info("👍 Fetching helpful feedback for user: {}", userId);
        return feedbackRepository.findByUserIdAndHelpfulTrue(userId);
    }
    
    /**
     * Get not helpful feedback
     */
    @Transactional(readOnly = true)
    public List<SuggestionFeedback> getNotHelpfulFeedback(String userId) {
        logger.info("👎 Fetching not helpful feedback for user: {}", userId);
        return feedbackRepository.findByUserIdAndHelpfulFalse(userId);
    }
    
    /**
     * Get feedback by sentiment
     */
    @Transactional(readOnly = true)
    public List<SuggestionFeedback> getFeedbackBySentiment(String userId, String sentiment) {
        logger.info("😊 Fetching feedback with sentiment {} for user: {}", sentiment, userId);
        return feedbackRepository.findByUserIdAndSentiment(userId, sentiment);
    }
    
    /**
     * Get feedback statistics for user
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserFeedbackStatistics(String userId) {
        logger.info("📊 Calculating feedback statistics for user: {}", userId);
        
        List<SuggestionFeedback> allFeedback = feedbackRepository.findByUserId(userId);
        
        if (allFeedback.isEmpty()) {
            return Map.of(
                    "userId", userId,
                    "totalFeedback", 0,
                    "averageRating", 0.0,
                    "helpfulCount", 0,
                    "notHelpfulCount", 0,
                    "acceptanceRate", "0%",
                    "rejectionRate", "0%"
            );
        }
        
        // Calculate statistics
        double averageRating = allFeedback.stream()
                .mapToInt(SuggestionFeedback::getRating)
                .average()
                .orElse(0.0);
        
        long helpfulCount = allFeedback.stream()
                .filter(f -> f.getHelpful() != null && f.getHelpful())
                .count();
        
        long notHelpfulCount = allFeedback.stream()
                .filter(f -> f.getHelpful() != null && !f.getHelpful())
                .count();
        
        long acceptedCount = allFeedback.stream()
                .filter(f -> "accepted".equalsIgnoreCase(f.getAction()))
                .count();
        
        long rejectedCount = allFeedback.stream()
                .filter(f -> "rejected".equalsIgnoreCase(f.getAction()))
                .count();
        
        double acceptanceRate = allFeedback.size() > 0 
                ? (acceptedCount * 100.0 / allFeedback.size()) 
                : 0.0;
        
        double rejectionRate = allFeedback.size() > 0 
                ? (rejectedCount * 100.0 / allFeedback.size()) 
                : 0.0;
        
        // Count by sentiment
        Map<String, Long> sentimentCounts = allFeedback.stream()
                .filter(f -> f.getSentiment() != null)
                .collect(Collectors.groupingBy(SuggestionFeedback::getSentiment, Collectors.counting()));
        
        // Count by action
        Map<String, Long> actionCounts = allFeedback.stream()
                .filter(f -> f.getAction() != null)
                .collect(Collectors.groupingBy(SuggestionFeedback::getAction, Collectors.counting()));
        
        // Count by suggestion type
        Map<String, Long> typeCounts = allFeedback.stream()
                .filter(f -> f.getSuggestionType() != null)
                .collect(Collectors.groupingBy(SuggestionFeedback::getSuggestionType, Collectors.counting()));
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("userId", userId);
        stats.put("totalFeedback", allFeedback.size());
        stats.put("averageRating", String.format("%.2f", averageRating));
        stats.put("helpfulCount", helpfulCount);
        stats.put("notHelpfulCount", notHelpfulCount);
        stats.put("helpfulPercentage", String.format("%.2f%%", (helpfulCount * 100.0 / allFeedback.size())));
        stats.put("acceptedCount", acceptedCount);
        stats.put("rejectedCount", rejectedCount);
        stats.put("acceptanceRate", String.format("%.2f%%", acceptanceRate));
        stats.put("rejectionRate", String.format("%.2f%%", rejectionRate));
        stats.put("sentimentDistribution", sentimentCounts);
        stats.put("actionDistribution", actionCounts);
        stats.put("suggestionTypeDistribution", typeCounts);
        
        return stats;
    }
    
    /**
     * Get suggestion effectiveness (average rating by suggestion type)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSuggestionEffectiveness(String userId) {
        logger.info("📈 Calculating suggestion effectiveness for user: {}", userId);
        
        List<SuggestionFeedback> allFeedback = feedbackRepository.findByUserId(userId);
        
        Map<String, List<SuggestionFeedback>> byType = allFeedback.stream()
                .filter(f -> f.getSuggestionType() != null)
                .collect(Collectors.groupingBy(SuggestionFeedback::getSuggestionType));
        
        Map<String, Object> effectiveness = new HashMap<>();
        
        for (Map.Entry<String, List<SuggestionFeedback>> entry : byType.entrySet()) {
            String type = entry.getKey();
            List<SuggestionFeedback> feedback = entry.getValue();
            
            double avgRating = feedback.stream()
                    .mapToInt(SuggestionFeedback::getRating)
                    .average()
                    .orElse(0.0);
            
            long helpfulCount = feedback.stream()
                    .filter(f -> f.getHelpful() != null && f.getHelpful())
                    .count();
            
            double helpfulPercentage = feedback.size() > 0 
                    ? (helpfulCount * 100.0 / feedback.size()) 
                    : 0.0;
            
            Map<String, Object> typeStats = new HashMap<>();
            typeStats.put("count", feedback.size());
            typeStats.put("averageRating", String.format("%.2f", avgRating));
            typeStats.put("helpfulCount", helpfulCount);
            typeStats.put("helpfulPercentage", String.format("%.2f%%", helpfulPercentage));
            
            effectiveness.put(type, typeStats);
        }
        
        return effectiveness;
    }
    
    /**
     * Get most helpful suggestion types
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMostHelpfulSuggestionTypes(String userId) {
        logger.info("⭐ Finding most helpful suggestion types for user: {}", userId);
        
        Map<String, Object> effectiveness = getSuggestionEffectiveness(userId);
        
        return effectiveness.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("suggestionType", entry.getKey());
                    result.putAll((Map<String, Object>) entry.getValue());
                    return result;
                })
                .sorted((a, b) -> {
                    String ratingA = (String) a.get("averageRating");
                    String ratingB = (String) b.get("averageRating");
                    return Double.compare(Double.parseDouble(ratingB), Double.parseDouble(ratingA));
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get least helpful suggestion types
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getLeastHelpfulSuggestionTypes(String userId) {
        logger.info("📉 Finding least helpful suggestion types for user: {}", userId);
        
        Map<String, Object> effectiveness = getSuggestionEffectiveness(userId);
        
        return effectiveness.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("suggestionType", entry.getKey());
                    result.putAll((Map<String, Object>) entry.getValue());
                    return result;
                })
                .sorted((a, b) -> {
                    String ratingA = (String) a.get("averageRating");
                    String ratingB = (String) b.get("averageRating");
                    return Double.compare(Double.parseDouble(ratingA), Double.parseDouble(ratingB));
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get feedback by date range
     */
    @Transactional(readOnly = true)
    public List<SuggestionFeedback> getFeedbackByDateRange(
            String userId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        
        logger.info("📅 Fetching feedback for user: {} between {} and {}", userId, startDate, endDate);
        return feedbackRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startDate, endDate);
    }
    
    /**
     * Get recent feedback
     */
    @Transactional(readOnly = true)
    public List<SuggestionFeedback> getRecentFeedback(String userId, int limit) {
        logger.info("⏱️  Fetching recent {} feedback for user: {}", limit, userId);
        List<SuggestionFeedback> allFeedback = feedbackRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return allFeedback.stream().limit(limit).collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Update feedback
     */
    public SuggestionFeedback updateFeedback(
            Long feedbackId,
            Integer rating,
            String action,
            String feedback,
            Boolean helpful,
            Boolean relevant,
            Boolean accurate,
            String sentiment) {
        
        logger.info("✏️  Updating feedback: {}", feedbackId);
        
        SuggestionFeedback existing = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found: " + feedbackId));
        
        if (rating != null) existing.setRating(rating);
        if (action != null) existing.setAction(action);
        if (feedback != null) existing.setFeedback(feedback);
        if (helpful != null) existing.setHelpful(helpful);
        if (relevant != null) existing.setRelevant(relevant);
        if (accurate != null) existing.setAccurate(accurate);
        if (sentiment != null) existing.setSentiment(sentiment);
        
        SuggestionFeedback updated = feedbackRepository.save(existing);
        logger.info("✅ Feedback updated: {}", feedbackId);
        
        return updated;
    }
    
    /**
     * Delete feedback
     */
    public void deleteFeedback(Long feedbackId) {
        logger.info("🗑️  Deleting feedback: {}", feedbackId);
        feedbackRepository.deleteById(feedbackId);
        logger.info("✅ Feedback deleted: {}", feedbackId);
    }
}
