package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.management.*;
import java.util.*;

/**
 * 🧠 PERFORMANCE PROFILER - Phase 10
 * 
 * Purpose: Profile system performance (CPU, memory, GC)
 * 
 * Responsibilities:
 * - Track CPU usage
 * - Track memory usage
 * - Track GC pauses
 * - Track response times
 * - Generate performance reports
 */
@Service
public class PerformanceProfiler {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceProfiler.class);
    
    private final RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private final List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    
    private long profilingStartTime = 0;
    private long initialCPUTime = 0;
    private final List<Long> latencies = Collections.synchronizedList(new ArrayList<>());
    
    /**
     * Start profiling
     */
    public void startProfiling() {
        profilingStartTime = System.currentTimeMillis();
        initialCPUTime = threadBean.getCurrentThreadCpuTime();
        latencies.clear();
        logger.info("🚀 Performance profiling started");
    }
    
    /**
     * Stop profiling
     */
    public void stopProfiling() {
        long duration = System.currentTimeMillis() - profilingStartTime;
        logger.info("⏹️ Performance profiling stopped (duration: {}ms)", duration);
    }
    
    /**
     * Record latency
     */
    public void recordLatency(long latencyMs) {
        latencies.add(latencyMs);
    }
    
    /**
     * Get memory statistics
     */
    public MemoryStats getMemoryStats() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        
        long gcCollections = gcBeans.stream()
            .mapToLong(GarbageCollectorMXBean::getCollectionCount)
            .sum();
        
        long gcTime = gcBeans.stream()
            .mapToLong(GarbageCollectorMXBean::getCollectionTime)
            .sum();
        
        return new MemoryStats(
            heapUsage.getUsed(),
            heapUsage.getMax(),
            nonHeapUsage.getUsed(),
            gcCollections,
            gcTime
        );
    }
    
    /**
     * Get CPU statistics
     */
    public CPUStats getCPUStats() {
        long currentCPUTime = threadBean.getCurrentThreadCpuTime();
        long cpuTimeDiff = currentCPUTime - initialCPUTime;
        long duration = System.currentTimeMillis() - profilingStartTime;
        
        double processCPU = duration > 0 ? (cpuTimeDiff / 1_000_000.0) / duration * 100 : 0;
        int threadCount = threadBean.getThreadCount();
        
        return new CPUStats(processCPU, threadCount);
    }
    
    /**
     * Get latency statistics
     */
    public LatencyStats getLatencyStats() {
        if (latencies.isEmpty()) {
            return new LatencyStats(0, 0, 0, 0, 0);
        }
        
        List<Long> sorted = new ArrayList<>(latencies);
        Collections.sort(sorted);
        
        long min = sorted.get(0);
        long max = sorted.get(sorted.size() - 1);
        double avg = latencies.stream().mapToLong(Long::longValue).average().orElse(0);
        
        // Calculate percentiles
        long p50 = sorted.get((int)(sorted.size() * 0.50));
        long p95 = sorted.get((int)(sorted.size() * 0.95));
        long p99 = sorted.get((int)(sorted.size() * 0.99));
        
        return new LatencyStats(min, max, avg, p95, p99);
    }
    
    /**
     * Generate performance report
     */
    public String generateReport() {
        MemoryStats memStats = getMemoryStats();
        CPUStats cpuStats = getCPUStats();
        LatencyStats latencyStats = getLatencyStats();
        
        StringBuilder report = new StringBuilder();
        report.append("\n=== PERFORMANCE REPORT ===\n");
        report.append("\nMemory:\n");
        report.append(String.format("  Heap: %dMB / %dMB\n", 
            memStats.heapUsed / 1024 / 1024, memStats.heapMax / 1024 / 1024));
        report.append(String.format("  Non-Heap: %dMB\n", memStats.nonHeapUsed / 1024 / 1024));
        report.append(String.format("  GC Collections: %d\n", memStats.gcCollections));
        report.append(String.format("  GC Time: %dms\n", memStats.gcTime));
        
        report.append("\nCPU:\n");
        report.append(String.format("  Process CPU: %.1f%%\n", cpuStats.processCPU));
        report.append(String.format("  Thread Count: %d\n", cpuStats.threadCount));
        
        report.append("\nLatency:\n");
        report.append(String.format("  Min: %dms\n", latencyStats.min));
        report.append(String.format("  Max: %dms\n", latencyStats.max));
        report.append(String.format("  Avg: %.1fms\n", latencyStats.avg));
        report.append(String.format("  P95: %dms\n", latencyStats.p95));
        report.append(String.format("  P99: %dms\n", latencyStats.p99));
        report.append("\n");
        
        return report.toString();
    }
    
    // ============ Inner Classes ============
    
    /**
     * Memory statistics
     */
    public static class MemoryStats {
        public final long heapUsed;
        public final long heapMax;
        public final long nonHeapUsed;
        public final long gcCollections;
        public final long gcTime;
        
        public MemoryStats(long heapUsed, long heapMax, long nonHeapUsed, long gcCollections, long gcTime) {
            this.heapUsed = heapUsed;
            this.heapMax = heapMax;
            this.nonHeapUsed = nonHeapUsed;
            this.gcCollections = gcCollections;
            this.gcTime = gcTime;
        }
    }
    
    /**
     * CPU statistics
     */
    public static class CPUStats {
        public final double processCPU;
        public final int threadCount;
        
        public CPUStats(double processCPU, int threadCount) {
            this.processCPU = processCPU;
            this.threadCount = threadCount;
        }
    }
    
    /**
     * Latency statistics
     */
    public static class LatencyStats {
        public final long min;
        public final long max;
        public final double avg;
        public final long p95;
        public final long p99;
        
        public LatencyStats(long min, long max, double avg, long p95, long p99) {
            this.min = min;
            this.max = max;
            this.avg = avg;
            this.p95 = p95;
            this.p99 = p99;
        }
    }
}
