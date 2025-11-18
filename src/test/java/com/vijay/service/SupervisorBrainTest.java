package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SupervisorBrainTest {

    private SupervisorBrain supervisor;

    @BeforeEach
    void setUp() {
        supervisor = new SupervisorBrain();
    }

    @Test
    @DisplayName("recordBrainOutput and mergeOutputs should combine top outputs and compute average quality")
    void mergeOutputs_basic() {
        supervisor.initializeConversation("user1", "conv1");

        supervisor.recordBrainOutput("conv1", "BrainA", "Output A", 0.8);
        supervisor.recordBrainOutput("conv1", "BrainB", "Output B", 0.6);
        supervisor.recordBrainOutput("conv1", "BrainC", "Output C", 0.9);

        SupervisorBrain.MergedOutput merged = supervisor.mergeOutputs("conv1");

        assertThat(merged.content).contains("Output A");
        assertThat(merged.content).contains("Output B");
        assertThat(merged.content).contains("Output C");
        assertThat(merged.quality).isBetween(0.6, 0.95);
    }

    @Test
    @DisplayName("shouldReevaluate should trigger only up to max cycles when quality is low")
    void shouldReevaluate_respectsMaxCycles() {
        supervisor.initializeConversation("user1", "conv2");

        boolean first = supervisor.shouldReevaluate("conv2", 0.5);
        boolean second = supervisor.shouldReevaluate("conv2", 0.4);
        boolean third = supervisor.shouldReevaluate("conv2", 0.3);
        boolean fourth = supervisor.shouldReevaluate("conv2", 0.2);

        assertThat(first).isTrue();
        assertThat(second).isTrue();
        assertThat(third).isTrue();
        assertThat(fourth).isFalse();
    }

    @Test
    @DisplayName("checkConsistency should report low similarity inconsistencies and average similarity")
    void checkConsistency_reportsInconsistencies() {
        supervisor.initializeConversation("user1", "conv3");

        supervisor.recordBrainOutput("conv3", "BrainA", "The result is 10.", 0.8);
        supervisor.recordBrainOutput("conv3", "BrainB", "Completely different answer", 0.9);

        SupervisorBrain.ConsistencyReport report = supervisor.checkConsistency("conv3");

        assertThat(report.averageSimilarity).isLessThan(1.0);
        assertThat(report.inconsistencies).isNotEmpty();
        assertThat(report.isConsistent()).isFalse();
    }

    @Test
    @DisplayName("BrainPerformance should track execution count and quality stats")
    void brainPerformance_tracksStats() {
        supervisor.initializeConversation("user1", "conv4");

        supervisor.recordBrainOutput("conv4", "BrainX", "out1", 0.5);
        supervisor.recordBrainOutput("conv4", "BrainX", "out2", 0.9);

        SupervisorBrain.BrainPerformance perf = supervisor.getBrainPerformance("BrainX");

        assertThat(perf).isNotNull();
        assertThat(perf.getExecutionCount()).isEqualTo(2);
        assertThat(perf.getAverageQuality()).isBetween(0.5, 0.9);
        assertThat(perf.getMinQuality()).isEqualTo(0.5);
        assertThat(perf.getMaxQuality()).isEqualTo(0.9);
    }

    @Test
    @DisplayName("cleanupOldConversations should remove conversations older than maxAgeMs")
    void cleanupOldConversations_removesOld() throws Exception {
        supervisor.initializeConversation("user1", "conv5");
        Thread.sleep(5);

        supervisor.cleanupOldConversations(0L);

        assertThat(supervisor.getConversationState("conv5")).isNull();
    }
}
