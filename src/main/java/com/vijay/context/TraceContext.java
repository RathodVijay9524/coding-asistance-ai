package com.vijay.context;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 🔍 TraceContext - Request Tracing for Debugging
 * 
 * This provides a unique trace ID for each request that flows through all brains.
 * Makes it easy to correlate logs across the entire request lifecycle.
 * 
 * Usage:
 * - ChatService: TraceContext.initialize()
 * - All brains: String traceId = TraceContext.getTraceId()
 * - Logging: logger.info("[{}] Message", TraceContext.getTraceId())
 * - Cleanup: TraceContext.clear()
 * 
 * Example Log Output:
 * [550e8400-e29b-41d4-a716-446655440000] SmartFinder: Found 3 tools
 * [550e8400-e29b-41d4-a716-446655440000] Conductor: Approved 2 tools
 * [550e8400-e29b-41d4-a716-446655440000] ToolCallAdvisor: Executing tools
 * 
 * Benefits:
 * ✅ Easy debugging
 * ✅ Request correlation
 * ✅ Performance monitoring
 * ✅ Error tracking
 */
@Component
public class TraceContext {
    
    private static final ThreadLocal<String> traceId = 
        ThreadLocal.withInitial(() -> null);
    
    private static final ThreadLocal<Long> startTime = 
        ThreadLocal.withInitial(() -> null);
    
    /**
     * Initialize trace context (call at start of request)
     */
    public static void initialize() {
        traceId.set(UUID.randomUUID().toString());
        startTime.set(System.currentTimeMillis());
    }
    
    /**
     * Initialize with custom trace ID
     */
    public static void initialize(String customTraceId) {
        traceId.set(customTraceId);
        startTime.set(System.currentTimeMillis());
    }
    
    /**
     * Get the trace ID
     */
    public static String getTraceId() {
        String id = traceId.get();
        return id != null ? id : "NO_TRACE_ID";
    }
    
    /**
     * Get elapsed time in milliseconds
     */
    public static long getElapsedTime() {
        Long start = startTime.get();
        if (start == null) {
            return 0;
        }
        return System.currentTimeMillis() - start;
    }
    
    /**
     * Get elapsed time formatted
     */
    public static String getElapsedTimeFormatted() {
        long elapsed = getElapsedTime();
        if (elapsed < 1000) {
            return elapsed + "ms";
        } else if (elapsed < 60000) {
            return String.format("%.2fs", elapsed / 1000.0);
        } else {
            return String.format("%.2fm", elapsed / 60000.0);
        }
    }
    
    /**
     * Clear the trace context (call at end of request)
     */
    public static void clear() {
        traceId.remove();
        startTime.remove();
    }
    
    /**
     * Check if trace context is initialized
     */
    public static boolean isInitialized() {
        return traceId.get() != null;
    }
    
    /**
     * Get trace info for logging
     */
    public static String getTraceInfo() {
        return String.format("[%s] (elapsed: %s)", getTraceId(), getElapsedTimeFormatted());
    }
}
