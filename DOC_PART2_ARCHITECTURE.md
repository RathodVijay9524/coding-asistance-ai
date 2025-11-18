# Part 2 – Architecture & Modules

## 2.1 High-Level Architecture

The application follows a layered architecture:

- **Controller Layer (`com.vijay.controller`)**  
  REST APIs for edits, feedback, inline suggestions, and test generation.

- **Service Layer (`com.vijay.service`)**  
  Business logic for:
  - Edit history & user patterns
  - Suggestion feedback analytics
  - Inline suggestion engine (rule-driven, persistence-aware)
  - Test generation engine (JUnit 5 skeletons)

- **Editing / AI Layer (`com.vijay.editing`)**  
  AI-integrated services and tools (Phase 2.x) for:
  - Code selection analysis
  - Suggestion generation
  - Code transformation
  - Dependency graph analysis
  - Pattern extraction
  - Semantic code search
  - Test generation via tools

- **Persistence Layer (`com.vijay.model`, `com.vijay.repository`)**  
  JPA entities and Spring Data repositories for:
  - `EditHistory`
  - `UserPattern`
  - `SuggestionFeedback`

- **Configuration (`com.vijay.config`)**  
  JPA repository scanning and transaction management.

## 2.2 Main Packages

- `com.vijay.controller`
  - `EditHistoryController`
  - `SuggestionFeedbackController`
  - `InlineSuggestionController`
  - `TestGenerationController`

- `com.vijay.service`
  - `EditHistoryService`
  - `SuggestionFeedbackService`
  - `InlineSuggestionEngineService`
  - `TestGenerationEngineService`

- `com.vijay.editing`
  - `InlineSuggestionEngine`
  - `TestGenerationService` (AI tool-based)
  - `CodeSelectionAnalyzer`
  - `EditSuggestionGenerator`
  - `CodeTransformationEngine`
  - `DependencyGraphAnalyzer`
  - `PatternExtractor`
  - `SemanticCodeSearch`
  - `InlineCodeEditor`

- `com.vijay.model`
  - `EditHistory`
  - `UserPattern`
  - `SuggestionFeedback`

- `com.vijay.repository`
  - `EditHistoryRepository`
  - `UserPatternRepository`
  - `SuggestionFeedbackRepository`

- `com.vijay.config`
  - `RepositoryConfig`

## 2.3 Technology Stack

- **Language:** Java (Spring Boot 3.x)
- **Web Framework:** Spring Boot (REST)
- **Persistence:** Spring Data JPA, Hibernate
- **Database:** MySQL (`coding-assistance` schema)
- **Migrations:** Flyway
- **Connection Pool:** HikariCP
- **Logging:** SLF4J + Spring Boot logging
- **Testing Target:** JUnit 5, Spring Boot test
- **AI/Tools:** Spring AI tools, `@Tool`-annotated methods in `com.vijay.editing`
