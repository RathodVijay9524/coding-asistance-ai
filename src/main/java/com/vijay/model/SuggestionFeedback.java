package com.vijay.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * ⭐ SUGGESTION FEEDBACK ENTITY
 * 
 * Collects user feedback on AI suggestions.
 * Enables learning which suggestions are most helpful.
 * 
 * ✅ PHASE 3: Advanced Features - Week 11
 */
@Entity
@Table(name = "suggestion_feedback", indexes = {
    @Index(name = "idx_feedback_user_id", columnList = "user_id"),
    @Index(name = "idx_feedback_suggestion_id", columnList = "suggestion_id"),
    @Index(name = "idx_feedback_rating", columnList = "rating"),
    @Index(name = "idx_feedback_created", columnList = "created_at"),
    @Index(name = "idx_feedback_user_created", columnList = "user_id,created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuggestionFeedback {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private Long suggestionId;
    
    @Column(length = 100)
    private String suggestionType; // "inline_suggestion", "transformation", "pattern", etc.
    
    @Column(columnDefinition = "LONGTEXT")
    private String suggestionContent; // The suggestion that was given
    
    @Column(nullable = false)
    private Integer rating; // 1-5 stars
    
    @Column(length = 50)
    private String action; // "accepted", "rejected", "modified", "ignored"
    
    @Column(length = 1000)
    private String feedback; // User's text feedback
    
    @Column(columnDefinition = "LONGTEXT")
    private String userModification; // If user modified the suggestion
    
    @Column(length = 500)
    private String reason; // Why user accepted/rejected
    
    private Boolean helpful; // Explicit helpful/not helpful flag
    private Boolean relevant; // Was the suggestion relevant?
    private Boolean accurate; // Was the suggestion accurate?
    
    @Column(length = 100)
    private String sentiment; // "positive", "neutral", "negative"
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
