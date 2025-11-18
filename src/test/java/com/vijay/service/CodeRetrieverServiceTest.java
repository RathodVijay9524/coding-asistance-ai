package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CodeRetrieverServiceTest {

    private VectorStore summaryStore;
    private VectorStore chunkStore;
    private DependencyGraphBuilder dependencyGraph;
    private ContextManager contextManager;
    private QueryPlanner queryPlanner;

    private CodeRetrieverService service;

    @BeforeEach
    void setUp() {
        summaryStore = mock(VectorStore.class);
        chunkStore = mock(VectorStore.class);
        dependencyGraph = mock(DependencyGraphBuilder.class);
        contextManager = mock(ContextManager.class);
        queryPlanner = mock(QueryPlanner.class);

        service = new CodeRetrieverService(summaryStore, chunkStore, dependencyGraph, contextManager, queryPlanner);
    }

    @Test
    @DisplayName("retrieveCodeContextWithPlan should build non-empty context using SearchPlan and VectorStores")
    void retrieveCodeContextWithPlan_buildsContext() {
        String query = "Explain CodeRetrieverService";

        QueryPlanner.SearchPlan plan = new QueryPlanner.SearchPlan();
        plan.originalQuery = query;
        plan.searchStrategy = "similarity_search";
        plan.topK = 3;
        plan.maxHops = 0; // avoid dependency expansion complexity
        plan.includeReverseDeps = false;
        plan.tokenBudget = 5000;
        plan.confidence = 0.85;

        when(queryPlanner.createSearchPlan(query)).thenReturn(plan);

        ContextManager.ContextBudget budget = new ContextManager.ContextBudget();
        when(contextManager.createBudget(query)).thenReturn(budget);

        Document summaryDoc = new Document("Summary text", Map.of("filename", "FileA.java"));
        when(summaryStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(summaryDoc));

        Document chunkDoc = new Document("public class FileA {}", Map.of(
                "filename", "FileA.java",
                "chunk_type", "class-chunk"
        ));
        when(chunkStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(chunkDoc));

        when(contextManager.prioritizeFiles(anyList(), anyString(), any(ContextManager.ContextBudget.class)))
                .thenReturn(List.of("FileA.java"));

        when(contextManager.pruneContent(anyList(), any(ContextManager.ContextBudget.class), anyString()))
                .thenReturn(List.of(chunkDoc.getText()));

        CodeRetrieverService.CodeContext context = service.retrieveCodeContextWithPlan(query);

        assertThat(context.isEmpty()).isFalse();
        assertThat(context.getQuery()).isEqualTo(query);
        assertThat(context.getFileSummaries()).hasSize(1);
        assertThat(context.getFileSummaries().get(0).getMetadata().get("filename")).isEqualTo("FileA.java");
        assertThat(context.getCodeChunks()).hasSize(1);
        assertThat(context.getCodeChunks().get(0).getMetadata().get("filename")).isEqualTo("FileA.java");
        assertThat(context.getRelevantFiles()).containsExactly("FileA.java");
        assertThat(context.getSearchStrategy()).isEqualTo(plan.searchStrategy);
        assertThat(context.getSearchConfidence()).isEqualTo(plan.confidence);

        verify(queryPlanner, times(1)).createSearchPlan(query);
        verify(summaryStore, atLeastOnce()).similaritySearch(any(SearchRequest.class));
        verify(chunkStore, atLeastOnce()).similaritySearch(any(SearchRequest.class));
    }

    @Test
    @DisplayName("retrieveCodeContextWithPlan should return empty context when no files are found")
    void retrieveCodeContextWithPlan_noResultsReturnsEmpty() {
        String query = "No matching files";

        QueryPlanner.SearchPlan plan = new QueryPlanner.SearchPlan();
        plan.originalQuery = query;
        plan.searchStrategy = "similarity_search";
        plan.topK = 3;
        plan.maxHops = 0;
        plan.includeReverseDeps = false;
        plan.tokenBudget = 5000;

        when(queryPlanner.createSearchPlan(query)).thenReturn(plan);

        ContextManager.ContextBudget budget = new ContextManager.ContextBudget();
        when(contextManager.createBudget(query)).thenReturn(budget);

        when(summaryStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of());

        CodeRetrieverService.CodeContext context = service.retrieveCodeContextWithPlan(query);

        assertThat(context.isEmpty()).isTrue();
        assertThat(context.getFileSummaries()).isEmpty();
        assertThat(context.getCodeChunks()).isEmpty();
        assertThat(context.getRelevantFiles()).isEmpty();
    }

    @Test
    @DisplayName("retrieveSpecificFile should return summaries and chunks only for requested file")
    void retrieveSpecificFile_returnsCorrectContext() {
        String filename = "MyFile.java";

        Document summary1 = new Document("Summary 1", Map.of("filename", filename));
        Document summary2 = new Document("Other summary", Map.of("filename", "OtherFile.java"));
        when(summaryStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(summary1, summary2));

        Document chunk1 = new Document("Code 1", Map.of(
                "filename", filename,
                "chunk_type", "class-chunk"
        ));
        Document chunk2 = new Document("Other code", Map.of(
                "filename", "OtherFile.java",
                "chunk_type", "class-chunk"
        ));
        when(chunkStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(chunk1, chunk2));

        CodeRetrieverService.CodeContext context = service.retrieveSpecificFile(filename);

        assertThat(context.getRelevantFiles()).containsExactly(filename);
        assertThat(context.getQuery()).isEqualTo("file: " + filename);
        assertThat(context.getFileSummaries()).hasSize(1);
        assertThat(context.getFileSummaries().get(0).getMetadata().get("filename")).isEqualTo(filename);
        assertThat(context.getCodeChunks()).hasSize(1);
        assertThat(context.getCodeChunks().get(0).getMetadata().get("filename")).isEqualTo(filename);
    }

    @Test
    @DisplayName("retrieveCodeContextWithPlan should execute entity_centered strategy using target entities")
    void retrieveCodeContextWithPlan_entityCenteredStrategy_usesEntities() {
        String query = "Explain UserService";

        QueryPlanner.SearchPlan plan = new QueryPlanner.SearchPlan();
        plan.originalQuery = query;
        plan.searchStrategy = "entity_centered";
        plan.topK = 3;
        plan.maxHops = 0;
        plan.includeReverseDeps = false;
        plan.tokenBudget = 5000;
        plan.confidence = 0.9;
        plan.targetEntities = List.of("UserService", "UserController");

        when(queryPlanner.createSearchPlan(query)).thenReturn(plan);

        ContextManager.ContextBudget budget = new ContextManager.ContextBudget();
        when(contextManager.createBudget(query)).thenReturn(budget);

        // First calls by entity, then by original query; just return one summary for simplicity
        Document summaryDoc = new Document("User service summary", Map.of("filename", "UserService.java"));
        when(summaryStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(summaryDoc));

        Document chunkDoc = new Document("class UserService {}", Map.of(
                "filename", "UserService.java",
                "chunk_type", "class-chunk"
        ));
        when(chunkStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(chunkDoc));

        when(contextManager.prioritizeFiles(anyList(), anyString(), any(ContextManager.ContextBudget.class)))
                .thenReturn(List.of("UserService.java"));

        when(contextManager.pruneContent(anyList(), any(ContextManager.ContextBudget.class), anyString()))
                .thenReturn(List.of(chunkDoc.getText()));

        CodeRetrieverService.CodeContext context = service.retrieveCodeContextWithPlan(query);

        assertThat(context.isEmpty()).isFalse();
        assertThat(context.getFileSummaries()).hasSize(1);
        assertThat(context.getFileSummaries().get(0).getMetadata().get("filename")).isEqualTo("UserService.java");
        assertThat(context.getSearchStrategy()).isEqualTo("entity_centered");
    }

    @Test
    @DisplayName("retrieveCodeContext should delegate to retrieveCodeContextWithPlan")
    void retrieveCodeContext_delegatesToWithPlan() {
        String query = "Find ChatService";

        QueryPlanner.SearchPlan plan = new QueryPlanner.SearchPlan();
        plan.originalQuery = query;
        plan.searchStrategy = "similarity_search";
        plan.topK = 1;
        plan.maxHops = 0;
        plan.includeReverseDeps = false;
        plan.tokenBudget = 1000;
        plan.confidence = 0.5;

        when(queryPlanner.createSearchPlan(query)).thenReturn(plan);
        when(contextManager.createBudget(query)).thenReturn(new ContextManager.ContextBudget());

        Document summaryDoc = new Document("ChatService summary", Map.of("filename", "ChatService.java"));
        when(summaryStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(summaryDoc));

        Document chunkDoc = new Document("class ChatService {}", Map.of(
                "filename", "ChatService.java",
                "chunk_type", "class-chunk"
        ));
        when(chunkStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(chunkDoc));

        when(contextManager.prioritizeFiles(anyList(), anyString(), any(ContextManager.ContextBudget.class)))
                .thenReturn(List.of("ChatService.java"));
        when(contextManager.pruneContent(anyList(), any(ContextManager.ContextBudget.class), anyString()))
                .thenReturn(List.of(chunkDoc.getText()));

        CodeRetrieverService.CodeContext context = service.retrieveCodeContext(query);

        assertThat(context.isEmpty()).isFalse();
        assertThat(context.getQuery()).isEqualTo(query);
        assertThat(context.getFileSummaries()).hasSize(1);
    }

    @Test
    @DisplayName("CodeContext.getFormattedContext should include summaries and chunks")
    void codeContext_getFormattedContext_includesSummariesAndChunks() {
        CodeRetrieverService.CodeContext ctx = new CodeRetrieverService.CodeContext();

        Document summaryDoc = new Document("Summary", Map.of("filename", "FileA.java"));
        Document chunkDoc = new Document("public class FileA {}", Map.of(
                "filename", "FileA.java",
                "chunk_type", "class-chunk"
        ));

        ctx.setFileSummaries(List.of(summaryDoc));
        ctx.setCodeChunks(List.of(chunkDoc));

        String formatted = ctx.getFormattedContext();
        assertThat(formatted).contains("File Summaries");
        assertThat(formatted).contains("FileA.java");
        assertThat(formatted).contains("Code Chunks");
        assertThat(formatted).contains("public class FileA");
    }

    @Test
    @DisplayName("retrieveCodeContextWithPlan should handle VectorStore errors gracefully and return empty context")
    void retrieveCodeContextWithPlan_handlesVectorStoreError() {
        String query = "error scenario";

        QueryPlanner.SearchPlan plan = new QueryPlanner.SearchPlan();
        plan.originalQuery = query;
        plan.searchStrategy = "similarity_search";
        plan.topK = 3;
        plan.maxHops = 0;
        plan.includeReverseDeps = false;
        plan.tokenBudget = 5000;

        when(queryPlanner.createSearchPlan(query)).thenReturn(plan);
        when(contextManager.createBudget(query)).thenReturn(new ContextManager.ContextBudget());

        when(summaryStore.similaritySearch(any(SearchRequest.class)))
                .thenThrow(new RuntimeException("store failure"));

        CodeRetrieverService.CodeContext context = service.retrieveCodeContextWithPlan(query);

        assertThat(context.isEmpty()).isTrue();
        assertThat(context.getFileSummaries()).isEmpty();
        assertThat(context.getCodeChunks()).isEmpty();
    }
}
