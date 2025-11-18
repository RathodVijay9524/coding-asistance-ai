package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OutputMergerTest {

    private OutputMerger merger;

    @BeforeEach
    void setUp() {
        merger = new OutputMerger();
    }

    @Test
    @DisplayName("mergeOutputs should merge primary and additional insights and compute average quality")
    void mergeOutputs_basic() {
        OutputMerger.BrainOutput o1 = new OutputMerger.BrainOutput("BrainA", "Main answer.", 0.9);
        OutputMerger.BrainOutput o2 = new OutputMerger.BrainOutput("BrainB", "Another different perspective.", 0.8);
        OutputMerger.BrainOutput o3 = new OutputMerger.BrainOutput("BrainC", "Yet another view.", 0.7);

        OutputMerger.MergedResponse merged = merger.mergeOutputs(List.of(o1, o2, o3));

        assertThat(merged.content).contains("Main answer.");
        assertThat(merged.content).contains("Additional insight:");
        assertThat(merged.sources).containsExactlyInAnyOrder("BrainA", "BrainB", "BrainC");
        assertThat(merged.quality).isBetween(0.7, 0.95);
    }

    @Test
    @DisplayName("mergeWithConflictResolution should detect conflicts and still merge outputs")
    void mergeWithConflictResolution_conflicts() {
        OutputMerger.BrainOutput o1 = new OutputMerger.BrainOutput("BrainA", "Yes, it is correct.", 0.8);
        OutputMerger.BrainOutput o2 = new OutputMerger.BrainOutput("BrainB", "No, it is not correct.", 0.9);

        OutputMerger.MergedResponse merged = merger.mergeWithConflictResolution(List.of(o1, o2));

        assertThat(merged.content).isNotBlank();
        assertThat(merged.quality).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("combineInsights should append additional perspectives for dissimilar outputs")
    void combineInsights_appendsAdditionalPerspectives() {
        OutputMerger.BrainOutput o1 = new OutputMerger.BrainOutput("BrainA", "Primary content.", 0.9);
        OutputMerger.BrainOutput o2 = new OutputMerger.BrainOutput("BrainB", "Completely different content with other words.", 0.8);

        String combined = merger.combineInsights(List.of(o1, o2));

        assertThat(combined).contains("Primary content.");
        assertThat(combined).contains("Additional perspective:");
    }

    @Test
    @DisplayName("createUnifiedResponse should wrap merged content with user metadata")
    void createUnifiedResponse_wrapsMetadata() {
        OutputMerger.BrainOutput o1 = new OutputMerger.BrainOutput("BrainA", "Answer.", 0.9);

        OutputMerger.UnifiedResponse response = merger.createUnifiedResponse(List.of(o1), "user123");

        assertThat(response.userId).isEqualTo("user123");
        assertThat(response.content).contains("Answer.");
        assertThat(response.sources).contains("BrainA");
        assertThat(response.quality).isGreaterThan(0.0);
    }
}
