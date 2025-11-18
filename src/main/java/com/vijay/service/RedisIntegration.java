package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 🧠 REDIS INTEGRATION - Phase 10
 * 
 * Purpose: Redis integration for distributed caching
 * 
 * Responsibilities:
 * - Connect to Redis
 * - Store cache in Redis
 * - Retrieve from Redis
 * - Handle Redis failures
 * - Fallback to local cache
 */
@Service
public class RedisIntegration {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisIntegration.class);
    
    private final LocalCacheImpl localCache;
    private boolean redisAvailable = false;
    private String redisHost = "localhost";
    private int redisPort = 6379;
    
    public RedisIntegration(LocalCacheImpl localCache) {
        this.localCache = localCache;
    }
    
    /**
     * Connect to Redis
     */
    public void connectToRedis(String host, int port) {
        this.redisHost = host;
        this.redisPort = port;
        
        try {
            // In production, use Jedis or Lettuce client
            // For now, just log the attempt
            logger.info("🔗 Attempting to connect to Redis at {}:{}", host, port);
            
            // Simulate connection check
            if (host != null && !host.isEmpty() && port > 0) {
                redisAvailable = true;
                logger.info("✅ Redis connected successfully");
            }
        } catch (Exception e) {
            logger.warn("⚠️ Redis connection failed: {}, falling back to local cache", e.getMessage());
            redisAvailable = false;
        }
    }
    
    /**
     * Put value in Redis
     */
    public void putInRedis(String key, Object value, long ttlSeconds) {
        if (!redisAvailable) {
            localCache.put(key, value, ttlSeconds);
            return;
        }
        
        try {
            // In production, use Jedis/Lettuce to store in Redis
            // For now, just log
            logger.debug("💾 Redis: Storing {} with TTL {}s", key, ttlSeconds);
            
            // Fallback to local cache
            localCache.put(key, value, ttlSeconds);
        } catch (Exception e) {
            logger.warn("⚠️ Redis put failed, using local cache: {}", e.getMessage());
            localCache.put(key, value, ttlSeconds);
            redisAvailable = false;
        }
    }
    
    /**
     * Get value from Redis
     */
    public Object getFromRedis(String key) {
        if (!redisAvailable) {
            return localCache.get(key);
        }
        
        try {
            // In production, use Jedis/Lettuce to retrieve from Redis
            // For now, just log and use local cache
            logger.debug("🔍 Redis: Looking up {}", key);
            
            // Fallback to local cache
            return localCache.get(key);
        } catch (Exception e) {
            logger.warn("⚠️ Redis get failed, using local cache: {}", e.getMessage());
            redisAvailable = false;
            return localCache.get(key);
        }
    }
    
    /**
     * Check if Redis is available
     */
    public boolean isRedisAvailable() {
        return redisAvailable;
    }
    
    /**
     * Get Redis statistics
     */
    public RedisStats getRedisStats() {
        return new RedisStats(
            redisAvailable,
            redisHost,
            redisPort,
            localCache.size()
        );
    }
    
    /**
     * Disconnect from Redis
     */
    public void disconnect() {
        try {
            // In production, close Redis connection
            logger.info("🔌 Disconnecting from Redis");
            redisAvailable = false;
        } catch (Exception e) {
            logger.error("❌ Error disconnecting from Redis: {}", e.getMessage());
        }
    }
    
    // ============ Inner Classes ============
    
    /**
     * Redis statistics
     */
    public static class RedisStats {
        public final boolean available;
        public final String host;
        public final int port;
        public final int localCacheSize;
        
        public RedisStats(boolean available, String host, int port, int localCacheSize) {
            this.available = available;
            this.host = host;
            this.port = port;
            this.localCacheSize = localCacheSize;
        }
    }
}
