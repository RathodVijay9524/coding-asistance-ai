# Neo4j Graph Integration for CodeGraphService

This document describes how to integrate **Neo4j** as a backend for the `CodeGraphService` used by the Hybrid Brain / GraphRAG layer.

The goal is to keep your current JPA database **unchanged** and add Neo4j **alongside** it, dedicated to storing and querying the **code graph** (classes, services, advisors, controllers and their relationships).

---

## 1. Current State

Today the system has:

- A `CodeGraphService` interface:
  - `List<String> getRelatedNodes(String nodeName)`
  - `List<String> getImpactRadius(String nodeName, int maxDepth, int maxNodes)`
- An `InMemoryCodeGraphService` implementation which:
  - Scans `src/main/java` on startup.
  - Derives a node for each `.java` file (class name).
  - Uses simple regex matching to detect references to other `*Service`, `*Advisor`, `*Controller` classes.
  - Builds a directed adjacency map in memory.
- `DynamicContextAdvisor` integration:
  - For `REFACTOR` / `ARCHITECTURE` / `IMPLEMENTATION` focus areas, it:
    - Extracts a primary code symbol from the user query (e.g. `ChatService`, `ConductorAdvisor`).
    - Calls `codeGraphService.getImpactRadius(symbol, 2, 10)`.
    - Logs and stores the impact radius in `GlobalBrainContext` as `codeGraphImpact`.

This means the system already behaves like a **GraphRAG stub** using local source code, without any external database.

Neo4j is an **optional upgrade** to make this graph persistent, richer, and more queryable.

---

## 2. Why Add Neo4j?

Neo4j gives you:

- **Persistent graph storage** across restarts.
- **Richer relationships**:
  - `(:CodeNode)-[:CALLS]->(:CodeNode)`
  - `(:CodeNode)-[:DEPENDS_ON]->(:CodeNode)`
  - `(:CodeNode)-[:EXTENDS]->(:CodeNode)`
  - `(:CodeNode)-[:IMPLEMENTS]->(:CodeNode)`
- **Powerful Cypher queries** to:
  - Traverse multiple hops.
  - Filter by attributes (package, layer, type, complexity).
  - Compute impact radius, fan-in/out, hotspots.
- **Better tooling**:
  - Neo4j Browser / Bloom to visualize the code graph.

This is especially useful for:

- Large projects where in-memory scanning is too slow or too big.
- Advanced refactoring and architecture analysis queries.

---

## 3. High-Level Architecture with Neo4j

You keep your existing stack:

- **Relational DB + JPA** → normal app/business data.
- **Neo4j** → code graph only.

`CodeGraphService` remains the **abstraction layer**. You introduce a new implementation:

- `Neo4jCodeGraphService implements CodeGraphService`
- Internally uses the Neo4j Java driver or Spring Data Neo4j.
- `DynamicContextAdvisor` and other brains do **not** change; they only depend on `CodeGraphService`.

At runtime you can choose which implementation to use:

- **Development / lightweight** → `InMemoryCodeGraphService`.
- **Production / advanced** → `Neo4jCodeGraphService`.

This can be controlled by Spring profiles or bean priorities.

---

## 4. Dependencies and Configuration

### 4.1 Maven Dependency

Add the Neo4j starter to `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-neo4j</artifactId>
</dependency>
```

You keep your existing JPA dependencies as they are.

### 4.2 Application Configuration

In `application.yml` (or `application.properties`):

```yaml
spring:
  neo4j:
    uri: bolt://localhost:7687
    authentication:
      username: neo4j
      password: YOUR_PASSWORD
```

This section is **independent** from your existing `spring.datasource.*` configuration for JPA.

Make sure a Neo4j server is running locally or remotely with the configured URI and credentials.

---

## 5. Graph Model in Neo4j

A simple starting model for the code graph:

- **Nodes**:
  - Label: `CodeNode`
  - Properties:
    - `name` (e.g. `ChatService`, `ConductorAdvisor`)
    - `kind` (e.g. `SERVICE`, `ADVISOR`, `CONTROLLER`, etc.)
    - Optional: `package`, `filePath`, `layer` (web/service/repo), etc.

- **Relationships** (examples):
  - `(:CodeNode {name:"ChatService"})-[:CALLS]->(:CodeNode {name:"ConductorAdvisor"})`
  - `(:CodeNode {name:"ChatService"})-[:DEPENDS_ON]->(:CodeNode {name:"ToolFinderService"})`
  - `(:CodeNode {name:"SelfRefineV3Advisor"})-[:USES]->(:CodeNode {name:"SupervisorBrain"})`

You can expand the relationship types later as you add a richer analyzer.

---

## 6. Implementing `Neo4jCodeGraphService`

Below is a conceptual design; exact code can be added later.

### 6.1 Service Skeleton

