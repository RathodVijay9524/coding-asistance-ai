package com.vijay.editing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;

class PatternExtractorTest {

    private PatternExtractor patternExtractor;

    @Mock
    private ChatClient chatClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        patternExtractor = new PatternExtractor(new ObjectMapper(), chatClient);
    }

    @Test
    @DisplayName("extractDesignPatterns should detect Singleton when pattern matches")
    void extractDesignPatterns_shouldDetectSingleton() {
        // Arrange
        String code = "public class Singleton { private static Singleton instance; public static Singleton getInstance() { return instance; } }";

        // Act
        String json = patternExtractor.extractDesignPatterns(code);

        // Assert
        assertThat(json).contains("\"status\":\"success\"");
        assertThat(json).contains("Singleton");
    }
}
