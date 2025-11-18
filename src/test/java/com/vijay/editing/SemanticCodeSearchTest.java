package com.vijay.editing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;

import static org.assertj.core.api.Assertions.assertThat;

class SemanticCodeSearchTest {

    private SemanticCodeSearch semanticCodeSearch;

    @Mock
    private ChatClient chatClient;

    @Mock
    private VectorStore vectorStore;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        semanticCodeSearch = new SemanticCodeSearch(new ObjectMapper(), chatClient, vectorStore);
    }

    @Test
    @DisplayName("searchByIntent should return JSON with status and results")
    void searchByIntent_shouldReturnJson() {
        // Act
        String json = semanticCodeSearch.searchByIntent("find user service", "./project");

        // Assert
        assertThat(json).contains("\"status\":\"success\"");
        assertThat(json).contains("resultCount");
    }

    @Test
    @DisplayName("calculateSemanticSimilarity should include similarity field in JSON")
    void calculateSemanticSimilarity_shouldReturnSimilarity() {
        // Act
        String json = semanticCodeSearch.calculateSemanticSimilarity(
                "public void a() {}",
                "public void b() {}"
        );

        // Assert
        assertThat(json).contains("\"status\":\"success\"");
        assertThat(json).contains("similarity");
    }
}
