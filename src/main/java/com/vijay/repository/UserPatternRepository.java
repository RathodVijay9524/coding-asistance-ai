package com.vijay.repository;

import com.vijay.model.UserPattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 👤 USER PATTERN REPOSITORY
 * 
 * Spring Data JPA repository for UserPattern entity.
 * Provides CRUD operations and custom queries for user pattern tracking.
 */
@Repository
public interface UserPatternRepository extends JpaRepository<UserPattern, Long> {
    
    /**
     * Find all patterns for a user
     */
    Page<UserPattern> findByUserId(String userId, Pageable pageable);
    
    /**
     * Find a specific pattern for a user
     */
    Optional<UserPattern> findByUserIdAndPatternType(String userId, String patternType);
    
    /**
     * Find active patterns for a user
     */
    List<UserPattern> findByUserIdAndActive(String userId, Boolean active);
    
    /**
     * Find patterns by type
     */
    Page<UserPattern> findByPatternType(String patternType, Pageable pageable);
    
    /**
     * Find patterns with high acceptance rate
     */
    @Query("SELECT p FROM UserPattern p WHERE p.userId = :userId AND p.acceptanceRate >= :minRate ORDER BY p.acceptanceRate DESC")
    List<UserPattern> findHighAcceptancePatterns(
            @Param("userId") String userId,
            @Param("minRate") Double minRate);
    
    /**
     * Get most frequent patterns by user
     */
    @Query("SELECT p FROM UserPattern p WHERE p.userId = :userId ORDER BY p.frequency DESC")
    List<UserPattern> findMostFrequentPatterns(@Param("userId") String userId);
    
    /**
     * Find active patterns by user
     */
    List<UserPattern> findByUserIdAndActiveTrue(String userId);
    
    /**
     * Find patterns with acceptance rate greater than threshold
     */
    List<UserPattern> findByUserIdAndAcceptanceRateGreaterThan(String userId, Double acceptanceRate);
    
    /**
     * Find patterns with low acceptance rate (for improvement)
     */
    @Query("SELECT p FROM UserPattern p WHERE p.userId = :userId AND p.acceptanceRate < :maxRate ORDER BY p.acceptanceRate ASC")
    List<UserPattern> findLowAcceptancePatterns(
            @Param("userId") String userId,
            @Param("maxRate") Double maxRate);
    
    /**
     * Get average acceptance rate for user
     */
    @Query("SELECT AVG(p.acceptanceRate) FROM UserPattern p WHERE p.userId = :userId")
    Double getAverageAcceptanceRate(@Param("userId") String userId);
    
    /**
     * Count patterns for user
     */
    Long countByUserId(String userId);
    
    /**
     * Count active patterns for user
     */
    Long countByUserIdAndActive(String userId, Boolean active);
    
    /**
     * Find patterns by acceptance rate range
     */
    @Query("SELECT p FROM UserPattern p WHERE p.userId = :userId AND p.acceptanceRate BETWEEN :minRate AND :maxRate")
    List<UserPattern> findPatternsByAcceptanceRange(
            @Param("userId") String userId,
            @Param("minRate") Double minRate,
            @Param("maxRate") Double maxRate);
}
