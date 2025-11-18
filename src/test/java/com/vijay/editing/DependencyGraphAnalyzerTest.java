package com.vijay.editing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;

class DependencyGraphAnalyzerTest {

    private DependencyGraphAnalyzer analyzer;

    @Mock
    private ChatClient chatClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        analyzer = new DependencyGraphAnalyzer(new ObjectMapper(), chatClient);
    }

    @Test
    @DisplayName("buildGraph should return JSON with graph and nodeCount")
    void buildGraph_shouldReturnGraphJson() {
        // Act
        String json = analyzer.buildGraph("./project");

        // Assert
        assertThat(json).contains("\"status\":\"success\"");
        assertThat(json).contains("nodeCount");
        assertThat(json).contains("edgeCount");
    }

    @Test
    @DisplayName("findDependencies should return direct and transitive dependencies")
    void findDependencies_shouldReturnDependencies() {
        // Act
        String json = analyzer.findDependencies("UserService.java");

        // Assert
        assertThat(json).contains("\"status\":\"success\"");
        assertThat(json).contains("directDependencies");
        assertThat(json).contains("transitiveDependencies");
    }
}
