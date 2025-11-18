# Part 3 – Database & Persistence Layer

## 3.1 JPA Entities

### 3.1.1 `EditHistory`
Tracks individual edits:
- `id` (PK)
- `userId`
- `filePath`
- `originalCode`
- `editedCode`
- `editType` (e.g. `rename_method`, `extract_method`)
- `suggestionSource` (e.g. `AI`, `manual`)
- `accepted` (Boolean)
- `description`
- `createdAt`, `updatedAt`

### 3.1.2 `UserPattern`
Captures user-level editing patterns:
- `id` (PK)
- `userId`
- `patternType` (e.g. `extract_method`, `rename_variable`)
- `frequency` (number of occurrences)
- `acceptanceRate` (0–1)
- `active` (Boolean)
- `createdAt`, `updatedAt`

### 3.1.3 `SuggestionFeedback`
Stores feedback on suggestions:
- `id` (PK)
- `userId`
- `suggestionId`
- `suggestionType`
- `suggestionContent`
- `rating` (1–5)
- `action` (e.g. `accepted`, `rejected`, `modified`, `ignored`)
- `feedback` (free text)
- `userModification`
- `reason`
- `helpful`, `relevant`, `accurate` (Booleans)
- `sentiment` (e.g. `positive`, `neutral`, `negative`)
- `createdAt`, `updatedAt`

All entities are annotated with `jakarta.persistence` and have indexes declared via `@Table(indexes = ...)`.

## 3.2 Repositories

### 3.2.1 `EditHistoryRepository`
- Basic CRUD (`JpaRepository<EditHistory, Long>`)
- Custom queries:
  - Find by `userId`, `filePath`, `editType`
  - Date-range queries
  - Recent edits
  - Aggregations for statistics

### 3.2.2 `UserPatternRepository`
- Basic CRUD (`JpaRepository<UserPattern, Long>`)
- Methods:
  - `findByUserIdAndPatternType(...)`
  - `findByUserIdAndActiveTrue(...)`
  - `findHighAcceptancePatterns(...)`
  - `findMostFrequentPatterns(...)`
  - `findLowAcceptancePatterns(...)`

### 3.2.3 `SuggestionFeedbackRepository`
- Basic CRUD (`JpaRepository<SuggestionFeedback, Long>`)
- Methods:
  - `findByUserId(...)`, `findBySuggestionId(...)`
  - `findByUserIdAndRating(...)`
  - `findByUserIdAndAction(...)`
  - `findByUserIdAndHelpfulTrue/False(...)`
  - `findByUserIdAndSentiment(...)`
  - Date-range queries
- Aggregations:
  - `getAverageRating(...)`
  - `getAcceptanceRate(...)`
  - `getHelpfulPercentage(...)`
  - `getMostCommonSentiment(...)`

## 3.3 Database Schema (MySQL)

Database: `coding-assistance`

Tables:
- `edit_history`
- `user_pattern`
- `suggestion_feedback`

Views (examples):
- `v_user_edit_stats`
- `v_edit_type_stats`
- `v_suggestion_source_stats`
- `v_user_pattern_stats`
- `v_feedback_stats`

Indexes:
- On `user_id`, `created_at`, `edit_type`, `rating`, `suggestion_id`, etc. for fast analytics.

## 3.4 Migrations (Flyway)

File: `src/main/resources/db/migration/V1__Create_Edit_History_Tables.sql`

Responsibilities:
- Create core tables with constraints
- Add helpful indexes
- Create analytical views
- Create at least one stored procedure for reporting/aggregation

## 3.5 JPA & DataSource Configuration

In `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/coding-assistance?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=YOUR_USER
spring.datasource.password=YOUR_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=validate

spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

spring.datasource.hikari.maximum-pool-size=10
```

In `RepositoryConfig`:
- `@EnableJpaRepositories(basePackages = "com.vijay.repository")`
- `@EnableTransactionManagement`
