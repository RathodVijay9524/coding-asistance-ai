package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PerformanceProfilerTest {

    private PerformanceProfiler profiler;

    @BeforeEach
    void setUp() {
        profiler = new PerformanceProfiler();
    }

    @Test
    @DisplayName("startProfiling, recordLatency and stats methods should produce a report")
    void profilingAndStats() {
        profiler.startProfiling();

        profiler.recordLatency(10);
        profiler.recordLatency(20);
        profiler.recordLatency(30);

        PerformanceProfiler.MemoryStats mem = profiler.getMemoryStats();
        PerformanceProfiler.CPUStats cpu = profiler.getCPUStats();
        PerformanceProfiler.LatencyStats latency = profiler.getLatencyStats();

        assertThat(mem.heapUsed).isGreaterThan(0L);
        assertThat(cpu.threadCount).isGreaterThan(0);
        assertThat(latency.max).isGreaterThanOrEqualTo(latency.min);

        String report = profiler.generateReport();
        assertThat(report).contains("PERFORMANCE REPORT");
        assertThat(report).contains("Memory:");
        assertThat(report).contains("CPU:");
        assertThat(report).contains("Latency:");

        profiler.stopProfiling();
    }

    @Test
    @DisplayName("getLatencyStats should handle empty latency list")
    void latencyStats_empty() {
        PerformanceProfiler.LatencyStats latency = profiler.getLatencyStats();
        assertThat(latency.min).isEqualTo(0L);
        assertThat(latency.max).isEqualTo(0L);
        assertThat(latency.avg).isEqualTo(0.0);
    }
}
