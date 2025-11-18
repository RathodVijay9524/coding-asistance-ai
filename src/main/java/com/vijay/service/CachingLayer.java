package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ⚡ Caching Layer
 * 
 * Caches analysis results:
 * - Cache brain analysis results
 * - Cache code context
 * - Cache suggestions
 * - TTL-based expiration
 */
@Component
public class CachingLayer {
    
    private static final Logger logger = LoggerFactory.getLogger(CachingLayer.class);
    
    private static final long DEFAULT_TTL_MS = 5 * 60 * 1000; // 5 minutes
    private static final int MAX_CACHE_SIZE = 1000;
    
    private final Map<String, CacheEntry<?>> cache = new ConcurrentHashMap<>();
    
    /**
     * Cache entry with TTL
     */
    private static class CacheEntry<T> {
        T value;
        long expiryTime;
        
        CacheEntry(T value, long ttlMs) {
            this.value = value;
            this.expiryTime = System.currentTimeMillis() + ttlMs;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }
    
    /**
     * Put value in cache
     */
    public <T> void put(String key, T value) {
        put(key, value, DEFAULT_TTL_MS);
    }
    
    /**
     * Put value with custom TTL
     */
    public <T> void put(String key, T value, long ttlMs) {
        if (cache.size() >= MAX_CACHE_SIZE) {
            evictExpired();
        }
        
        cache.put(key, new CacheEntry<>(value, ttlMs));
        logger.debug("💾 Cached: {} (TTL: {}ms)", key, ttlMs);
    }
    
    /**
     * Get value from cache
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        CacheEntry<?> entry = cache.get(key);
        
        if (entry == null) {
            logger.debug("❌ Cache miss: {}", key);
            return null;
        }
        
        if (entry.isExpired()) {
            cache.remove(key);
            logger.debug("⏰ Cache expired: {}", key);
            return null;
        }
        
        logger.debug("✅ Cache hit: {}", key);
        return (T) entry.value;
    }
    
    /**
     * Check if key exists and not expired
     */
    public boolean contains(String key) {
        CacheEntry<?> entry = cache.get(key);
        
        if (entry == null) {
            return false;
        }
        
        if (entry.isExpired()) {
            cache.remove(key);
            return false;
        }
        
        return true;
    }
    
    /**
     * Remove from cache
     */
    public void remove(String key) {
        cache.remove(key);
        logger.debug("🗑️ Removed from cache: {}", key);
    }
    
    /**
     * Clear all cache
     */
    public void clear() {
        cache.clear();
        logger.debug("🧹 Cache cleared");
    }
    
    /**
     * Evict expired entries
     */
    public void evictExpired() {
        int before = cache.size();
        
        cache.entrySet().removeIf(e -> e.getValue().isExpired());
        
        int after = cache.size();
        logger.debug("🧹 Evicted {} expired entries", before - after);
    }
    
    /**
     * Get cache statistics
     */
    public CacheStats getStats() {
        CacheStats stats = new CacheStats();
        stats.totalSize = cache.size();
        stats.maxSize = MAX_CACHE_SIZE;
        stats.utilizationPercent = (stats.totalSize * 100) / stats.maxSize;
        
        int expiredCount = 0;
        for (CacheEntry<?> entry : cache.values()) {
            if (entry.isExpired()) {
                expiredCount++;
            }
        }
        stats.expiredCount = expiredCount;
        
        return stats;
    }
    
    /**
     * Cache Statistics DTO
     */
    public static class CacheStats {
        public int totalSize;
        public int maxSize;
        public int utilizationPercent;
        public int expiredCount;
        
        @Override
        public String toString() {
            return String.format("CacheStats{size=%d/%d (%d%%), expired=%d}",
                    totalSize, maxSize, utilizationPercent, expiredCount);
        }
    }
}
