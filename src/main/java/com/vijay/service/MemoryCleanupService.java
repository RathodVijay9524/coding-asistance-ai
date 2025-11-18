package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 🧹 Memory Cleanup Service
 * 
 * Automatically cleans up old conversation sessions and optimizes memory usage.
 * Runs periodically to prevent memory leaks and maintain system performance.
 */
@Service
public class MemoryCleanupService {
    
    private static final Logger logger = LoggerFactory.getLogger(MemoryCleanupService.class);
    
    private final ConversationMemoryManager memoryManager;
    
    public MemoryCleanupService(ConversationMemoryManager memoryManager) {
        this.memoryManager = memoryManager;
    }
    
    /**
     * Clean up old sessions every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour = 3,600,000 milliseconds
    public void cleanupOldSessions() {
        logger.info("🧹 Starting scheduled memory cleanup...");
        
        try {
            memoryManager.cleanupOldSessions();
            logger.info("✅ Memory cleanup completed successfully");
        } catch (Exception e) {
            logger.error("❌ Memory cleanup failed", e);
        }
    }
    
    /**
     * Log memory statistics every 30 minutes
     */
    @Scheduled(fixedRate = 1800000) // 30 minutes = 1,800,000 milliseconds
    public void logMemoryStatistics() {
        logger.info("📊 Memory Statistics:");
        logger.info("   🧠 Active sessions: {}", getActiveSessionCount());
        logger.info("   👥 Total users: {}", getTotalUserCount());
        logger.info("   💾 Memory usage: {}MB", getMemoryUsageMB());
    }
    
    private int getActiveSessionCount() {
        // This would be implemented by adding a method to ConversationMemoryManager
        // For now, return a placeholder
        return 0;
    }
    
    private int getTotalUserCount() {
        // This would be implemented by adding a method to ConversationMemoryManager
        // For now, return a placeholder
        return 0;
    }
    
    private long getMemoryUsageMB() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
    }
}
