package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🧠 API LOGGER - Phase 10
 * 
 * Purpose: Log every brain's API calls with detailed metrics
 * 
 * Responsibilities:
 * - Log advisor execution
 * - Log model calls
 * - Log token usage
 * - Log latency
 * - Log errors
 * - Provide statistics
 */
@Service
public class APILogger {
    
    private static final Logger logger = LoggerFactory.getLogger(APILogger.class);
    
    // Tracking
    private final Map<String, AdvisorStats> advisorStats = new ConcurrentHashMap<>();
    private final List<APILogEntry> logEntries = Collections.synchronizedList(new ArrayList<>());
    
    /**
     * Log advisor call
     */
    public void logAdvisorCall(String advisorName, long durationMs, int tokensUsed, String status) {
        APILogEntry entry = new APILogEntry(
            advisorName,
            durationMs,
            tokensUsed,
            status,
            System.currentTimeMillis()
        );
        
        logEntries.add(entry);
        
        // Update stats
        AdvisorStats stats = advisorStats.computeIfAbsent(advisorName, k -> new AdvisorStats(advisorName));
        stats.recordCall(durationMs, tokensUsed, status);
        
        logger.info("📊 API Log: {} - {}ms, {} tokens, {}", 
            advisorName, durationMs, tokensUsed, status);
    }
    
    /**
     * Log model call
     */
    public void logModelCall(String modelName, int tokensUsed, long durationMs, String status) {
        APILogEntry entry = new APILogEntry(
            "MODEL:" + modelName,
            durationMs,
            tokensUsed,
            status,
            System.currentTimeMillis()
        );
        
        logEntries.add(entry);
        
        logger.info("🤖 Model Call: {} - {} tokens, {}ms, {}", 
            modelName, tokensUsed, durationMs, status);
    }
    
    /**
     * Log error
     */
    public void logError(String advisorName, Exception e) {
        logAdvisorCall(advisorName, 0, 0, "ERROR: " + e.getMessage());
        logger.error("❌ Error in {}: {}", advisorName, e.getMessage());
    }
    
    /**
     * Get advisor statistics
     */
    public AdvisorStats getAdvisorStats(String advisorName) {
        return advisorStats.get(advisorName);
    }
    
    /**
     * Get all statistics
     */
    public Map<String, AdvisorStats> getAllStats() {
        return new HashMap<>(advisorStats);
    }
    
    /**
     * Get recent logs
     */
    public List<APILogEntry> getRecentLogs(int count) {
        int start = Math.max(0, logEntries.size() - count);
        return new ArrayList<>(logEntries.subList(start, logEntries.size()));
    }
    
    /**
     * Clear logs
     */
    public void clearLogs() {
        logEntries.clear();
        advisorStats.clear();
        logger.info("🧹 API logs cleared");
    }
    
    /**
     * Get total statistics
     */
    public TotalStats getTotalStats() {
        int totalCalls = logEntries.size();
        int totalTokens = logEntries.stream().mapToInt(e -> e.tokensUsed).sum();
        long totalDuration = logEntries.stream().mapToLong(e -> e.durationMs).sum();
        long errorCount = logEntries.stream().filter(e -> e.status.contains("ERROR")).count();
        
        return new TotalStats(totalCalls, totalTokens, totalDuration, errorCount);
    }
    
    // ============ Inner Classes ============
    
    /**
     * API log entry
     */
    public static class APILogEntry {
        public final String component;
        public final long durationMs;
        public final int tokensUsed;
        public final String status;
        public final long timestamp;
        
        public APILogEntry(String component, long durationMs, int tokensUsed, String status, long timestamp) {
            this.component = component;
            this.durationMs = durationMs;
            this.tokensUsed = tokensUsed;
            this.status = status;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * Advisor statistics
     */
    public static class AdvisorStats {
        public final String advisorName;
        public int callCount = 0;
        public long totalDuration = 0;
        public int totalTokens = 0;
        public int errorCount = 0;
        public long minDuration = Long.MAX_VALUE;
        public long maxDuration = 0;
        
        public AdvisorStats(String advisorName) {
            this.advisorName = advisorName;
        }
        
        public synchronized void recordCall(long duration, int tokens, String status) {
            callCount++;
            totalDuration += duration;
            totalTokens += tokens;
            minDuration = Math.min(minDuration, duration);
            maxDuration = Math.max(maxDuration, duration);
            
            if (status.contains("ERROR")) {
                errorCount++;
            }
        }
        
        public double getAverageDuration() {
            return callCount > 0 ? (double) totalDuration / callCount : 0;
        }
        
        public double getErrorRate() {
            return callCount > 0 ? (double) errorCount / callCount * 100 : 0;
        }
    }
    
    /**
     * Total statistics
     */
    public static class TotalStats {
        public final int totalCalls;
        public final int totalTokens;
        public final long totalDuration;
        public final long errorCount;
        
        public TotalStats(int totalCalls, int totalTokens, long totalDuration, long errorCount) {
            this.totalCalls = totalCalls;
            this.totalTokens = totalTokens;
            this.totalDuration = totalDuration;
            this.errorCount = errorCount;
        }
        
        public double getAverageLatency() {
            return totalCalls > 0 ? (double) totalDuration / totalCalls : 0;
        }
        
        public double getErrorRate() {
            return totalCalls > 0 ? (double) errorCount / totalCalls * 100 : 0;
        }
    }
}
