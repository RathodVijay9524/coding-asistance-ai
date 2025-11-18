package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StressTestRunnerTest {

    private StressTestRunner runner;

    @BeforeEach
    void setUp() {
        runner = new StressTestRunner();
    }

    @Test
    @DisplayName("generateTestQueries should cycle through predefined queries")
    void generateTestQueries_cycles() {
        List<String> queries = runner.generateTestQueries(15);

        assertThat(queries).hasSize(15);
        assertThat(queries).contains("How do I use lambdas in Java?");
        assertThat(queries).contains("How do I write unit tests?");
    }

    @Test
    @DisplayName("runStressTest should produce a reasonable result without throwing")
    void runStressTest_basic() {
        StressTestRunner.StressTestResult result = runner.runStressTest(2, 1);

        assertThat(result.totalRequests).isEqualTo(2);
        assertThat(result.successCount + result.failureCount).isEqualTo(2);
        assertThat(result.successRate + result.errorRate).isBetween(99.0, 101.0);
        assertThat(result.toString()).contains("Stress Test Result:");
    }
}
