package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CodeIntelligenceEngineTest {

    private CodeIntelligenceEngine engine;

    @BeforeEach
    void setUp() {
        engine = new CodeIntelligenceEngine();
    }

    @Test
    @DisplayName("analyzeCode should detect bugs, refactorings, patterns and performance issues")
    void analyzeCode_detectsIssues() {
        String code = "" +
                "@Component\n" +
                "public class Demo {\n" +
                "  public void test() {\n" +
                "    while(true) { }\n" + // infinite loop
                "    String s = \"\";\n" +
                "    for (int i = 0; i < 10; i++) { s += \"x\"; }\n" + // string concat in loop
                "    new java.io.FileInputStream(\"file.txt\");\n" + // resource leak
                "    throw new RuntimeException();\n" + // unhandled exception
                "  }\n" +
                "}\n";

        CodeIntelligenceEngine.CodeIntelligence result = engine.analyzeCode(code, "java");

        List<CodeIntelligenceEngine.Bug> bugs = result.getBugs();
        List<CodeIntelligenceEngine.Refactoring> refactorings = result.getRefactorings();
        List<String> patterns = result.getPatterns();
        List<CodeIntelligenceEngine.PerformanceIssue> perf = result.getPerformanceIssues();

        // At least one category of issue should be detected
        int totalIssues = bugs.size() + refactorings.size() + perf.size();
        assertThat(totalIssues).isGreaterThan(0);

        // Spring @Component pattern should be recognized
        assertThat(patterns).contains("Spring Component");

        // Quality score should always be in [0, 100]
        assertThat(result.getQualityScore()).isBetween(0.0, 100.0);
    }

    @Test
    @DisplayName("analyzeCode should return empty intelligence for empty code")
    void analyzeCode_empty() {
        CodeIntelligenceEngine.CodeIntelligence result = engine.analyzeCode("", "java");
        assertThat(result.getBugs()).isEmpty();
        assertThat(result.getRefactorings()).isEmpty();
        assertThat(result.getPatterns()).isEmpty();
        assertThat(result.getPerformanceIssues()).isEmpty();
        assertThat(result.getQualityScore()).isEqualTo(100.0);
    }
}
