package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

/**
 * ⚡ Async Execution
 * 
 * Non-blocking code analysis:
 * - Non-blocking code analysis
 * - Parallel tool execution
 * - Background indexing
 * - Future-based results
 */
@Component
public class AsyncExecution {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncExecution.class);
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2);
    
    /**
     * Async code analysis
     */
    @Async
    public CompletableFuture<String> analyzeCodeAsync(String code, String language) {
        logger.debug("⚡ Starting async code analysis");
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(100); // Simulate analysis
                logger.debug("✅ Async analysis complete");
                return "Analysis result for: " + language;
            } catch (InterruptedException e) {
                logger.error("❌ Async analysis interrupted", e);
                return "Analysis failed";
            }
        }, executorService);
    }
    
    /**
     * Parallel tool execution
     */
    public CompletableFuture<List<String>> executeToolsParallel(List<String> tools) {
        logger.debug("⚡ Executing {} tools in parallel", tools.size());
        
        List<CompletableFuture<String>> futures = new ArrayList<>();
        
        for (String tool : tools) {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                logger.debug("  Executing tool: {}", tool);
                try {
                    Thread.sleep(50); // Simulate tool execution
                    return "Result from " + tool;
                } catch (InterruptedException e) {
                    logger.error("❌ Tool execution interrupted: {}", tool);
                    return "Failed: " + tool;
                }
            }, executorService);
            
            futures.add(future);
        }
        
        // Wait for all to complete
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<String> results = new ArrayList<>();
                    for (CompletableFuture<String> future : futures) {
                        results.add(future.join());
                    }
                    logger.debug("✅ All {} tools executed", tools.size());
                    return results;
                });
    }
    
    /**
     * Background indexing
     */
    @Async
    public CompletableFuture<Void> indexInBackground(String data, String indexName) {
        logger.debug("⚡ Starting background indexing: {}", indexName);
        
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(200); // Simulate indexing
                logger.debug("✅ Background indexing complete: {}", indexName);
            } catch (InterruptedException e) {
                logger.error("❌ Background indexing interrupted: {}", indexName);
            }
        }, executorService);
    }
    
    /**
     * Scheduled task execution
     */
    public ScheduledFuture<?> scheduleTask(Runnable task, long delayMs) {
        logger.debug("⏰ Scheduling task with delay: {}ms", delayMs);
        
        return scheduledExecutor.schedule(() -> {
            logger.debug("⚡ Executing scheduled task");
            task.run();
            logger.debug("✅ Scheduled task complete");
        }, delayMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Recurring task execution
     */
    public ScheduledFuture<?> scheduleRecurring(Runnable task, long initialDelayMs, long periodMs) {
        logger.debug("🔄 Scheduling recurring task: initial={}ms, period={}ms", initialDelayMs, periodMs);
        
        return scheduledExecutor.scheduleAtFixedRate(() -> {
            logger.debug("⚡ Executing recurring task");
            task.run();
            logger.debug("✅ Recurring task complete");
        }, initialDelayMs, periodMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Wait for async result with timeout
     */
    public <T> T waitForResult(CompletableFuture<T> future, long timeoutMs) {
        try {
            logger.debug("⏳ Waiting for result (timeout: {}ms)", timeoutMs);
            T result = future.get(timeoutMs, TimeUnit.MILLISECONDS);
            logger.debug("✅ Result received");
            return result;
        } catch (TimeoutException e) {
            logger.error("❌ Result timeout after {}ms", timeoutMs);
            future.cancel(true);
            return null;
        } catch (Exception e) {
            logger.error("❌ Error waiting for result: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Combine multiple async results
     */
    public <T> CompletableFuture<List<T>> combineAsync(List<CompletableFuture<T>> futures) {
        logger.debug("⚡ Combining {} async results", futures.size());
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<T> results = new ArrayList<>();
                    for (CompletableFuture<T> future : futures) {
                        results.add(future.join());
                    }
                    logger.debug("✅ Combined {} results", results.size());
                    return results;
                });
    }
    
    /**
     * Get executor stats
     */
    public ExecutorStats getStats() {
        ExecutorStats stats = new ExecutorStats();
        
        if (executorService instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) executorService;
            stats.activeThreads = tpe.getActiveCount();
            stats.poolSize = tpe.getPoolSize();
            stats.corePoolSize = tpe.getCorePoolSize();
            stats.maxPoolSize = tpe.getMaximumPoolSize();
            stats.tasksCompleted = tpe.getCompletedTaskCount();
            stats.tasksPending = tpe.getTaskCount() - tpe.getCompletedTaskCount();
        }
        
        return stats;
    }
    
    /**
     * Shutdown executors
     */
    public void shutdown() {
        logger.debug("🛑 Shutting down async executors");
        
        executorService.shutdown();
        scheduledExecutor.shutdown();
        
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            if (!scheduledExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
            logger.debug("✅ Async executors shut down");
        } catch (InterruptedException e) {
            logger.error("❌ Error shutting down executors", e);
            executorService.shutdownNow();
            scheduledExecutor.shutdownNow();
        }
    }
    
    /**
     * Executor Statistics DTO
     */
    public static class ExecutorStats {
        public int activeThreads;
        public int poolSize;
        public int corePoolSize;
        public int maxPoolSize;
        public long tasksCompleted;
        public long tasksPending;
        
        @Override
        public String toString() {
            return String.format("ExecutorStats{active=%d, pool=%d/%d, completed=%d, pending=%d}",
                    activeThreads, poolSize, maxPoolSize, tasksCompleted, tasksPending);
        }
    }
}
