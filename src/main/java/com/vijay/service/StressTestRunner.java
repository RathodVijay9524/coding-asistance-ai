package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 🧠 STRESS TEST RUNNER - Phase 10
 * 
 * Purpose: Run stress tests (100 req/sec)
 * 
 * Responsibilities:
 * - Generate test queries
 * - Send concurrent requests
 * - Track success/failure rates
 * - Measure throughput
 * - Generate stress test report
 */
@Service
public class StressTestRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(StressTestRunner.class);
    
    private final List<String> testQueries = Arrays.asList(
        "How do I use lambdas in Java?",
        "Explain Spring Boot",
        "What is a REST API?",
        "How do I debug code?",
        "What are microservices?",
        "Explain dependency injection",
        "How do I optimize performance?",
        "What is Docker?",
        "Explain Kubernetes",
        "How do I write unit tests?"
    );
    
    /**
     * Run stress test
     */
    public StressTestResult runStressTest(int requestsPerSecond, int durationSeconds) {
        logger.info("🚀 Starting stress test: {} req/sec for {} seconds", requestsPerSecond, durationSeconds);
        
        StressTestResult result = new StressTestResult();
        result.requestsPerSecond = requestsPerSecond;
        result.durationSeconds = durationSeconds;
        result.startTime = System.currentTimeMillis();
        
        ExecutorService executor = Executors.newFixedThreadPool(requestsPerSecond);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<Long> latencies = Collections.synchronizedList(new ArrayList<>());
        
        // Calculate total requests
        int totalRequests = requestsPerSecond * durationSeconds;
        
        // Submit requests
        for (int i = 0; i < totalRequests; i++) {
            executor.submit(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    
                    // Simulate request
                    String query = testQueries.get((int)(Math.random() * testQueries.size()));
                    simulateRequest(query);
                    
                    long latency = System.currentTimeMillis() - startTime;
                    latencies.add(latency);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                }
            });
            
            // Rate limiting
            if ((i + 1) % requestsPerSecond == 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        // Shutdown executor
        executor.shutdown();
        try {
            executor.awaitTermination(durationSeconds + 60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Calculate results
        result.endTime = System.currentTimeMillis();
        result.totalRequests = totalRequests;
        result.successCount = successCount.get();
        result.failureCount = failureCount.get();
        result.successRate = totalRequests > 0 ? (double) successCount.get() / totalRequests * 100 : 0;
        result.errorRate = totalRequests > 0 ? (double) failureCount.get() / totalRequests * 100 : 0;
        result.throughput = (double) successCount.get() / ((result.endTime - result.startTime) / 1000.0);
        
        // Calculate latency stats
        if (!latencies.isEmpty()) {
            Collections.sort(latencies);
            result.minLatency = latencies.get(0);
            result.maxLatency = latencies.get(latencies.size() - 1);
            result.avgLatency = latencies.stream().mapToLong(Long::longValue).average().orElse(0);
            result.p95Latency = latencies.get((int)(latencies.size() * 0.95));
            result.p99Latency = latencies.get((int)(latencies.size() * 0.99));
        }
        
        logger.info("✅ Stress test completed: {} success, {} failed, {:.1f}% success rate", 
            successCount.get(), failureCount.get(), result.successRate);
        
        return result;
    }
    
    /**
     * Simulate request
     */
    private void simulateRequest(String query) throws InterruptedException {
        // Simulate processing
        Thread.sleep((long)(Math.random() * 100));
    }
    
    /**
     * Generate test queries
     */
    public List<String> generateTestQueries(int count) {
        List<String> queries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            queries.add(testQueries.get(i % testQueries.size()));
        }
        return queries;
    }
    
    // ============ Inner Classes ============
    
    /**
     * Stress test result
     */
    public static class StressTestResult {
        public int requestsPerSecond;
        public int durationSeconds;
        public long startTime;
        public long endTime;
        public int totalRequests;
        public int successCount;
        public int failureCount;
        public double successRate;
        public double errorRate;
        public double throughput;
        public long minLatency;
        public long maxLatency;
        public double avgLatency;
        public long p95Latency;
        public long p99Latency;
        
        @Override
        public String toString() {
            return String.format(
                "Stress Test Result:\n" +
                "  Total Requests: %d\n" +
                "  Success: %d (%.1f%%)\n" +
                "  Failures: %d (%.1f%%)\n" +
                "  Throughput: %.1f req/sec\n" +
                "  Latency - Min: %dms, Avg: %.1fms, P95: %dms, P99: %dms, Max: %dms",
                totalRequests, successCount, successRate, failureCount, errorRate,
                throughput, minLatency, avgLatency, p95Latency, p99Latency, maxLatency
            );
        }
    }
}
