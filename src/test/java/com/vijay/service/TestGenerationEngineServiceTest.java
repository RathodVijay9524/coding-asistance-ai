package com.vijay.service;

import com.vijay.service.TestGenerationEngineService.GeneratedTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestGenerationEngineServiceTest {

    private TestGenerationEngineService testGenerationEngineService;

    @BeforeEach
    void setUp() {
        testGenerationEngineService = new TestGenerationEngineService();
    }

    @Test
    @DisplayName("generateUnitTests should create JUnit5 test class with methods")
    void generateUnitTests_shouldCreateUnitTestClass() {
        // Arrange
        String userId = "user123";
        String sourceCode = "public class Calculator {\n" +
                "  public int add(int a, int b) { return a + b; }\n" +
                "  public void reset() { }\n" +
                "}";

        // Act
        GeneratedTests generated = testGenerationEngineService.generateUnitTests(
                userId,
                "java",
                "junit5",
                sourceCode,
                null
        );

        String testCode = generated.getTestCode();

        // Assert
        assertThat(generated.getClassName()).isEqualTo("Calculator");
        assertThat(generated.getTestClassName()).isEqualTo("CalculatorTest");
        assertThat(testCode).contains("import org.junit.jupiter.api.Test;");
        assertThat(testCode).contains("class CalculatorTest");
        assertThat(testCode).contains("void add_shouldBehaveAsExpected()");
        assertThat(testCode).contains("void reset_shouldBehaveAsExpected()");
    }

    @Test
    @DisplayName("generateTests should use classNameOverride when provided")
    void generateTests_shouldUseClassNameOverride() {
        // Arrange
        String userId = "user123";
        String sourceCode = "public class IgnoredName { public void m() {} }";

        // Act
        GeneratedTests generated = testGenerationEngineService.generateTests(
                userId,
                "java",
                "unit",
                "junit5",
                sourceCode,
                "CustomName"
        );

        // Assert
        assertThat(generated.getClassName()).isEqualTo("CustomName");
        assertThat(generated.getTestClassName()).isEqualTo("CustomNameTest");
        assertThat(generated.getTestCode()).contains("class CustomNameTest");
    }

    @Test
    @DisplayName("generateIntegrationTests should add SpringBootTest annotation and autowired field")
    void generateIntegrationTests_shouldAddSpringBootAnnotations() {
        // Arrange
        String userId = "user123";
        String sourceCode = "public class UserService { public String find(String id) { return \"x\"; } }";

        // Act
        GeneratedTests generated = testGenerationEngineService.generateIntegrationTests(
                userId,
                "java",
                "junit5",
                sourceCode,
                null
        );

        String testCode = generated.getTestCode();

        // Assert
        assertThat(testCode).contains("@SpringBootTest");
        assertThat(testCode).contains("@Autowired");
        assertThat(testCode).contains("private UserService target;");
        assertThat(testCode).contains("void find_integration()");
    }

    @Test
    @DisplayName("generateEdgeCaseTests should create edge case test methods")
    void generateEdgeCaseTests_shouldCreateEdgeCaseTests() {
        // Arrange
        String userId = "user123";
        String sourceCode = "public class StringUtils { public String reverse(String s) { return s; } }";

        // Act
        GeneratedTests generated = testGenerationEngineService.generateEdgeCaseTests(
                userId,
                "java",
                "junit5",
                sourceCode,
                null
        );

        String testCode = generated.getTestCode();

        // Assert
        assertThat(testCode).contains("class StringUtilsTest");
        assertThat(testCode).contains("void reverse_withEdgeCases()");
        assertThat(testCode).contains("// Arrange edge cases");
    }

    @Test
    @DisplayName("generateUnitTests should skip trivial getters and setters")
    void generateUnitTests_shouldSkipGettersAndSetters() {
        // Arrange
        String userId = "user123";
        String sourceCode = "public class Person {\n" +
                "  private String name;\n" +
                "  public String getName() { return name; }\n" +
                "  public void setName(String name) { this.name = name; }\n" +
                "  public String fullName(String prefix) { return prefix + name; }\n" +
                "}";

        // Act
        GeneratedTests generated = testGenerationEngineService.generateUnitTests(
                userId,
                "java",
                "junit5",
                sourceCode,
                null
        );

        String testCode = generated.getTestCode();

        // Assert
        assertThat(testCode).contains("void fullName_shouldBehaveAsExpected()");
        assertThat(testCode).doesNotContain("getName_shouldBehaveAsExpected()");
        assertThat(testCode).doesNotContain("setName_shouldBehaveAsExpected()");
    }
}
