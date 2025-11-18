package com.vijay.service;

import com.vijay.service.ASTAnalysisService.ClassInfo;
import com.vijay.service.ASTAnalysisService.CodeDependencies;
import com.vijay.service.ASTAnalysisService.CodeSmell;
import com.vijay.service.ASTAnalysisService.MethodCallGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ASTAnalysisServiceTest {

    private ASTAnalysisService service;

    @BeforeEach
    void setUp() {
        service = new ASTAnalysisService();
    }

    @Test
    @DisplayName("extractClassInfo should populate name, methods, fields, complexity, and linesOfCode")
    void extractClassInfo_basic() {
        String code = "" +
                "public class Demo extends Base implements A, B {\n" +
                "  private int value;\n" +
                "  public void m1() {}\n" +
                "  private String m2(int x) { return \"ok\"; }\n" +
                "}\n";

        ClassInfo info = service.extractClassInfo(code);

        assertThat(info.getName()).isEqualTo("Demo");
        assertThat(info.getParentClass()).isEqualTo("Base");
        assertThat(info.getInterfaces().stream().map(String::trim)).contains("A", "B");
        assertThat(info.getFields()).hasSize(1);
        assertThat(info.getMethods()).hasSize(2);
        assertThat(info.getComplexity()).isGreaterThanOrEqualTo(1);
        assertThat(info.getLinesOfCode()).isGreaterThan(0);
    }

    @Test
    @DisplayName("calculateCyclomaticComplexity should increase with decision points")
    void calculateCyclomaticComplexity_countsDecisions() {
        String simple = "class A { void m(){ } }";
        String complex = "class A { void m(){ if(true){} for(int i=0;i<10;i++){} while(false){} } }";

        int simpleCc = service.calculateCyclomaticComplexity(simple);
        int complexCc = service.calculateCyclomaticComplexity(complex);

        assertThat(simpleCc).isGreaterThanOrEqualTo(1);
        assertThat(complexCc).isGreaterThan(simpleCc);
    }

    @Test
    @DisplayName("detectCodeSmells should flag long methods or high complexity")
    void detectCodeSmells_longMethod() {
        StringBuilder code = new StringBuilder("class A { void big(){\n");
        for (int i = 0; i < 60; i++) {
            code.append("if(true){}\n");
        }
        code.append("}\n}");

        List<CodeSmell> smells = service.detectCodeSmells(code.toString());

        assertThat(smells).isNotEmpty();
        assertThat(smells.stream().anyMatch(s -> s.getType().equals("Long Method") || s.getType().equals("High Complexity")))
                .isTrue();
    }

    @Test
    @DisplayName("generateCallGraph should create nodes for methods and their calls")
    void generateCallGraph_basic() {
        String code = "class A { void a(){ b(); } void b(){ } }";

        MethodCallGraph graph = service.generateCallGraph(code);

        assertThat(graph.getNodeCount()).isGreaterThanOrEqualTo(2);
        assertThat(graph.getNodes().get("a")).contains("b");
    }

    @Test
    @DisplayName("analyzeDependencies should collect imports and external classes")
    void analyzeDependencies_basic() {
        String code = "import java.util.List;\nclass A { void m(){ new String(\"x\"); } }";

        CodeDependencies deps = service.analyzeDependencies(code);

        assertThat(deps.getImportCount()).isGreaterThanOrEqualTo(1);
        assertThat(deps.getExternalClasses()).contains("String");
    }
}
