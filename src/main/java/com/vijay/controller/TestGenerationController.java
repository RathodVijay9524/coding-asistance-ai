package com.vijay.controller;

import com.vijay.service.TestGenerationEngineService;
import com.vijay.service.TestGenerationEngineService.GeneratedTests;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 🧪 TEST GENERATION CONTROLLER
 *
 * REST API for generating unit, integration, and edge-case tests
 * from existing source code.
 *
 * ✅ PHASE 3.4: Test Generation - Week 14
 */
@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestGenerationController {

    private static final Logger logger = LoggerFactory.getLogger(TestGenerationController.class);
    private final TestGenerationEngineService testGenerationService;

    /**
     * Generate tests (generic endpoint)
     * POST /api/tests/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateTests(@RequestBody GenerateTestsRequest request) {
        logger.info("🧪 Generating {} tests for user: {}", request.getTestType(), request.getUserId());
        try {
            GeneratedTests generated = testGenerationService.generateTests(
                    request.getUserId(),
                    request.getLanguage(),
                    request.getTestType(),
                    request.getFramework(),
                    request.getSourceCode(),
                    request.getClassNameOverride()
            );

            return ResponseEntity.ok(toResponse(generated));
        } catch (Exception e) {
            logger.error("❌ Failed to generate tests: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to generate tests: " + e.getMessage()
            ));
        }
    }

    /**
     * Generate unit tests
     * POST /api/tests/generate-unit
     */
    @PostMapping("/generate-unit")
    public ResponseEntity<Map<String, Object>> generateUnitTests(@RequestBody GenerateTestsRequest request) {
        logger.info("🧪 Generating UNIT tests for user: {}", request.getUserId());
        try {
            GeneratedTests generated = testGenerationService.generateUnitTests(
                    request.getUserId(),
                    request.getLanguage(),
                    request.getFramework(),
                    request.getSourceCode(),
                    request.getClassNameOverride()
            );

            return ResponseEntity.ok(toResponse(generated));
        } catch (Exception e) {
            logger.error("❌ Failed to generate unit tests: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to generate unit tests: " + e.getMessage()
            ));
        }
    }

    /**
     * Generate integration tests
     * POST /api/tests/generate-integration
     */
    @PostMapping("/generate-integration")
    public ResponseEntity<Map<String, Object>> generateIntegrationTests(@RequestBody GenerateTestsRequest request) {
        logger.info("🧪 Generating INTEGRATION tests for user: {}", request.getUserId());
        try {
            GeneratedTests generated = testGenerationService.generateIntegrationTests(
                    request.getUserId(),
                    request.getLanguage(),
                    request.getFramework(),
                    request.getSourceCode(),
                    request.getClassNameOverride()
            );

            return ResponseEntity.ok(toResponse(generated));
        } catch (Exception e) {
            logger.error("❌ Failed to generate integration tests: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to generate integration tests: " + e.getMessage()
            ));
        }
    }

    /**
     * Generate edge-case tests
     * POST /api/tests/generate-edge-cases
     */
    @PostMapping("/generate-edge-cases")
    public ResponseEntity<Map<String, Object>> generateEdgeCaseTests(@RequestBody GenerateTestsRequest request) {
        logger.info("🧪 Generating EDGE CASE tests for user: {}", request.getUserId());
        try {
            GeneratedTests generated = testGenerationService.generateEdgeCaseTests(
                    request.getUserId(),
                    request.getLanguage(),
                    request.getFramework(),
                    request.getSourceCode(),
                    request.getClassNameOverride()
            );

            return ResponseEntity.ok(toResponse(generated));
        } catch (Exception e) {
            logger.error("❌ Failed to generate edge-case tests: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to generate edge-case tests: " + e.getMessage()
            ));
        }
    }

    /**
     * List supported frameworks
     * GET /api/tests/frameworks
     */
    @GetMapping("/frameworks")
    public ResponseEntity<Map<String, Object>> getSupportedFrameworks() {
        logger.info("📋 Listing supported test frameworks");
        List<String> frameworks = List.of("junit5");
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "frameworks", frameworks
        ));
    }

    // ---------------------------------------------------------------------
    // Helpers & DTOs
    // ---------------------------------------------------------------------

    private Map<String, Object> toResponse(GeneratedTests generated) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("userId", generated.getUserId());
        response.put("framework", generated.getFramework());
        response.put("testType", generated.getTestType());
        response.put("className", generated.getClassName());
        response.put("testClassName", generated.getTestClassName());
        response.put("sourceMethodCount", generated.getSourceMethodCount());
        response.put("generatedAt", generated.getGeneratedAt());
        response.put("testCode", generated.getTestCode());
        return response;
    }

    @lombok.Data
    public static class GenerateTestsRequest {
        private String userId;
        private String language;
        private String testType;        // unit, integration, edge_cases
        private String framework;       // junit5 (default)
        private String sourceCode;      // Full source code of target class
        private String classNameOverride; // Optional explicit class name
    }
}
