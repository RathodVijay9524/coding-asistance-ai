package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 🧠 LOCAL CACHE IMPLEMENTATION - Phase 10
 * 
 * Purpose: Local in-memory caching with LRU eviction
 * 
 * Responsibilities:
 * - Store cache in memory
 * - Implement LRU eviction
 * - TTL management
 * - Thread-safe operations
 */
@Service
public class LocalCacheImpl {
    
    private static final Logger logger = LoggerFactory.getLogger(LocalCacheImpl.class);
    
    private static final int DEFAULT_MAX_SIZE = 10000;
    
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final int maxSize;
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);
    
    public LocalCacheImpl() {
        this.maxSize = DEFAULT_MAX_SIZE;
    }
    
    public LocalCacheImpl(int maxSize) {
        this.maxSize = maxSize;
    }
    
    /**
     * Put value in cache
     */
    public synchronized void put(String key, Object value, long ttlSeconds) {
        // Check if eviction needed
        if (cache.size() >= maxSize) {
            evictLRU();
        }
        
        long expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        cache.put(key, new CacheEntry(value, expiryTime));
    }
    
    /**
     * Get value from cache
     */
    public Object get(String key) {
        CacheEntry entry = cache.get(key);
        
        if (entry == null) {
            misses.incrementAndGet();
            return null;
        }
        
        // Check if expired
        if (System.currentTimeMillis() > entry.expiryTime) {
            cache.remove(key);
            misses.incrementAndGet();
            return null;
        }
        
        hits.incrementAndGet();
        entry.lastAccessTime = System.currentTimeMillis();
        return entry.value;
    }
    
    /**
     * Remove from cache
     */
    public void remove(String key) {
        cache.remove(key);
    }
    
    /**
     * Clear cache
     */
    public synchronized void clear() {
        cache.clear();
        hits.set(0);
        misses.set(0);
    }
    
    /**
     * Evict expired entries
     */
    public synchronized void evictExpired() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> now > entry.getValue().expiryTime);
    }
    
    /**
     * Evict LRU entry
     */
    private void evictLRU() {
        if (cache.isEmpty()) {
            return;
        }
        
        // Find least recently used entry
        String lruKey = cache.entrySet().stream()
            .min(Comparator.comparingLong(e -> e.getValue().lastAccessTime))
            .map(Map.Entry::getKey)
            .orElse(null);
        
        if (lruKey != null) {
            cache.remove(lruKey);
            logger.debug("🗑️ LRU evicted: {}", lruKey);
        }
    }
    
    /**
     * Get cache statistics
     */
    public CacheManager.CacheStats getStats() {
        return new CacheManager.CacheStats(
            cache.size(),
            maxSize,
            hits.get(),
            misses.get()
        );
    }
    
    /**
     * Get cache size
     */
    public int size() {
        return cache.size();
    }
    
    /**
     * Check if key exists
     */
    public boolean containsKey(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return false;
        }
        
        // Check if expired
        if (System.currentTimeMillis() > entry.expiryTime) {
            cache.remove(key);
            return false;
        }
        
        return true;
    }
    
    // ============ Inner Classes ============
    
    /**
     * Cache entry with TTL and LRU tracking
     */
    private static class CacheEntry {
        final Object value;
        final long expiryTime;
        long lastAccessTime;
        
        CacheEntry(Object value, long expiryTime) {
            this.value = value;
            this.expiryTime = expiryTime;
            this.lastAccessTime = System.currentTimeMillis();
        }
    }
}
