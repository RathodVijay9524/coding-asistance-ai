# Part 5 – Intelligent Editing & AI Features

This part summarizes the AI / advanced editing features primarily located in `com.vijay.editing`.

## 5.1 Goals

- Provide intelligent, context-aware suggestions.
- Enable semantic code search and pattern extraction.
- Support automated test generation and code transformation.
- Integrate with external AI models via Spring AI tools (Phase 2).

## 5.2 Key Editing Services

> Note: These are Phase 2.x components that complement Phase 3.

### 5.2.1 `InlineSuggestionEngine` (editing)

- Real-time inline suggestions while typing.
- Rule-based + AI-powered heuristics.
- Types of suggestions:
  - Method extraction
  - Variable renaming
  - Code simplification
  - Pattern application
  - Bug fix hints

### 5.2.2 `TestGenerationService` (editing, AI tool)

- Annotated with `@Tool` from Spring AI.
- Generates tests using LLM-based reasoning.
- Focused on:
  - Generating test cases from method signatures
  - Inferring edge cases
  - Proposing assertions

> Distinct from `TestGenerationEngineService` which is REST-based and template-driven.

### 5.2.3 `CodeSelectionAnalyzer`

- Analyzes selected code blocks.
- Identifies:
  - Language, methods, classes, variables
  - Patterns and anti-patterns
  - Code smells and potential issues

### 5.2.4 `EditSuggestionGenerator`

- Takes selected code + user instructions.
- Produces structured edit suggestions.
- Uses `CodeSelectionAnalyzer` and other services.

### 5.2.5 `CodeTransformationEngine`

- Applies code transformations safely.
- Supports single-file and multi-file transformations.
- Uses rule definitions and, optionally, LLM assistance.

### 5.2.6 `DependencyGraphAnalyzer`

- Builds/uses project dependency graphs.
- Detects:
  - Circular dependencies
  - Highly coupled modules
  - Refactoring opportunities

### 5.2.7 `PatternExtractor`

- Extracts design patterns and naming conventions.
- Detects:
  - Singleton, Factory, Strategy, Template Method, Decorator patterns
  - Common code structure patterns

### 5.2.8 `SemanticCodeSearch`

- Intent-based code search.
- Integrates with vector store (semantic embeddings).
- Finds code by meaning rather than just keywords.

## 5.3 Relationship to Phase 3

- Phase 3.1 + 3.2 provide **data**: edits, patterns, feedback.
- Phase 3.3 leverages this data for **better suggestions**.
- Phase 3.4 leverages code structure, and can later be enhanced with AI-based test creation.
- The `com.vijay.editing` services can consume feedback (Phase 3.2) to tune their behavior over time.
