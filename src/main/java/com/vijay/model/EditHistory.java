package com.vijay.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 📝 EDIT HISTORY ENTITY
 * 
 * Tracks all code edits performed by users.
 * Enables learning from past edits and analyzing patterns.
 * 
 * ✅ PHASE 3: Advanced Features - Week 11
 */
@Entity
@Table(name = "edit_history", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_file_path", columnList = "file_path"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_user_created", columnList = "user_id,created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false, length = 500)
    private String filePath;
    
    @Column(columnDefinition = "LONGTEXT")
    private String originalCode;
    
    @Column(columnDefinition = "LONGTEXT")
    private String editedCode;
    
    @Column(length = 50)
    private String editType; // "extract_method", "rename", "optimize", etc.
    
    @Column(length = 100)
    private String suggestionSource; // "AI", "Rule-based", "Manual", etc.
    
    private Long suggestionId;
    
    @Column(nullable = false)
    private Boolean accepted; // Whether user accepted the suggestion
    
    @Column(length = 500)
    private String description; // What was changed
    
    @Column(length = 1000)
    private String context; // Additional context about the edit
    
    private Integer linesChanged;
    private Integer complexity; // Cyclomatic complexity change
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
