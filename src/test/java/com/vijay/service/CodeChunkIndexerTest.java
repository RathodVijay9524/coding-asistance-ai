package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class CodeChunkIndexerTest {

    private VectorStore chunkStore;
    private EmbeddingCacheManager cacheManager;
    private CodeChunkIndexer indexer;

    @BeforeEach
    void setUp() {
        chunkStore = mock(VectorStore.class);
        cacheManager = mock(EmbeddingCacheManager.class);
        indexer = new CodeChunkIndexer(chunkStore, cacheManager);
    }

    @Test
    @DisplayName("indexCodeChunks should skip indexing when cache file exists")
    void indexCodeChunks_skipsWhenCacheExists() {
        when(cacheManager.cacheFileExists()).thenReturn(true);

        indexer.indexCodeChunks();

        verify(cacheManager, times(1)).cacheFileExists();
        verify(chunkStore, never()).add(anyList());
        verify(cacheManager, never()).calculateDocumentsHash(anyList());
    }

    @Test
    @DisplayName("getIndexedChunkCount should use VectorStore similaritySearch result size")
    void getIndexedChunkCount_usesVectorStore() {
        when(chunkStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(
                        new Document("chunk1", java.util.Map.of()),
                        new Document("chunk2", java.util.Map.of())
                ));

        long count = indexer.getIndexedChunkCount();

        assertThat(count).isEqualTo(2L);
    }
}
