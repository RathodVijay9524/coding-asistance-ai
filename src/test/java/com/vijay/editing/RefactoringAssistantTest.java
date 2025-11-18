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

class RefactoringAssistantTest {

    private ObjectMapper objectMapper;
    private ASTAnalysisService astAnalysisService;
    private MLPatternDetectionService mlPatternDetectionService;
    private RefactoringAssistant assistant;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        astAnalysisService = mock(ASTAnalysisService.class);
        mlPatternDetectionService = mock(MLPatternDetectionService.class);
        assistant = new RefactoringAssistant(objectMapper, astAnalysisService, mlPatternDetectionService);
    }

    @Test
    @DisplayName("createRefactoringPlan should create a plan with steps and affected files")
    void createRefactoringPlan_createsPlan() throws Exception {
        String command = "Extract method from this block";
        String sourceCode = "public class Sample { void m() { int a=1; int b=2; } }";

        String json = assistant.createRefactoringPlan(command, sourceCode, "/project/root");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        JsonNode plan = root.get("plan");
        assertThat(plan.get("refactoringType").asText()).isEqualTo("EXTRACT_METHOD");
        assertThat(plan.get("steps").isArray()).isTrue();
        assertThat(plan.get("affectedFiles").isArray()).isTrue();
    }

    @Test
    @DisplayName("validateRefactoringPlan should return warnings when no tests present")
    void validateRefactoringPlan_returnsWarnings() throws Exception {
        String sourceCode = "public class Sample { void m() {} }"; // no @Test

        String json = assistant.validateRefactoringPlan(sourceCode, "RENAME");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("warnings").isArray()).isTrue();
        assertThat(root.get("warnings").size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("executeRefactoringPlan should iterate over steps and return execution results")
    void executeRefactoringPlan_executesSteps() throws Exception {
        // Build a minimal plan JSON with two steps
        RefactoringAssistant.RefactoringPlan plan = new RefactoringAssistant.RefactoringPlan();
        plan.setRefactoringType("RENAME");
        plan.setCommand("Rename variable");
        plan.setStatus("PLANNED");
        plan.setSteps(java.util.List.of(
                new RefactoringAssistant.RefactoringStep(1, "Step 1", "details"),
                new RefactoringAssistant.RefactoringStep(2, "Step 2", "details")
        ));

        String planJson = objectMapper.writeValueAsString(plan);
        String sourceCode = "public class Sample { void m() {} }";

        String json = assistant.executeRefactoringPlan(planJson, sourceCode);

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("executionResults").isArray()).isTrue();
        assertThat(root.get("executionResults").size()).isEqualTo(2);
        assertThat(root.get("finalCode").asText()).isEqualTo(sourceCode);
    }
}
