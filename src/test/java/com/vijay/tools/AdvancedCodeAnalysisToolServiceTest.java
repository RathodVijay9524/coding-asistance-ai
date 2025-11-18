package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.service.ASTAnalysisService;
import com.vijay.service.MLPatternDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdvancedCodeAnalysisToolServiceTest {

    private ObjectMapper objectMapper;
    private ASTAnalysisService astAnalysisService;
    private MLPatternDetectionService mlPatternDetectionService;
    private AdvancedCodeAnalysisToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        astAnalysisService = mock(ASTAnalysisService.class);
        mlPatternDetectionService = mock(MLPatternDetectionService.class);
        service = new AdvancedCodeAnalysisToolService(objectMapper, astAnalysisService, mlPatternDetectionService);
    }

    @Test
    @DisplayName("performComprehensiveAnalysis should include structure, patterns, quality and overallAssessment")
    void performComprehensiveAnalysis_basic() throws Exception {
        String source = "class A { void m() {} }";

        // Build a concrete ClassInfo instance using setters
        ASTAnalysisService.ClassInfo classInfo = new ASTAnalysisService.ClassInfo();
        classInfo.setName("A");
        classInfo.setFields(Collections.emptyList());
        classInfo.setMethods(Collections.emptyList());
        classInfo.setComplexity(3);
        classInfo.setLinesOfCode(10);

        when(astAnalysisService.extractClassInfo(source)).thenReturn(classInfo);
        when(astAnalysisService.generateCallGraph(source)).thenReturn(new ASTAnalysisService.MethodCallGraph());
        when(astAnalysisService.detectCodeSmells(source)).thenReturn(Collections.emptyList());
        when(astAnalysisService.analyzeDependencies(source)).thenReturn(new ASTAnalysisService.CodeDependencies());

        when(mlPatternDetectionService.detectDesignPatterns(source)).thenReturn(Collections.emptyList());
        when(mlPatternDetectionService.detectAntiPatterns(source)).thenReturn(Collections.emptyList());
        when(mlPatternDetectionService.detectCodeClones(source)).thenReturn(Collections.emptyList());
        when(mlPatternDetectionService.detectAnomalies(source)).thenReturn(Collections.emptyList());

        String json = service.performComprehensiveAnalysis(source, "comprehensive", "all");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("structure")).isTrue();
        assertThat(root.has("patterns")).isTrue();
        assertThat(root.has("quality")).isTrue();
        assertThat(root.has("overallAssessment")).isTrue();
    }

    @Test
    @DisplayName("analyzePatterns should return design and anti patterns with patternCount")
    void analyzePatterns_basic() throws Exception {
        String source = "class A {}";

        MLPatternDetectionService.PatternMatch pm =
                new MLPatternDetectionService.PatternMatch("Singleton", "Design Pattern", 0.9, "desc");
        MLPatternDetectionService.AntiPattern ap =
                new MLPatternDetectionService.AntiPattern("God Object", "desc", "High", "suggestion");

        when(mlPatternDetectionService.detectDesignPatterns(source)).thenReturn(List.of(pm));
        when(mlPatternDetectionService.detectAntiPatterns(source)).thenReturn(List.of(ap));

        String json = service.analyzePatterns(source, "all");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("designPatterns").isArray()).isTrue();
        assertThat(root.get("antiPatterns").isArray()).isTrue();
        assertThat(root.get("patternCount").asInt()).isEqualTo(2);
    }
}
