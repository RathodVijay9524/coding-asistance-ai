package com.vijay.editing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestGenerationServiceTest {

    private ObjectMapper objectMapper;
    private TestGenerationService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new TestGenerationService(objectMapper);
    }

    @Test
    @DisplayName("generateUnitTests should produce happy, edge, and exception tests with coverage")
    void generateUnitTests_basic() throws Exception {
        String methodCode = "public int add(int a, int b) throws IllegalArgumentException {\n" +
                "  if (a < 0) throw new IllegalArgumentException();\n" +
                "  return a + b;\n" +
                "}";

        String json = service.generateUnitTests(methodCode, "Calculator", "add");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("testCount").asInt()).isEqualTo(4); // 1 happy + 2 edge + 1 exception
        assertThat(root.get("coverage").asText()).isEqualTo("60%");

        JsonNode testClass = root.get("testClass");
        assertThat(testClass.asText()).contains("CalculatorTest");
    }

    @Test
    @DisplayName("generateIntegrationTests should include DB, API, and service integration tests when dependencies present")
    void generateIntegrationTests_withDependencies() throws Exception {
        String methodCode = "public void save(User user) { repository.save(user); // http call }";
        String dependencies = "database,api,service";

        String json = service.generateIntegrationTests(methodCode, "UserService", dependencies);

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("testCount").asInt()).isEqualTo(3);

        JsonNode testClass = root.get("testClass");
        assertThat(testClass.asText()).contains("UserServiceIntegrationTest");
    }

    @Test
    @DisplayName("generateMocks should create mocks and setup code for dependencies")
    void generateMocks_basic() throws Exception {
        String classCode = "public class Demo { }";
        String dependencies = "UserRepository,NotificationService";

        String json = service.generateMocks(classCode, dependencies);

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("mockCount").asInt()).isEqualTo(2);

        JsonNode mocks = root.get("mocks");
        assertThat(mocks.isArray()).isTrue();
        assertThat(mocks.size()).isEqualTo(2);

        String setupCode = root.get("mockSetupCode").asText();
        assertThat(setupCode).contains("MockitoAnnotations.initMocks");
    }

    @Test
    @DisplayName("generateTestSuite should create tests for each public method in class")
    void generateTestSuite_basic() throws Exception {
        String classCode = "public class Demo {\n" +
                "  public void foo() {}\n" +
                "  public int bar() { return 1; }\n" +
                "}";

        String json = service.generateTestSuite(classCode, "Demo");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("methodCount").asInt()).isEqualTo(2);
        assertThat(root.get("testCaseCount").asInt()).isEqualTo(2);
        assertThat(root.get("estimatedCoverage").asText()).isEqualTo("85%");

        JsonNode testClass = root.get("testClass");
        assertThat(testClass.asText()).contains("DemoTest");
    }
}
