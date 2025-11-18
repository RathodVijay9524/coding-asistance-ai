package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IncrementalIndexerTest {

    private VectorStore chunkStore;
    private FileHashTracker fileHashTracker;
    private IncrementalIndexer indexer;

    @BeforeEach
    void setUp() {
        chunkStore = mock(VectorStore.class);
        fileHashTracker = mock(FileHashTracker.class);
        indexer = new IncrementalIndexer(chunkStore, fileHashTracker);
    }

    @Test
    @DisplayName("indexChangedFiles should index only changed and new files and update statistics")
    void indexChangedFiles_basic() throws Exception {
        Path dir = Files.createTempDirectory("inc-index-test");
        Path fileA = dir.resolve("A.java");
        Path fileB = dir.resolve("B.java");
        String codeA = "package com.test;\npublic class A { void m() { System.out.println(\"A\"); } }";
        String codeB = "package com.test;\npublic class B { void n() { System.out.println(\"B\"); } }";
        Files.writeString(fileA, codeA);
        Files.writeString(fileB, codeB);

        List<String> files = List.of(fileA.toString(), fileB.toString());

        when(fileHashTracker.getChangedFiles(files)).thenReturn(List.of(fileA.toString()));
        when(fileHashTracker.getNewFiles(files)).thenReturn(List.of(fileB.toString()));

        IncrementalIndexer.IncrementalIndexResult result = indexer.indexChangedFiles(files);

        assertThat(result.totalFiles).isEqualTo(2);
        assertThat(result.changedFiles).isEqualTo(1);
        assertThat(result.newFiles).isEqualTo(1);
        assertThat(result.filesProcessed).isEqualTo(2);
        assertThat(result.chunksIndexed).isGreaterThanOrEqualTo(2);
        assertThat(result.errors).isEqualTo(0);

        IncrementalIndexer.IndexingStatistics stats = indexer.getStatistics();
        assertThat(stats.filesIndexed).isEqualTo(2);
        assertThat(stats.totalChunks).isGreaterThanOrEqualTo(2);
    }
}