```java
package com.vijay.service;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Profile("neo4j-graph")
public class Neo4jCodeGraphService implements CodeGraphService {

    private final Driver driver;

    public Neo4jCodeGraphService(Driver driver) {
        this.driver = driver;
    }

    @Override
    public List<String> getRelatedNodes(String nodeName) {
        String cypher = """
            MATCH (n:CodeNode {name: $name})-[:CALLS|DEPENDS_ON|USES]->(m:CodeNode)
            RETURN DISTINCT m.name AS name
            """;

        try (Session session = driver.session()) {
            return session.readTransaction(tx ->
                tx.run(cypher, java.util.Map.of("name", nodeName))
                  .list(r -> r.get("name").asString())
            );
        }
    }

    @Override
    public List<String> getImpactRadius(String nodeName, int maxDepth, int maxNodes) {
        String cypher = """
            MATCH p = (n:CodeNode {name: $name})-[:CALLS|DEPENDS_ON|USES*1..$depth]->(m:CodeNode)
            RETURN DISTINCT m.name AS name
            LIMIT $limit
            """;

        try (Session session = driver.session()) {
            return session.readTransaction(tx ->
                tx.run(cypher, java.util.Map.of(
                        "name", nodeName,
                        "depth", maxDepth,
                        "limit", maxNodes
                )).list(r -> r.get("name").asString())
            );
        }
    }
}
```

Key points:

- Annotated with `@Service` + `@Profile("neo4j-graph")` so you can enable it via Spring profile.
- Uses the low-level `org.neo4j.driver.Driver` to run Cypher.
- Implements the same `CodeGraphService` interface as the in-memory version.

### 6.2 Driver Configuration

If you use `spring-boot-starter-data-neo4j`, Spring Boot can auto-configure the `Driver` bean using `spring.neo4j.*` settings.

If you prefer manual configuration, you can define:

```java
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Neo4jConfig {

    @Bean
    public Driver neo4jDriver() {
        return GraphDatabase.driver(
            "bolt://localhost:7687",
            AuthTokens.basic("neo4j", "YOUR_PASSWORD")
        );
    }
}
```

---

## 7. Populating the Neo4j Code Graph

You have two main options:

### 7.1 Startup Indexer (Inside This App)

- Reuse your current source scanner logic from `InMemoryCodeGraphService`:
  - Walk `src/main/java`.
  - For each class, infer `kind` from the name (`*Service`, `*Advisor`, `*Controller`, etc.).
  - Detect references to other classes in the same way.
- Instead of (or in addition to) populating the in-memory adjacency map, write Cypher to Neo4j:

  ```cypher
  MERGE (a:CodeNode {name: $from})
  MERGE (b:CodeNode {name: $to})
  MERGE (a)-[:DEPENDS_ON]->(b);
  ```

- Implement this as a Spring `@Component` that runs on startup (e.g. `CommandLineRunner` or `ApplicationRunner`).

### 7.2 Offline Indexer (Separate Tool)

- A separate small Java or Kotlin CLI app that:
  - Scans your repository.
  - Connects to Neo4j.
  - Populates nodes/edges.
- Advantage: you can run it periodically or as part of CI without touching the main app.

Both approaches are compatible with your current `CodeGraphService` interface.

---

## 8. Switching Between In-Memory and Neo4j

There are multiple ways to choose which implementation backs `CodeGraphService`:

### 8.1 Spring Profiles

- `InMemoryCodeGraphService` → no profile (or `@Profile("!neo4j-graph")`).
- `Neo4jCodeGraphService` → `@Profile("neo4j-graph")`.

Activate Neo4j mode by starting the app with:

```bash
SPRING_PROFILES_ACTIVE=neo4j-graph
```

### 8.2 `@Primary` Bean

- Mark Neo4j implementation as `@Primary` when you are ready to default to it.
- Keep in-memory implementation for tests or local dev.

---

## 9. How It Interacts with the Existing Architecture

- `DynamicContextAdvisor` still calls:

  ```java
  List<String> impact = codeGraphService.getImpactRadius(mainNode, 2, 10);
  ```

- `KnowledgeGraphAdvisor` and future brains can also read `codeGraphImpact` from `GlobalBrainContext` and/or call `CodeGraphService` directly.
- `ChatService`, ReAct loop, SelfRefine, etc. are **unchanged**.
- Your JPA-based relational database remains responsible for:
  - Tokens, logs, users, conversations, etc.
- Neo4j is responsible only for:
  - Code structure graph and relationships.

---

## 10. Recommended Migration Path

1. **Keep current implementation (already done)**
   - `CodeGraphService` + `InMemoryCodeGraphService` scanning `src/main/java`.
   - `DynamicContextAdvisor` using `getImpactRadius` for refactor/architecture queries.

2. **Add Neo4j dependencies and configuration (no behavior change yet)**
   - Add `spring-boot-starter-data-neo4j`.
   - Configure `spring.neo4j.uri` and credentials.

3. **Implement `Neo4jCodeGraphService` (read-only first)**
   - Implement `getRelatedNodes` / `getImpactRadius` using Cypher.
   - Manually load a small test graph into Neo4j for experiments.

4. **Add a startup or offline indexer**
   - Reuse your existing source scanning patterns.
   - Write nodes + relationships into Neo4j.

5. **Switch DynamicContextAdvisor to use Neo4j-backed CodeGraphService**
   - Enable the `neo4j-graph` profile in staging.
   - Compare logs and behavior (impact radius, performance).

6. **Gradually enhance the graph model**
   - Add method-level nodes if needed.
   - Add relationship types (`CALLS`, `RETURNS`, `USES_TOOL`, etc.).
   - Add attributes like package, layer, metrics.

This approach lets you **keep everything working today** while giving you a clear path to a powerful Neo4j-backed GraphRAG system when you are ready.
