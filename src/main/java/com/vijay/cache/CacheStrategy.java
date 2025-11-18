package com.vijay.cache;

/**
 * 🎯 Cache Strategy - Determines how long to cache tool results
 * 
 * Different tools have different cache durations:
 * - getCurrentDateTime: 30 seconds (changes frequently)
 * - getTodayEvents: 10 minutes (daily updates)
 * - getWeather: 2 minutes (real-time)
 * - getUserProfile: 1 hour (rarely changes)
 * - getProjectList: 30 minutes (changes occasionally)
 */
public enum CacheStrategy {
    
    // Real-time tools (very short cache)
    REAL_TIME(5, "Real-time data, cache 5 seconds"),
    
    // Frequently changing tools
    FREQUENTLY_CHANGING(30, "Changes every minute, cache 30 seconds"),
    
    // Moderately changing tools
    MODERATELY_CHANGING(120, "Changes every 2 minutes, cache 2 minutes"),
    
    // Slowly changing tools
    SLOWLY_CHANGING(600, "Changes every 10 minutes, cache 10 minutes"),
    
    // Static tools
    STATIC(3600, "Rarely changes, cache 1 hour"),
    
    // No caching
    NONE(0, "Do not cache");
    
    private final int durationSeconds;
    private final String description;
    
    CacheStrategy(int durationSeconds, String description) {
        this.durationSeconds = durationSeconds;
        this.description = description;
    }
    
    public int getDurationSeconds() {
        return durationSeconds;
    }
    
    public long getDurationMillis() {
        return (long) durationSeconds * 1000;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isCacheable() {
        return durationSeconds > 0;
    }
}
