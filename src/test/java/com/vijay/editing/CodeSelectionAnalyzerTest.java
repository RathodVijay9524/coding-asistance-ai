package com.vijay.editing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.service.ASTAnalysisService;
import com.vijay.service.MLPatternDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CodeSelectionAnalyzerTest {

    private CodeSelectionAnalyzer analyzer;

    @Mock
    private ASTAnalysisService astAnalysisService;

    @Mock
    private MLPatternDetectionService mlPatternDetectionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        analyzer = new CodeSelectionAnalyzer(new ObjectMapper(), astAnalysisService, mlPatternDetectionService);
    }

    @Test
    @DisplayName("analyzeSelection should compute basic metrics and detect language")
    void analyzeSelection_shouldComputeBasicMetrics() {
        // Arrange
        String code = "public class Demo {\n" +
                "  public void test() { int x = 1; }\n" +
                "}";

        // Act
        CodeSelectionAnalyzer.SelectionAnalysis analysis = analyzer.analyzeSelection(code);

        // Assert
        assertThat(analysis.getCodeLength()).isEqualTo(code.length());
        assertThat(analysis.getLineCount()).isEqualTo(3);
        assertThat(analysis.getLanguage()).isEqualTo("Java");
        assertThat(analysis.getMethods()).isNotEmpty();
        assertThat(analysis.getClasses()).isNotEmpty();
    }

    @Test
    @DisplayName("detectPatterns should identify Singleton when private static and getInstance present")
    void detectPatterns_shouldIdentifySingleton() {
        // Arrange
        String code = "public class Singleton {\n" +
                "  private static Singleton instance;\n" +
                "  public static Singleton getInstance() { return instance; }\n" +
                "}";

        // Act
        List<CodeSelectionAnalyzer.PatternInfo> patterns = analyzer.detectPatterns(code);

        // Assert
        assertThat(patterns)
                .extracting(CodeSelectionAnalyzer.PatternInfo::getName)
                .contains("Singleton");
    }
}
