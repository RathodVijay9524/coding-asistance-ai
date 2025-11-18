package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TimelineVisualizerTest {

    private TimelineVisualizer visualizer;

    @BeforeEach
    void setUp() {
        visualizer = new TimelineVisualizer();
    }

    @Test
    @DisplayName("startAdvisor and endAdvisor should record events and compute durations")
    void startAndEndAdvisor_recordsEvents() throws Exception {
        visualizer.startAdvisor("AdvisorA", 0);
        Thread.sleep(5);
        visualizer.endAdvisor("AdvisorA");

        assertThat(visualizer.getTimeline()).hasSize(1);
        TimelineVisualizer.TimelineEvent event = visualizer.getTimeline().get(0);
        assertThat(event.advisorName).isEqualTo("AdvisorA");
        assertThat(event.durationMs).isGreaterThanOrEqualTo(0L);
    }

    @Test
    @DisplayName("exportToJSON and exportToCSV should include event data")
    void exportFormats_includeData() throws Exception {
        visualizer.startAdvisor("AdvisorB", 1);
        Thread.sleep(2);
        visualizer.endAdvisor("AdvisorB");

        String json = visualizer.exportToJSON();
        String csv = visualizer.exportToCSV();

        assertThat(json).contains("\"advisor\": \"AdvisorB\"");
        assertThat(csv).contains("AdvisorB");
    }

    @Test
    @DisplayName("getStatistics should summarize timeline events")
    void getStatistics_summarizes() throws Exception {
        visualizer.startAdvisor("AdvisorC", 0);
        Thread.sleep(3);
        visualizer.endAdvisor("AdvisorC");

        TimelineVisualizer.TimelineStatistics stats = visualizer.getStatistics();

        assertThat(stats.eventCount).isEqualTo(1);
        assertThat(stats.totalDuration).isGreaterThanOrEqualTo(0L);
        assertThat(stats.averageDuration).isGreaterThanOrEqualTo(0.0);
        assertThat(stats.maxDuration).isGreaterThanOrEqualTo(0L);
    }

    @Test
    @DisplayName("reset should clear timeline and start time")
    void reset_clearsState() throws Exception {
        visualizer.startAdvisor("AdvisorD", 0);
        Thread.sleep(2);
        visualizer.endAdvisor("AdvisorD");

        visualizer.reset();

        assertThat(visualizer.getTimeline()).isEmpty();
        assertThat(visualizer.getTotalDuration()).isEqualTo(0L);
    }
}
