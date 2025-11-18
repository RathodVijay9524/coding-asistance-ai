package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🧠 CACHE MANAGER - Phase 10
 * 
 * Purpose: Memory caching interface (local + Redis)
 * 
 * Responsibilities:
 * - Cache advisor outputs
 * - Cache model responses
 * - Cache knowledge graph
 * - Cache user preferences
 * - TTL management
 */
@Service
public class CacheManager {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);
    
    private final LocalCacheImpl localCache;
    
    public CacheManager(LocalCacheImpl localCache) {
        this.localCache = localCache;
    }
    
    /**
     * Put value in cache
     */
    public void put(String key, Object value, long ttlSeconds) {
        try {
            localCache.put(key, value, ttlSeconds);
            logger.debug("💾 Cached: {} (TTL: {}s)", key, ttlSeconds);
        } catch (Exception e) {
            logger.error("❌ Cache put failed for {}: {}", key, e.getMessage());
        }
    }
    
    /**
     * Get value from cache
     */
    public Object get(String key) {
        try {
            Object value = localCache.get(key);
            if (value != null) {
                logger.debug("✅ Cache hit: {}", key);
            }
            return value;
        } catch (Exception e) {
            logger.error("❌ Cache get failed for {}: {}", key, e.getMessage());
            return null;
        }
    }
    
    /**
     * Remove from cache
     */
    public void remove(String key) {
        try {
            localCache.remove(key);
            logger.debug("🗑️ Removed from cache: {}", key);
        } catch (Exception e) {
            logger.error("❌ Cache remove failed for {}: {}", key, e.getMessage());
        }
    }
    
    /**
     * Clear cache
     */
    public void clear() {
        try {
            localCache.clear();
            logger.info("🧹 Cache cleared");
        } catch (Exception e) {
            logger.error("❌ Cache clear failed: {}", e.getMessage());
        }
    }
    
    /**
     * Get cache statistics
     */
    public CacheStats getStats() {
        return localCache.getStats();
    }
    
    /**
     * Cache advisor output
     */
    public void cacheAdvisorOutput(String advisorName, String queryHash, Object output) {
        String key = "advisor:" + advisorName + ":" + queryHash;
        put(key, output, 3600); // 1 hour TTL
    }
    
    /**
     * Get cached advisor output
     */
    public Object getCachedAdvisorOutput(String advisorName, String queryHash) {
        String key = "advisor:" + advisorName + ":" + queryHash;
        return get(key);
    }
    
    /**
     * Cache model response
     */
    public void cacheModelResponse(String modelName, String promptHash, Object response) {
        String key = "model:" + modelName + ":" + promptHash;
        put(key, response, 7200); // 2 hours TTL
    }
    
    /**
     * Get cached model response
     */
    public Object getCachedModelResponse(String modelName, String promptHash) {
        String key = "model:" + modelName + ":" + promptHash;
        return get(key);
    }
    
    /**
     * Cache user preferences
     */
    public void cacheUserPreferences(String userId, Object preferences) {
        String key = "user:" + userId + ":preferences";
        put(key, preferences, 3600); // 1 hour TTL
    }
    
    /**
     * Get cached user preferences
     */
    public Object getCachedUserPreferences(String userId) {
        String key = "user:" + userId + ":preferences";
        return get(key);
    }
    
    // ============ Inner Classes ============
    
    /**
     * Cache statistics
     */
    public static class CacheStats {
        public final int size;
        public final int maxSize;
        public final long hits;
        public final long misses;
        
        public CacheStats(int size, int maxSize, long hits, long misses) {
            this.size = size;
            this.maxSize = maxSize;
            this.hits = hits;
            this.misses = misses;
        }
        
        public double getHitRate() {
            long total = hits + misses;
            return total > 0 ? (double) hits / total * 100 : 0;
        }
    }
}
