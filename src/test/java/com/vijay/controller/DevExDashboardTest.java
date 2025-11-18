package com.vijay.controller;

import com.vijay.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DevExDashboard.class)
class DevExDashboardTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private APILogger apiLogger;
    @MockBean private TimelineVisualizer timelineVisualizer;
    @MockBean private PerformanceProfiler performanceProfiler;
    @MockBean private StressTestRunner stressTestRunner;
    @MockBean private CacheManager cacheManager;

    @Test
    @DisplayName("GET /api/devex/logs should return logs and stats")
    void getLogs_basic() throws Exception {
        when(apiLogger.getRecentLogs(anyInt())).thenReturn(Collections.emptyList());
        when(apiLogger.getAllStats()).thenReturn(Collections.emptyMap());
        when(apiLogger.getTotalStats()).thenReturn(new APILogger.TotalStats(0, 0, 0L, 0L));

        mockMvc.perform(get("/api/devex/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logs").isArray())
                .andExpect(jsonPath("$.stats").isMap())
                .andExpect(jsonPath("$.total_stats.totalCalls").value(0));
    }

    @Test
    @DisplayName("GET /api/devex/timeline should default to JSON format")
    void getTimeline_json() throws Exception {
        TimelineVisualizer.TimelineStatistics stats = new TimelineVisualizer.TimelineStatistics(3, 120L, 40.0, 70L);
        when(timelineVisualizer.exportToJSON()).thenReturn("[{\"advisor\":\"A\"}]");
        when(timelineVisualizer.getStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/devex/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.format").value("json"))
                .andExpect(jsonPath("$.data").value("[{\"advisor\":\"A\"}]"))
                .andExpect(jsonPath("$.stats.eventCount").value(3))
                .andExpect(jsonPath("$.stats.totalDuration").value(120));
    }

    @Test
    @DisplayName("GET /api/devex/timeline?format=csv should return CSV data")
    void getTimeline_csv() throws Exception {
        TimelineVisualizer.TimelineStatistics stats = new TimelineVisualizer.TimelineStatistics(2, 80L, 25.0, 60L);
        when(timelineVisualizer.exportToCSV()).thenReturn("advisor,order\nA,1\n");
        when(timelineVisualizer.getStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/devex/timeline").param("format", "csv"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.format").value("csv"))
                .andExpect(jsonPath("$.data").value("advisor,order\nA,1\n"))
                .andExpect(jsonPath("$.stats.eventCount").value(2))
                .andExpect(jsonPath("$.stats.maxDuration").value(60));
    }

    @Test
    @DisplayName("GET /api/devex/profile should return profiler data")
    void getProfile_basic() throws Exception {
        PerformanceProfiler.MemoryStats memoryStats = new PerformanceProfiler.MemoryStats(1000L, 2000L, 500L, 5L, 10L);
        PerformanceProfiler.CPUStats cpuStats = new PerformanceProfiler.CPUStats(12.5, 8);
        PerformanceProfiler.LatencyStats latencyStats = new PerformanceProfiler.LatencyStats(5L, 50L, 15.0, 30L, 45L);

        when(performanceProfiler.getMemoryStats()).thenReturn(memoryStats);
        when(performanceProfiler.getCPUStats()).thenReturn(cpuStats);
        when(performanceProfiler.getLatencyStats()).thenReturn(latencyStats);
        when(performanceProfiler.generateReport()).thenReturn("report");

        mockMvc.perform(get("/api/devex/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memory.heapUsed").value(1000))
                .andExpect(jsonPath("$.cpu.threadCount").value(8))
                .andExpect(jsonPath("$.latency.max").value(50))
                .andExpect(jsonPath("$.report").value("report"));
    }

    @Test
    @DisplayName("POST /api/devex/stress-test should return stress test result")
    void runStressTest_basic() throws Exception {
        StressTestRunner.StressTestResult result = new StressTestRunner.StressTestResult();
        result.totalRequests = 1000;
        result.successCount = 950;
        result.failureCount = 50;
        result.successRate = 95.0;
        result.errorRate = 5.0;

        when(stressTestRunner.runStressTest(100, 10)).thenReturn(result);

        mockMvc.perform(post("/api/devex/stress-test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("completed"))
                .andExpect(jsonPath("$.result.totalRequests").value(1000))
                .andExpect(jsonPath("$.result.successRate").value(95.0));
    }

    @Test
    @DisplayName("GET /api/devex/cache should return cache stats")
    void getCacheStats_basic() throws Exception {
        CacheManager.CacheStats stats = new CacheManager.CacheStats(5, 100, 20L, 10L);
        when(cacheManager.getStats()).thenReturn(stats);

        mockMvc.perform(get("/api/devex/cache"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stats.size").value(5))
                .andExpect(jsonPath("$.stats.maxSize").value(100))
                .andExpect(jsonPath("$.stats.hits").value(20));
    }

    @Test
    @DisplayName("DELETE /api/devex/cache should clear cache")
    void clearCache_basic() throws Exception {
        mockMvc.perform(delete("/api/devex/cache"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Cache cleared"));

        verify(cacheManager).clear();
    }

    @Test
    @DisplayName("GET /api/devex/stats should aggregate all stats")
    void getAllStats_basic() throws Exception {
        APILogger.TotalStats apiTotals = new APILogger.TotalStats(10, 500, 2000L, 1L);
        TimelineVisualizer.TimelineStatistics timelineStats = new TimelineVisualizer.TimelineStatistics(4, 150L, 37.5, 80L);
        PerformanceProfiler.MemoryStats memoryStats = new PerformanceProfiler.MemoryStats(1500L, 4000L, 700L, 3L, 5L);
        PerformanceProfiler.CPUStats cpuStats = new PerformanceProfiler.CPUStats(20.0, 12);
        PerformanceProfiler.LatencyStats latencyStats = new PerformanceProfiler.LatencyStats(4L, 60L, 22.0, 40L, 55L);
        CacheManager.CacheStats cacheStats = new CacheManager.CacheStats(7, 100, 30L, 5L);

        when(apiLogger.getTotalStats()).thenReturn(apiTotals);
        when(timelineVisualizer.getStatistics()).thenReturn(timelineStats);
        when(performanceProfiler.getMemoryStats()).thenReturn(memoryStats);
        when(performanceProfiler.getCPUStats()).thenReturn(cpuStats);
        when(performanceProfiler.getLatencyStats()).thenReturn(latencyStats);
        when(cacheManager.getStats()).thenReturn(cacheStats);

        mockMvc.perform(get("/api/devex/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.api_logs.totalCalls").value(10))
                .andExpect(jsonPath("$.timeline.maxDuration").value(80))
                .andExpect(jsonPath("$.memory.heapMax").value(4000))
                .andExpect(jsonPath("$.cpu.threadCount").value(12))
                .andExpect(jsonPath("$.latency.p99").value(55))
                .andExpect(jsonPath("$.cache.size").value(7));
    }

    @Test
    @DisplayName("GET /api/devex/health should return ok")
    void health_basic() throws Exception {
        mockMvc.perform(get("/api/devex/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }
}
