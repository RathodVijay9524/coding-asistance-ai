package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class APILoggerTest {

    private APILogger apiLogger;

    @BeforeEach
    void setUp() {
        apiLogger = new APILogger();
    }

    @Test
    @DisplayName("logAdvisorCall should update advisor stats and total stats")
    void logAdvisorCall_updatesStats() {
        apiLogger.logAdvisorCall("advisorA", 100, 50, "OK");
        apiLogger.logAdvisorCall("advisorA", 200, 30, "ERROR timeout");

        APILogger.AdvisorStats stats = apiLogger.getAdvisorStats("advisorA");
        assertThat(stats).isNotNull();
        assertThat(stats.callCount).isEqualTo(2);
        assertThat(stats.totalTokens).isEqualTo(80);
        assertThat(stats.errorCount).isEqualTo(1);
        assertThat(stats.getAverageDuration()).isBetween(100.0, 200.0);
        assertThat(stats.getErrorRate()).isGreaterThan(0.0);

        APILogger.TotalStats total = apiLogger.getTotalStats();
        assertThat(total.totalCalls).isEqualTo(2);
        assertThat(total.totalTokens).isEqualTo(80);
        assertThat(total.getAverageLatency()).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("logModelCall and logError should create log entries and track errors")
    void logModelCall_andError() {
        apiLogger.logModelCall("gpt-4", 120, 300, "OK");
        apiLogger.logError("advisorB", new RuntimeException("boom"));

        APILogger.TotalStats total = apiLogger.getTotalStats();
        assertThat(total.totalCalls).isEqualTo(2);
        assertThat(total.errorCount).isEqualTo(1);

        List<APILogger.APILogEntry> recent = apiLogger.getRecentLogs(10);
        assertThat(recent).hasSize(2);
        assertThat(recent.get(0).component).isEqualTo("MODEL:gpt-4");
        assertThat(recent.get(1).component).isEqualTo("advisorB");
    }

    @Test
    @DisplayName("clearLogs should reset stats and logs")
    void clearLogs_resets() {
        apiLogger.logAdvisorCall("advisorA", 50, 10, "OK");
        assertThat(apiLogger.getTotalStats().totalCalls).isEqualTo(1);

        apiLogger.clearLogs();

        APILogger.TotalStats total = apiLogger.getTotalStats();
        assertThat(total.totalCalls).isEqualTo(0);
        assertThat(apiLogger.getAllStats()).isEmpty();
        assertThat(apiLogger.getRecentLogs(5)).isEmpty();
    }
}
