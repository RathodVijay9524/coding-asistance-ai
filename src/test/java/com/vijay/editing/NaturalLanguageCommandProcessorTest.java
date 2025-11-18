package com.vijay.editing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.service.ASTAnalysisService;
import com.vijay.service.MLPatternDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class NaturalLanguageCommandProcessorTest {

    private ObjectMapper objectMapper;
    private EditSuggestionGenerator editSuggestionGenerator;
    private RefactoringAssistant refactoringAssistant;
    private NaturalLanguageCommandProcessor processor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        editSuggestionGenerator = mock(EditSuggestionGenerator.class);
        refactoringAssistant = new RefactoringAssistant(objectMapper, mock(ASTAnalysisService.class), mock(MLPatternDetectionService.class));
        processor = new NaturalLanguageCommandProcessor(objectMapper, editSuggestionGenerator, refactoringAssistant);
    }

    @Test
    @DisplayName("interpretCommand should return a confident interpretation for extract command")
    void interpretCommand_extract() throws Exception {
        String json = processor.interpretCommand("Extract this method", "public void m() {}" );

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("interpretation").get("action").asText()).isEqualTo("EXTRACT");
        assertThat(root.get("confidence").asDouble()).isGreaterThan(0.5);
    }

    @Test
    @DisplayName("planCommandExecution should build an execution plan from interpretation")
    void planCommandExecution_buildsPlan() throws Exception {
        NaturalLanguageCommandProcessor.CommandInterpretation interpretation = new NaturalLanguageCommandProcessor.CommandInterpretation();
        interpretation.setAction("EXTRACT");
        interpretation.setConfidence(0.9);
        String interpretationJson = objectMapper.writeValueAsString(interpretation);

        String json = processor.planCommandExecution(interpretationJson, "public void m() {}" );

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("plan").get("steps").isArray()).isTrue();
        assertThat(root.get("stepCount").asInt()).isGreaterThan(0);
    }

    @Test
    @DisplayName("executeCommand should iterate over plan steps and return transformedCode")
    void executeCommand_executesSteps() throws Exception {
        NaturalLanguageCommandProcessor.ExecutionPlan plan = new NaturalLanguageCommandProcessor.ExecutionPlan();
        plan.setCommand("EXTRACT");
        plan.setSteps(java.util.List.of(
                new NaturalLanguageCommandProcessor.ExecutionStep(1, "Step 1", "details"),
                new NaturalLanguageCommandProcessor.ExecutionStep(2, "Step 2", "details"))
        );
        String planJson = objectMapper.writeValueAsString(plan);

        String originalCode = "public void m() {}";
        String json = processor.executeCommand(planJson, originalCode);

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("executions").isArray()).isTrue();
        assertThat(root.get("executions").size()).isEqualTo(2);
        assertThat(root.get("transformedCode").asText()).isEqualTo(originalCode);
    }

    @Test
    @DisplayName("getCommandSuggestions should return suggestions based on selected code")
    void getCommandSuggestions_returnsSuggestions() throws Exception {
        String code = "public void m() { for (int i=0;i<n;i++) {} }";
        String json = processor.getCommandSuggestions(code);

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("suggestions").isArray()).isTrue();
        assertThat(root.get("count").asInt()).isGreaterThan(0);
    }
}
