# Part 6 – Test Generation Engine (Phase 3.4)

## 6.1 Purpose

The **Test Generation Engine** provides endpoints and a service for creating JUnit 5–style test skeletons from existing Java source code.

It focuses on **speeding up test creation** by generating:
- Unit tests
- Integration tests
- Edge case tests

## 6.2 Core Service: `TestGenerationEngineService`

Located at: `com.vijay.service.TestGenerationEngineService`

### Responsibilities

- Accepts source code + meta-data.
- Extracts class name and method signatures.
- Filters out trivial methods (getters/setters).
- Generates test class code based on requested `testType`:
  - `unit`
  - `integration`
  - `edge_cases`

### Key Methods

- `generateTests(userId, language, testType, framework, sourceCode, classNameOverride)`
- `generateUnitTests(...)`
- `generateIntegrationTests(...)`
- `generateEdgeCaseTests(...)`

### DTOs

- `GeneratedTests`
  - `userId`
  - `framework`
  - `testType`
  - `className`
  - `testClassName`
  - `sourceMethodCount`
  - `generatedAt`
  - `testCode`

- `MethodSignature`
  - `visibility`
  - `returnType`
  - `name`
  - `parameters`

## 6.3 Controller: `TestGenerationController`

Located at: `com.vijay.controller.TestGenerationController`

Base path: `/api/tests`

### Endpoints

- `POST /api/tests/generate`
- `POST /api/tests/generate-unit`
- `POST /api/tests/generate-integration`
- `POST /api/tests/generate-edge-cases`
- `GET  /api/tests/frameworks`

### Request DTO

`GenerateTestsRequest`:
- `userId`
- `language` (e.g. `java`)
- `testType` (`unit`, `integration`, `edge_cases`)
- `framework` (`junit5`)
- `sourceCode` (full class code)
- `classNameOverride` (optional explicit class name)

## 6.4 Generated Test Styles

### Unit Test Example

Input:
```java
public class Calculator {
    public int add(int a, int b) {
        return a + b;
    }
}
```

Output (simplified):
```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalculatorTest {

    private final Calculator target = new Calculator();

    @Test
    void add_shouldBehaveAsExpected() {
        // Arrange
        // TODO: set up inputs

        // Act
        int result = target.add(/* TODO: args */);

        // Assert
        assertNotNull(result);
    }
}
```

### Integration Test Example

```java
@SpringBootTest
class CalculatorTest {

    @Autowired
    private Calculator target;

    @Test
    void add_integration() {
        // Arrange - real Spring context
        // Act
        int result = target.add(/* TODO: args */);
        // Assert
        assertNotNull(result);
    }
}
```

### Edge Case Test Example

```java
class CalculatorTest {

    private final Calculator target = new Calculator();

    @Test
    void add_withEdgeCases() {
        // Arrange edge cases
        // TODO: nulls, boundaries, negatives, large values

        // Act & Assert
        // TODO: call target.add(...) and assert behavior
    }
}
```

## 6.5 Relationship to AI-Based Test Generation

- `TestGenerationEngineService` is **template-driven**, deterministic, and does not depend on an external AI model.
- `com.vijay.editing.TestGenerationService` (Phase 2.x) is **AI-based**, using `@Tool` and LLM integration for more sophisticated test proposals.
- Future work can combine both: use AI to enrich or refine templates from the engine.
