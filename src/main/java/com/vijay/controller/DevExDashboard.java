package com.vijay.controller;

import com.vijay.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 🧠 DEVEX DASHBOARD - Phase 10
 * 
 * Purpose: REST endpoints for DevEx features
 * 
 * Endpoints:
 * - GET /api/devex/logs - Get API logs
 * - GET /api/devex/timeline - Get timeline
 * - GET /api/devex/profile - Get profiling stats
 * - GET /api/devex/stress-test - Run stress test
 * - GET /api/devex/cache - Get cache stats
 */
@RestController
@RequestMapping("/api/devex")
public class DevExDashboard {
    
    private static final Logger logger = LoggerFactory.getLogger(DevExDashboard.class);
    
    private final APILogger apiLogger;
    private final TimelineVisualizer timelineVisualizer;
    private final PerformanceProfiler performanceProfiler;
    private final StressTestRunner stressTestRunner;
    private final CacheManager cacheManager;
    
    public DevExDashboard(APILogger apiLogger,
                         TimelineVisualizer timelineVisualizer,
                         PerformanceProfiler performanceProfiler,
                         StressTestRunner stressTestRunner,
                         CacheManager cacheManager) {
        this.apiLogger = apiLogger;
        this.timelineVisualizer = timelineVisualizer;
        this.performanceProfiler = performanceProfiler;
        this.stressTestRunner = stressTestRunner;
        this.cacheManager = cacheManager;
    }
    
    /**
     * Get API logs
     */
    @GetMapping("/logs")
    public Map<String, Object> getLogs(@RequestParam(defaultValue = "100") int count) {
        logger.info("📊 DevEx: Getting API logs (count: {})", count);
        
        Map<String, Object> response = new HashMap<>();
        response.put("logs", apiLogger.getRecentLogs(count));
        response.put("stats", apiLogger.getAllStats());
        response.put("total_stats", apiLogger.getTotalStats());
        
        return response;
    }
    
    /**
     * Get timeline
     */
    @GetMapping("/timeline")
    public Map<String, Object> getTimeline(@RequestParam(defaultValue = "json") String format) {
        logger.info("⏱️ DevEx: Getting timeline (format: {})", format);
        
        Map<String, Object> response = new HashMap<>();
        
        if ("csv".equalsIgnoreCase(format)) {
            response.put("data", timelineVisualizer.exportToCSV());
            response.put("format", "csv");
        } else {
            response.put("data", timelineVisualizer.exportToJSON());
            response.put("format", "json");
        }
        
        response.put("stats", timelineVisualizer.getStatistics());
        
        return response;
    }
    
    /**
     * Get performance profile
     */
    @GetMapping("/profile")
    public Map<String, Object> getProfile() {
        logger.info("📈 DevEx: Getting performance profile");
        
        Map<String, Object> response = new HashMap<>();
        response.put("memory", performanceProfiler.getMemoryStats());
        response.put("cpu", performanceProfiler.getCPUStats());
        response.put("latency", performanceProfiler.getLatencyStats());
        response.put("report", performanceProfiler.generateReport());
        
        return response;
    }
    
    /**
     * Run stress test
     */
    @PostMapping("/stress-test")
    public Map<String, Object> runStressTest(
            @RequestParam(defaultValue = "100") int requestsPerSecond,
            @RequestParam(defaultValue = "10") int durationSeconds) {
        
        logger.info("🚀 DevEx: Running stress test ({} req/sec for {} seconds)", 
            requestsPerSecond, durationSeconds);
        
        StressTestRunner.StressTestResult result = stressTestRunner.runStressTest(
            requestsPerSecond, durationSeconds);
        
        Map<String, Object> response = new HashMap<>();
        response.put("result", result);
        response.put("status", "completed");
        
        return response;
    }
    
    /**
     * Get cache statistics
     */
    @GetMapping("/cache")
    public Map<String, Object> getCacheStats() {
        logger.info("💾 DevEx: Getting cache statistics");
        
        Map<String, Object> response = new HashMap<>();
        response.put("stats", cacheManager.getStats());
        
        return response;
    }
    
    /**
     * Clear cache
     */
    @DeleteMapping("/cache")
    public Map<String, String> clearCache() {
        logger.info("🧹 DevEx: Clearing cache");
        
        cacheManager.clear();
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Cache cleared");
        
        return response;
    }
    
    /**
     * Get all DevEx stats
     */
    @GetMapping("/stats")
    public Map<String, Object> getAllStats() {
        logger.info("📊 DevEx: Getting all statistics");
        
        Map<String, Object> response = new HashMap<>();
        response.put("api_logs", apiLogger.getTotalStats());
        response.put("timeline", timelineVisualizer.getStatistics());
        response.put("memory", performanceProfiler.getMemoryStats());
        response.put("cpu", performanceProfiler.getCPUStats());
        response.put("latency", performanceProfiler.getLatencyStats());
        response.put("cache", cacheManager.getStats());
        
        return response;
    }
    
    /**
     * Health check
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        return response;
    }
}
