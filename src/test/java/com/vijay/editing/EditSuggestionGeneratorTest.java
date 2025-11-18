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
import static org.mockito.Mockito.when;

class EditSuggestionGeneratorTest {

    @Mock
    private ASTAnalysisService astAnalysisService;

    @Mock
    private MLPatternDetectionService mlPatternDetectionService;

    @Mock
    private CodeSelectionAnalyzer codeSelectionAnalyzer;

    private EditSuggestionGenerator generator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new EditSuggestionGenerator(
                new ObjectMapper(),
                astAnalysisService,
                mlPatternDetectionService,
                codeSelectionAnalyzer
        );
    }

    @Test
    @DisplayName("generateSuggestions should produce extract and rename suggestions based on instruction and analysis")
    void generateSuggestions_shouldProduceSuggestions() {
        // Arrange
        String code = "public void test() { int i = 0; int x = 1; }";
        String instruction = "extract and rename for better clarity";

        CodeSelectionAnalyzer.SelectionAnalysis analysis = new CodeSelectionAnalyzer.SelectionAnalysis();
        analysis.setLineCount(15); // trigger extract method
        analysis.setVariables(List.of("i", "x")); // trigger renaming

        when(codeSelectionAnalyzer.analyzeSelection(code)).thenReturn(analysis);

        // Act
        List<EditSuggestionGenerator.EditSuggestion> suggestions = generator.generateSuggestions(code, instruction);

        // Assert
        assertThat(suggestions).isNotEmpty();
        assertThat(suggestions)
                .extracting(EditSuggestionGenerator.EditSuggestion::getType)
                .contains("Extract Method", "Rename Variable");
    }
}
