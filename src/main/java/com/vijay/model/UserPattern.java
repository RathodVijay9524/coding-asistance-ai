package com.vijay.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 👤 USER PATTERN ENTITY
 * 
 * Tracks patterns in user editing behavior.
 * Enables personalized suggestions based on user preferences.
 * 
 * ✅ PHASE 3: Advanced Features - Week 11
 */
@Entity
@Table(name = "user_pattern", indexes = {
    @Index(name = "idx_user_pattern_id", columnList = "user_id"),
    @Index(name = "idx_pattern_type", columnList = "pattern_type"),
    @Index(name = "idx_user_pattern_type", columnList = "user_id,pattern_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPattern {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false, length = 100)
    private String patternType; // "extract_method", "rename_variable", "add_null_check", etc.
    
    @Column(nullable = false)
    private Integer frequency; // How many times this pattern was used
    
    @Column(nullable = false)
    private Double acceptanceRate; // Percentage of suggestions accepted (0.0-1.0)
    
    @Column(length = 500)
    private String description; // Pattern description
    
    @Column(length = 1000)
    private String examples; // JSON array of example edits
    
    private Integer totalSuggestions; // Total suggestions of this type
    private Integer acceptedSuggestions; // How many were accepted
    
    private LocalDateTime lastUsed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Column(nullable = false)
    private Boolean active; // Whether this pattern is still relevant
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        active = true;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
