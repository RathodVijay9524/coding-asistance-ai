package com.vijay.repository;

import com.vijay.model.EditHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 📝 EDIT HISTORY REPOSITORY
 * 
 * Spring Data JPA repository for EditHistory entity.
 * Provides CRUD operations and custom queries for edit tracking.
 */
@Repository
public interface EditHistoryRepository extends JpaRepository<EditHistory, Long> {
    
    /**
     * Find all edits by user
     */
    Page<EditHistory> findByUserId(String userId, Pageable pageable);
    
    /**
     * Find all edits for a specific file
     */
    Page<EditHistory> findByFilePath(String filePath, Pageable pageable);
    
    /**
     * Find edits by user and file
     */
    Page<EditHistory> findByUserIdAndFilePath(String userId, String filePath, Pageable pageable);
    
    /**
     * Find edits by type
     */
    Page<EditHistory> findByEditType(String editType, Pageable pageable);
    
    /**
     * Find accepted edits
     */
    Page<EditHistory> findByUserIdAndAccepted(String userId, Boolean accepted, Pageable pageable);
    
    /**
     * Find edits within date range
     */
    @Query("SELECT e FROM EditHistory e WHERE e.userId = :userId AND e.createdAt BETWEEN :startDate AND :endDate")
    List<EditHistory> findByUserIdAndDateRange(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find edits by suggestion source
     */
    Page<EditHistory> findBySuggestionSource(String suggestionSource, Pageable pageable);
    
    /**
     * Get acceptance rate for user
     */
    @Query("SELECT COUNT(e) * 100.0 / COUNT(e) FROM EditHistory e WHERE e.userId = :userId AND e.accepted = true")
    Double getUserAcceptanceRate(@Param("userId") String userId);
    
    /**
     * Get most common edit types for user
     */
    @Query("SELECT e.editType, COUNT(e) as count FROM EditHistory e WHERE e.userId = :userId GROUP BY e.editType ORDER BY count DESC")
    List<Object[]> getMostCommonEditTypes(@Param("userId") String userId);
    
    /**
     * Count edits by user
     */
    Long countByUserId(String userId);
    
    /**
     * Count accepted edits by user
     */
    Long countByUserIdAndAccepted(String userId, Boolean accepted);
    
    /**
     * Find recent edits
     */
    @Query("SELECT e FROM EditHistory e WHERE e.userId = :userId ORDER BY e.createdAt DESC LIMIT :limit")
    List<EditHistory> findRecentEdits(@Param("userId") String userId, @Param("limit") int limit);
    
    /**
     * Find edits by suggestion ID
     */
    List<EditHistory> findBySuggestionId(Long suggestionId);
    
    /**
     * Find edits by user ordered by date
     */
    List<EditHistory> findByUserIdOrderByCreatedAtDesc(String userId);
}
