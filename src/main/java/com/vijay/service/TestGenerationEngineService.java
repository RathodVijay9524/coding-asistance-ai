package com.vijay.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 🧪 TEST GENERATION ENGINE SERVICE
 *
 * Generates skeleton unit and integration tests from source code.
 * Focuses on JUnit 5 style tests with Mockito-friendly structure.
 *
 * ✅ PHASE 3.4: Test Generation - Week 14
 */
@Service
@RequiredArgsConstructor
public class TestGenerationEngineService {

    private static final Logger logger = LoggerFactory.getLogger(TestGenerationEngineService.class);

    private static final String DEFAULT_TEST_FRAMEWORK = "junit5";

    /**
     * Generate tests based on type (unit/integration/edge_cases)
     */
    public GeneratedTests generateTests(String userId,
                                        String language,
                                        String testType,
                                        String framework,
                                        String sourceCode,
                                        String classNameOverride) {

        logger.info("🧪 Generating {} tests for user: {} using framework: {}", testType, userId, framework);

        if (framework == null || framework.isBlank()) {
            framework = DEFAULT_TEST_FRAMEWORK;
        }

        String className = resolveClassName(sourceCode, classNameOverride);
        List<MethodSignature> methods = extractMethodSignatures(sourceCode, language);

        String testClassName = className + "Test";
        String testCode;

        switch (Optional.ofNullable(testType).orElse("unit").toLowerCase(Locale.ROOT)) {
            case "integration":
                testCode = generateIntegrationTestClass(testClassName, className, methods, framework);
                break;
            case "edge_cases":
            case "edge":
                testCode = generateEdgeCaseTestClass(testClassName, className, methods, framework);
                break;
            default:
                testCode = generateUnitTestClass(testClassName, className, methods, framework);
        }

        GeneratedTests generated = new GeneratedTests();
        generated.setUserId(userId);
        generated.setFramework(framework);
        generated.setTestType(testType != null ? testType : "unit");
        generated.setClassName(className);
        generated.setTestClassName(testClassName);
        generated.setSourceMethodCount(methods.size());
        generated.setGeneratedAt(new Date());
        generated.setTestCode(testCode);

        return generated;
    }

    /**
     * Generate unit tests only
     */
    public GeneratedTests generateUnitTests(String userId,
                                            String language,
                                            String framework,
                                            String sourceCode,
                                            String classNameOverride) {
        return generateTests(userId, language, "unit", framework, sourceCode, classNameOverride);
    }

    /**
     * Generate integration tests only
     */
    public GeneratedTests generateIntegrationTests(String userId,
                                                   String language,
                                                   String framework,
                                                   String sourceCode,
                                                   String classNameOverride) {
        return generateTests(userId, language, "integration", framework, sourceCode, classNameOverride);
    }

    /**
     * Generate edge case tests only
     */
    public GeneratedTests generateEdgeCaseTests(String userId,
                                                String language,
                                                String framework,
                                                String sourceCode,
                                                String classNameOverride) {
        return generateTests(userId, language, "edge_cases", framework, sourceCode, classNameOverride);
    }

    // ---------------------------------------------------------------------
    // Core generation helpers
    // ---------------------------------------------------------------------

    private String resolveClassName(String sourceCode, String override) {
        if (override != null && !override.isBlank()) {
            return override.trim();
        }
        // Very simple Java class name extraction
        Pattern pattern = Pattern.compile("class\\s+([A-Za-z0-9_]+)\\s*");
        Matcher matcher = pattern.matcher(sourceCode);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "GeneratedClass";
    }

    private List<MethodSignature> extractMethodSignatures(String sourceCode, String language) {
        // For now we focus on Java-style methods: visibility returnType name(params)
        Pattern pattern = Pattern.compile("(public|protected|private)\\s+([A-Za-z0-9_<>\\[\\]]+)\\s+([A-Za-z0-9_]+)\\s*\\(([^)]*)\\)");
        Matcher matcher = pattern.matcher(sourceCode);

        List<MethodSignature> methods = new ArrayList<>();
        while (matcher.find()) {
            MethodSignature sig = new MethodSignature();
            sig.setVisibility(matcher.group(1));
            sig.setReturnType(matcher.group(2));
            sig.setName(matcher.group(3));
            sig.setParameters(rawParamsToList(matcher.group(4)));
            methods.add(sig);
        }

        // Filter out common boilerplate (getters/setters) to avoid noise
        return methods.stream()
                .filter(m -> !isGetterOrSetter(m.getName(), m.getParameters()))
                .collect(Collectors.toList());
    }

