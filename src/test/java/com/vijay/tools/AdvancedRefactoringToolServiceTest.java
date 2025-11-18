package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.service.ASTAnalysisService;
import com.vijay.service.MLPatternDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdvancedRefactoringToolServiceTest {

    private ObjectMapper objectMapper;
    private ASTAnalysisService astAnalysisService;
    private MLPatternDetectionService mlPatternDetectionService;
    private AdvancedRefactoringToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        astAnalysisService = mock(ASTAnalysisService.class);
        mlPatternDetectionService = mock(MLPatternDetectionService.class);
        service = new AdvancedRefactoringToolService(objectMapper, astAnalysisService, mlPatternDetectionService);
    }

    @Test
    @DisplayName("extractMethod should build extractedMethod, parameters, impactAnalysis and testingStrategy")
    void extractMethod_basic() throws Exception {
        String source = """
                public class Sample {
                    public void method() {
                        int a = 1;
                        int b = 2;
                        int c = a + b;
                    }
                }
                """;

        String json = service.extractMethod(source, 3, 5, "extractedMethod");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("extractedMethod")).isTrue();
        assertThat(root.get("extractedMethod").asText()).contains("public void extractedMethod()");
        assertThat(root.get("parameters").isArray()).isTrue();
        assertThat(root.get("impactAnalysis").isObject()).isTrue();
        assertThat(root.get("testingStrategy").isArray()).isTrue();
    }

    @Test
    @DisplayName("renameRefactoring should replace occurrences and validate new name")
    void renameRefactoring_basic() throws Exception {
        String source = "public class Sample { int value = 1; void m() { value++; } }";

        String json = service.renameRefactoring(source, "value", "amount", "class");

        JsonNode root = objectMapper.readTree(json);
        String refactored = root.get("refactoredCode").asText();
        assertThat(refactored).contains("amount");
        assertThat(refactored).doesNotContain("value ");
        assertThat(root.get("occurrences").asInt()).isGreaterThan(0);
        assertThat(root.get("impactAnalysis").isObject()).isTrue();
        assertThat(root.get("validationPassed").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("consolidateDuplicateCode should use MLPatternDetectionService and return consolidationPlan")
    void consolidateDuplicateCode_usesMlService() throws Exception {
        String source = "int a = 1; int b = 1; int c = 1;";

        MLPatternDetectionService.CodeClone clone =
                new MLPatternDetectionService.CodeClone("Type 1", 3, List.of(1, 2, 3), "desc");
        when(mlPatternDetectionService.detectCodeClones(source)).thenReturn(List.of(clone));

        String json = service.consolidateDuplicateCode(source, 0.8);

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("duplicateCodeFound").asInt()).isEqualTo(1);
        assertThat(root.get("consolidationPlan").isArray()).isTrue();
        assertThat(root.get("estimatedEffort").asInt()).isGreaterThanOrEqualTo(0);
        assertThat(root.get("estimatedImpact").asDouble()).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("consolidateDuplicateCode should return error JSON when ML service throws")
    void consolidateDuplicateCode_error() throws Exception {
        String source = "code";
        when(mlPatternDetectionService.detectCodeClones(source)).thenThrow(new RuntimeException("boom"));

        String json = service.consolidateDuplicateCode(source, 0.8);

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("error")).isTrue();
        assertThat(root.get("error").asText()).contains("Consolidation failed");
    }

    @Test
    @DisplayName("simplifyExpressions should return simplifications, readabilityScore and recommendations")
    void simplifyExpressions_basic() throws Exception {
        String json = service.simplifyExpressions("int x = (1 + 2) * 3;", 5);

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("complexExpressionsFound").asInt()).isEqualTo(5);
        assertThat(root.get("simplifications").isArray()).isTrue();
        assertThat(root.get("readabilityScore").asDouble()).isGreaterThan(0.0);
        assertThat(root.get("recommendations").isArray()).isTrue();
    }

    @Test
    @DisplayName("applyDesignPatterns should include pattern, patternCode, benefits and steps")
    void applyDesignPatterns_basic() throws Exception {
        String json = service.applyDesignPatterns("class A {}", "Singleton");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("pattern").asText()).isEqualTo("Singleton");
        assertThat(root.get("patternCode").asText()).contains("Singleton");
        assertThat(root.get("benefits").isObject()).isTrue();
        assertThat(root.get("implementationSteps").isArray()).isTrue();
        assertThat(root.get("testingStrategy").isArray()).isTrue();
    }

    @Test
    @DisplayName("optimizePerformance should return issuesFound, optimizations and estimatedPerformanceGain")
    void optimizePerformance_basic() throws Exception {
        String json = service.optimizePerformance("for (int i = 0; i < 10; i++) {}", "loops");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("issuesFound").asInt()).isEqualTo(3);
        assertThat(root.get("optimizations").isArray()).isTrue();
        assertThat(root.get("estimatedPerformanceGain").asText()).contains("%");
        assertThat(root.get("implementationPriority").isArray()).isTrue();
    }

    @Test
    @DisplayName("automateBugFixes should return fixes, riskAssessment and testingStrategy")
    void automateBugFixes_basic() throws Exception {
        String json = service.automateBugFixes("if (obj == null) obj.toString();", "null-pointer");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("bugsFound").asInt()).isEqualTo(2);
        assertThat(root.get("fixes").isArray()).isTrue();
        assertThat(root.get("riskAssessment").asText()).isNotBlank();
        assertThat(root.get("testingStrategy").isArray()).isTrue();
        assertThat(root.get("reviewChecklist").isArray()).isTrue();
    }
}
