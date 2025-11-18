package com.vijay.tools;

import com.vijay.context.GlobalBrainContext;
import com.vijay.dto.ReasoningState;
import org.junit.jupiter.api.AfterEach;
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

class ToolFinderServiceTest {

    private VectorStore vectorStore;
    private ToolFinderService toolFinderService;

    @BeforeEach
    void setUp() {
        vectorStore = mock(VectorStore.class);
        toolFinderService = new ToolFinderService(vectorStore);
    }

    @AfterEach
    void tearDown() {
        GlobalBrainContext.setReasoningState(null);
    }

    @Test
    @DisplayName("findToolsFor should query VectorStore and store ReasoningState in GlobalBrainContext")
    void findToolsFor_populatesReasoningState() {
        String prompt = "Analyze project dependencies and suggest tools";

        Document doc1 = new Document("Tool A desc", Map.of("toolName", "toolA"));
        Document doc2 = new Document("Tool B desc", Map.of("toolName", "toolB"));

        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(doc1, doc2));

        List<String> tools = toolFinderService.findToolsFor(prompt);

        assertThat(tools).containsExactly("toolA", "toolB");

        ReasoningState state = GlobalBrainContext.getReasoningState();
        assertThat(state).isNotNull();
        assertThat(state.getUserQuery()).isEqualTo(prompt);
        assertThat(state.getSuggestedTools()).containsExactly("toolA", "toolB");

        verify(vectorStore, times(1)).similaritySearch(any(SearchRequest.class));
    }
}
