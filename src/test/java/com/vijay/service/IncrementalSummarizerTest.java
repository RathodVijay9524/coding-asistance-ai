package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class IncrementalSummarizerTest {

    private IncrementalSummarizer summarizer;

    @BeforeEach
    void setUp() {
        summarizer = new IncrementalSummarizer();
    }

    @Test
    @DisplayName("summarizeChangedChunks should summarize new content and cache by hash")
    void summarizeChangedChunks_basic() {
        IncrementalSummarizer.ChunkToSummarize c1 = new IncrementalSummarizer.ChunkToSummarize("1", "word ".repeat(150), "source1");
        IncrementalSummarizer.ChunkToSummarize c2 = new IncrementalSummarizer.ChunkToSummarize("2", "short content", "source2");

        IncrementalSummarizer.SummarizationResult r1 = summarizer.summarizeChangedChunks(List.of(c1, c2));

        assertThat(r1.totalChunks).isEqualTo(2);
        assertThat(r1.summarizedChunks).isEqualTo(2);
        assertThat(r1.cachedChunks).isEqualTo(0);

        String s1 = summarizer.getChunkSummary("1");
        String s2 = summarizer.getChunkSummary("2");
        assertThat(s1.split("\\s+").length).isLessThanOrEqualTo(101);
        assertThat(s1).endsWith("...");
        assertThat(s2).contains("short content");

        IncrementalSummarizer.SummarizationResult r2 = summarizer.summarizeChangedChunks(List.of(c1, c2));
        assertThat(r2.cachedChunks).isEqualTo(2);

        Map<String, String> all = summarizer.getAllSummaries();
        assertThat(all).containsKeys("1", "2");

        IncrementalSummarizer.SummarizationStatistics stats = summarizer.getStatistics();
        assertThat(stats.totalSummaries).isEqualTo(2);
        assertThat(stats.totalSummaryLength).isGreaterThan(0);
    }
}
