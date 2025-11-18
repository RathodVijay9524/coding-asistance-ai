package com.vijay.editing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CodeTransformationEngineTest {

    private CodeTransformationEngine engine;

    @BeforeEach
    void setUp() {
        // ChatClient is not used by transformFile, so we can pass null here
        engine = new CodeTransformationEngine(new ObjectMapper(), null);
    }

    @Test
    @DisplayName("transformFile should apply simple rename rule and return JSON with transformedCode")
    void transformFile_shouldApplyRenameRule() {
        // Arrange
        String original = "public void test() { int foo = 1; foo++; }";
        // include 'rename' keyword so CodeTransformationEngine.parseRules creates a RENAME rule
        String rules = "rename from: foo to: bar";

        // Act
        String json = engine.transformFile("Test.java", original, rules);

        // Assert
        assertThat(json).contains("\"status\":\"success\"");
        assertThat(json).contains("bar");
    }

}