    private boolean isGetterOrSetter(String name, List<String> params) {
        if ((name.startsWith("get") || name.startsWith("is")) && params.isEmpty()) {
            return true;
        }
        if (name.startsWith("set") && params.size() == 1) {
            return true;
        }
        return false;
    }

    private List<String> rawParamsToList(String rawParams) {
        if (rawParams == null || rawParams.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(rawParams.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private String generateUnitTestClass(String testClassName,
                                         String targetClassName,
                                         List<MethodSignature> methods,
                                         String framework) {
        StringBuilder sb = new StringBuilder();
        sb.append("import org.junit.jupiter.api.Test;\n");
        sb.append("import static org.junit.jupiter.api.Assertions.*;\n\n");
        sb.append("class ").append(testClassName).append(" {\n\n");
        sb.append("    private final ").append(targetClassName).append(" target = new ").append(targetClassName).append("();\n\n");

        for (MethodSignature method : methods) {
            sb.append("    @Test\n");
            sb.append("    void ").append(method.getName()).append("_shouldBehaveAsExpected() {").append("\n");
            sb.append("        // Arrange\n");
            sb.append("        // TODO: set up inputs\n\n");
            sb.append("        // Act\n");
            if (!"void".equals(method.getReturnType())) {
                sb.append("        ").append(method.getReturnType()).append(" result = target.")
                        .append(method.getName()).append("(/* TODO: args */);").append("\n\n");
                sb.append("        // Assert\n");
                sb.append("        assertNotNull(result);").append("\n");
            } else {
                sb.append("        target.").append(method.getName()).append("(/* TODO: args */);").append("\n\n");
                sb.append("        // Assert\n");
                sb.append("        // TODO: verify side effects\n");
            }
            sb.append("    }\n\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    private String generateIntegrationTestClass(String testClassName,
                                                String targetClassName,
                                                List<MethodSignature> methods,
                                                String framework) {
        StringBuilder sb = new StringBuilder();
        sb.append("import org.junit.jupiter.api.Test;\n");
        sb.append("import org.springframework.boot.test.context.SpringBootTest;\n");
        sb.append("import org.springframework.beans.factory.annotation.Autowired;\n");
        sb.append("import static org.junit.jupiter.api.Assertions.*;\n\n");
        sb.append("@SpringBootTest\n");
        sb.append("class ").append(testClassName).append(" {\n\n");
        sb.append("    @Autowired\n");
        sb.append("    private ").append(targetClassName).append(" target;\n\n");

        for (MethodSignature method : methods) {
            sb.append("    @Test\n");
            sb.append("    void ").append(method.getName()).append("_integration() {").append("\n");
            sb.append("        // Arrange - wire real Spring context\n\n");
            sb.append("        // Act\n");
            if (!"void".equals(method.getReturnType())) {
                sb.append("        ").append(method.getReturnType()).append(" result = target.")
                        .append(method.getName()).append("(/* TODO: args */);").append("\n\n");
                sb.append("        // Assert\n");
                sb.append("        assertNotNull(result);").append("\n");
            } else {
                sb.append("        target.").append(method.getName()).append("(/* TODO: args */);").append("\n\n");
                sb.append("        // Assert\n");
                sb.append("        // TODO: verify side effects\n");
            }
            sb.append("    }\n\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    private String generateEdgeCaseTestClass(String testClassName,
                                             String targetClassName,
                                             List<MethodSignature> methods,
                                             String framework) {
        StringBuilder sb = new StringBuilder();
        sb.append("import org.junit.jupiter.api.Test;\n");
        sb.append("import static org.junit.jupiter.api.Assertions.*;\n\n");
        sb.append("class ").append(testClassName).append(" {\n\n");
        sb.append("    private final ").append(targetClassName).append(" target = new ").append(targetClassName).append("();\n\n");

        for (MethodSignature method : methods) {
            sb.append("    @Test\n");
            sb.append("    void ").append(method.getName()).append("_withEdgeCases() {").append("\n");
            sb.append("        // Arrange edge cases\n");
            sb.append("        // TODO: nulls, empty collections, boundary values\n\n");
            sb.append("        // Act & Assert\n");
            sb.append("        // TODO: call target.").append(method.getName()).append(" and assert behavior\n");
            sb.append("    }\n\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    // ---------------------------------------------------------------------
    // DTOs
    // ---------------------------------------------------------------------

    @lombok.Data
    public static class GeneratedTests {
        private String userId;
        private String framework;
        private String testType;
        private String className;
        private String testClassName;
        private int sourceMethodCount;
        private Date generatedAt;
        private String testCode;
    }

    @lombok.Data
    public static class MethodSignature {
        private String visibility;
        private String returnType;
        private String name;
        private List<String> parameters;
    }
}
