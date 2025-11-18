package com.vijay.repository;

import com.vijay.model.SuggestionFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ⭐ SUGGESTION FEEDBACK REPOSITORY
 * 
 * Spring Data JPA repository for SuggestionFeedback entity.
 * Provides CRUD operations and custom queries for feedback tracking.
 */
@Repository
public interface SuggestionFeedbackRepository extends JpaRepository<SuggestionFeedback, Long> {
    
    /**
     * Find all feedback by user
     */
    Page<SuggestionFeedback> findByUserId(String userId, Pageable pageable);
    
    /**
     * Find feedback for a specific suggestion
     */
    List<SuggestionFeedback> findBySuggestionId(Long suggestionId);
    
    /**
     * Find feedback by rating
     */
    Page<SuggestionFeedback> findByRating(Integer rating, Pageable pageable);
    
    /**
     * Find helpful feedback
     */
    Page<SuggestionFeedback> findByUserIdAndHelpful(String userId, Boolean helpful, Pageable pageable);
    
    /**
     * Find feedback by action
     */
    Page<SuggestionFeedback> findByUserIdAndAction(String userId, String action, Pageable pageable);
    
    /**
     * Find feedback by suggestion type
     */
    Page<SuggestionFeedback> findBySuggestionType(String suggestionType, Pageable pageable);
    
    /**
     * Get average rating for user
     */
    @Query("SELECT AVG(f.rating) FROM SuggestionFeedback f WHERE f.userId = :userId")
    Double getAverageRating(@Param("userId") String userId);
    
    /**
     * Get average rating by suggestion type
     */
    @Query("SELECT AVG(f.rating) FROM SuggestionFeedback f WHERE f.suggestionType = :type")
    Double getAverageRatingByType(@Param("type") String type);
    
    /**
     * Get acceptance rate (accepted/total)
     */
    @Query("SELECT COUNT(f) * 100.0 / COUNT(f) FROM SuggestionFeedback f WHERE f.userId = :userId AND f.action = 'accepted'")
    Double getAcceptanceRate(@Param("userId") String userId);
    
    /**
     * Get helpful percentage
     */
    @Query("SELECT COUNT(f) * 100.0 / COUNT(f) FROM SuggestionFeedback f WHERE f.userId = :userId AND f.helpful = true")
    Double getHelpfulPercentage(@Param("userId") String userId);
    
    /**
     * Find feedback within date range
     */
    @Query("SELECT f FROM SuggestionFeedback f WHERE f.userId = :userId AND f.createdAt BETWEEN :startDate AND :endDate")
    List<SuggestionFeedback> findByUserIdAndDateRange(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find high-rated feedback
     */
    @Query("SELECT f FROM SuggestionFeedback f WHERE f.userId = :userId AND f.rating >= :minRating ORDER BY f.rating DESC")
    List<SuggestionFeedback> findHighRatedFeedback(
            @Param("userId") String userId,
            @Param("minRating") Integer minRating);
    
    /**
     * Find low-rated feedback (for improvement)
     */
    @Query("SELECT f FROM SuggestionFeedback f WHERE f.userId = :userId AND f.rating <= :maxRating ORDER BY f.rating ASC")
    List<SuggestionFeedback> findLowRatedFeedback(
            @Param("userId") String userId,
            @Param("maxRating") Integer maxRating);
    
    /**
     * Count feedback by user
     */
    Long countByUserId(String userId);
    
    /**
     * Count helpful feedback by user
     */
    Long countByUserIdAndHelpful(String userId, Boolean helpful);
    
    /**
     * Get most common feedback sentiment
     */
    @Query("SELECT f.sentiment, COUNT(f) as count FROM SuggestionFeedback f WHERE f.userId = :userId GROUP BY f.sentiment ORDER BY count DESC")
    List<Object[]> getMostCommonSentiment(@Param("userId") String userId);
    
    /**
     * Find feedback by sentiment
     */
    Page<SuggestionFeedback> findByUserIdAndSentiment(String userId, String sentiment, Pageable pageable);
    
    /**
     * Find all feedback by user (without pagination)
     */
    List<SuggestionFeedback> findByUserId(String userId);
    
    /**
     * Find feedback by user and rating
     */
    List<SuggestionFeedback> findByUserIdAndRating(String userId, Integer rating);
    
    /**
     * Find feedback by user and action
     */
    List<SuggestionFeedback> findByUserIdAndActionAndSentiment(String userId, String action, String sentiment);
    
    /**
     * Find helpful feedback by user
     */
    List<SuggestionFeedback> findByUserIdAndHelpfulTrue(String userId);
    
    /**
     * Find not helpful feedback by user
     */
    List<SuggestionFeedback> findByUserIdAndHelpfulFalse(String userId);
    
    /**
     * Find feedback by user and sentiment
     */
    List<SuggestionFeedback> findByUserIdAndSentiment(String userId, String sentiment);
    
    /**
     * Find feedback by user and action
     */
    List<SuggestionFeedback> findByUserIdAndAction(String userId, String action);
    
    /**
     * Find feedback by suggestion ID (ordered by date)
     */
    List<SuggestionFeedback> findBySuggestionIdOrderByCreatedAtDesc(Long suggestionId);
    
    /**
     * Find feedback by user (ordered by date)
     */
    List<SuggestionFeedback> findByUserIdOrderByCreatedAtDesc(String userId);
    
    /**
     * Find feedback by user and date range
     */
    List<SuggestionFeedback> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            String userId,
            LocalDateTime startDate,
            LocalDateTime endDate);
}
