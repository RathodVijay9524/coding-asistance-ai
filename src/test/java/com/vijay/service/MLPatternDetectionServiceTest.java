package com.vijay.service;

import com.vijay.service.MLPatternDetectionService.AntiPattern;
import com.vijay.service.MLPatternDetectionService.CodeAnomaly;
import com.vijay.service.MLPatternDetectionService.CodeClone;
import com.vijay.service.MLPatternDetectionService.PatternMatch;
import com.vijay.service.MLPatternDetectionService.RefactoringOpportunity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MLPatternDetectionServiceTest {

    private MLPatternDetectionService service;

    @BeforeEach
    void setUp() {
        service = new MLPatternDetectionService();
    }

    @Test
    @DisplayName("detectDesignPatterns should detect Singleton when signature keywords are present")
    void detectDesignPatterns_singleton() {
        String code = "public class S { private static S instance; private S(){} public static S getInstance(){return instance;} }";

        List<PatternMatch> matches = service.detectDesignPatterns(code);

        assertThat(matches.stream().anyMatch(m -> m.getName().equals("Singleton"))).isTrue();
    }

    @Test
    @DisplayName("detectAntiPatterns should detect God Object when many publics and few privates")
    void detectAntiPatterns_godObject() {
        StringBuilder code = new StringBuilder("public class G {\n");
        for (int i = 0; i < 25; i++) {
            code.append("public void m" + i + "(){}\n");
        }
        code.append("}");

        List<AntiPattern> anti = service.detectAntiPatterns(code.toString());

        assertThat(anti.stream().anyMatch(a -> a.getName().equals("God Object"))).isTrue();
    }

    @Test
    @DisplayName("detectCodeClones should report clones when identical lines repeat")
    void detectCodeClones_basic() {
        String code = "int a=1;\nint a=1;\nint a=1;\nint a=1;\n";

        List<CodeClone> clones = service.detectCodeClones(code);

        assertThat(clones).isNotEmpty();
        assertThat(clones.get(0).getOccurrences()).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("detectAnomalies should include anomalies when thresholds exceeded")
    void detectAnomalies_basic() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            code.append("if(x){} // comment\n");
        }

        List<CodeAnomaly> anomalies = service.detectAnomalies(code.toString());

        assertThat(anomalies).isNotEmpty();
    }

    @Test
    @DisplayName("predictRefactoringOpportunities should combine anti-patterns and anomalies")
    void predictRefactoringOpportunities_basic() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 25; i++) {
            code.append("public void m" + i + "(){}\n");
        }

        List<RefactoringOpportunity> opps = service.predictRefactoringOpportunities(code.toString());

        assertThat(opps).isNotEmpty();
        assertThat(opps.stream().anyMatch(o -> o.getName().contains("God Object") || o.getName().contains("Long"))).isTrue();
    }
}
