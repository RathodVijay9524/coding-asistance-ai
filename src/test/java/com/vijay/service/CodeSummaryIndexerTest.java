package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class CodeSummaryIndexerTest {

    private VectorStore summaryStore;
    private OpenAiChatModel chatModel;
    private EmbeddingCacheManager cacheManager;
    private CodeSummaryIndexer indexer;

    @BeforeEach
    void setUp() {
        summaryStore = mock(VectorStore.class);
        chatModel = mock(OpenAiChatModel.class);
        cacheManager = mock(EmbeddingCacheManager.class);
        indexer = new CodeSummaryIndexer(summaryStore, chatModel, cacheManager);
    }

    @Test
    @DisplayName("indexCodeSummaries should skip indexing when cache file exists")
    void indexCodeSummaries_skipsWhenCacheExists() {
        when(cacheManager.cacheFileExists()).thenReturn(true);

        indexer.indexCodeSummaries();

        verify(cacheManager, times(1)).cacheFileExists();
        verify(summaryStore, never()).add(anyList());
        verify(cacheManager, never()).calculateDocumentsHash(anyList());
    }

    @Test
    @DisplayName("getIndexedFileCount should use VectorStore similaritySearch result size")
    void getIndexedFileCount_usesVectorStore() {
        when(summaryStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(
                        new Document("summary1", java.util.Map.of()),
                        new Document("summary2", java.util.Map.of()),
                        new Document("summary3", java.util.Map.of())
                ));

        long count = indexer.getIndexedFileCount();

        assertThat(count).isEqualTo(3L);
    }
}
