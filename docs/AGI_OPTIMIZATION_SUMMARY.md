# AGI Optimization Summary – Hybrid Brain Architecture

This document summarizes the AGI-style optimization ideas from the long roadmap text and maps them to your existing Hybrid Brain system.

It answers three questions:

- What do you already have?
- What should you add or change?
- In what order should you implement them?

---

## 1. What You Already Have

Your system already has a strong foundation for AGI-style behaviour:

- **Multi-provider Chat Clients** (OpenAI, Ollama, Anthropic, Google, HF) via `AIProviderConfig`.
- **Hybrid Brain advisor chain**:
  - `ConversationHistoryAdvisor` + `MessageChatMemoryAdvisor` – short-term memory.
  - `ConductorAdvisor` (Brain 0) – master planner, fast-path logic.
  - `DynamicContextAdvisor` (Brain 1) – dynamic context & Brain/Tool selection.
  - `ToolCallAdvisor` (Brain 2) – plan-aware tool enforcement.
  - `SelfRefineV3Advisor` + `MultiCriteriaJudgeAdvisor` – quality and self-critique.
  - `PersonalityAdvisor` (Brain 14) – tone & personality.
  - `SupervisorBrain` – global state & performance tracking.
- **Tool RAG** – tool indexing & semantic search with `ToolIndexingService` + `ToolFinderService`.
- **Brain RAG** – brain indexing & semantic selection with `BrainIndexerService` + `BrainFinderService`.
- **Code vector stores** – `summaryVectorStore`, `chunkVectorStore` for project understanding.
- **Phase docs** and human-like improvement plans already written in `docs/`.

This means you are already beyond a simple chatbot and have many of the building blocks for an AGI-like coding assistant.

---

## 2. Key AGI Ideas from the Roadmap

The long “AGI optimization” document proposes several major upgrades:

1. **Hierarchical Memory** – short-term + episodic summaries + project knowledge graph.
2. **Visible Chain-of-Thought Streaming** – show thinking before final answer.
3. **Reflection + User Profile** – learn from corrections and store user preferences.
4. **ReAct Agent Loop** – Plan → Act (tool) → Observe → Re-plan → Repeat before answering.
5. **Sandbox Code Execution** – run and fix code in a sandbox before returning it.
6. **Asynchronous / Speculative Brains** – run some RAG/search in parallel.
7. **Fast vs Slow Routing** – system-1 vs system-2 models and advisor chains.
8. **Clarification Brain** – ask questions when ambiguity is high instead of guessing.
9. **Predictive Context Loading** – pre-fetch likely next context (e.g. related files).

Not all of these need to be implemented at once; some map directly to your existing components.

---

## 3. Mapping to Your System – What to Add/Change

### 3.1 Hierarchical Memory (Short-Term + Episodic + Project Graph)

**Already have:**

- Short-term: `MessageChatMemoryAdvisor` with `ChatMemory` (last 20 messages).
- Project-level: `summaryVectorStore`, `chunkVectorStore`, code indexers.

**Add/clarify:**

- Formalize three tiers:
  - **Working memory:** recent messages (existing).
  - **Episodic memory:** conversation summaries stored in a dedicated vector store.
  - **Project graph:** simple dependency/relationship graph over code (controller→service→repo, etc.), which later becomes a **GraphRAG** layer.
- Introduce a service like `ProjectKnowledgeGraphService` or `CodeGraphService` to expose relationships and answer graph queries:
  - Nodes: classes, methods, interfaces, database tables.
  - Edges: `EXTENDS`, `IMPLEMENTS`, `CALLS`, `RETURNS`, `DEPENDS_ON`.
- Let `DynamicContextAdvisor` and planners use this structure rather than only raw search, e.g. for "Fix `OrderService`" it can fetch `OrderService` + its callers + its repositories.

### 3.2 Visible Chain-of-Thought Streaming

**Already have:**

- `ConductorAdvisor` and `ChainOfThoughtPlannerAdvisor` doing internal reasoning.

**Add:**

- Allow the planner/judge to produce a **separate “thinking” block** and stream it to the frontend as a grey “Thought Bubble” before final answer.
- This is primarily a **frontend + streaming** enhancement and minimal backend changes.

### 3.3 Reflection & User Profile

**Already have:**

- `SelfRefineV3Advisor` and `MultiCriteriaJudgeAdvisor` evaluating answers.
- Planning for `UserProfile` in `HUMAN_LIKE_IMPROVEMENTS.md`.

**Add:**

- A real `UserProfile` store & service for preferences (e.g. Java 17 vs 8, coding style).
- A small feedback hook that, when user corrects the system, records a **lesson** into profile.
- Let `DynamicContextAdvisor` and `ConductorAdvisor` read profile (e.g., "prefer Java 17").

### 3.4 ReAct Agent Loop (Plan → Act → Observe → Re-plan)

**Already have:**

- Linear chain: Conductor → DynamicContext → ToolCall → Model → Judge.

**Add:**

- Wrap the **tool execution + reasoning** part in a small **loop** (state machine) for selected flows:
  - After a tool runs, the LLM sees tool output and may decide to call another tool or change strategy.
  - Limit loop to N iterations (e.g. 3–5) to control cost.
- Start small: implement ReAct only for a subset of tools (e.g., file/project tools) before generalizing.

### 3.5 Sandbox Code Execution

**Already have:**

- Tool infrastructure (`AiToolProvider`, ToolFinder, ToolCall).

**Add:**

- A new `CodeExecutionTool` that runs code in an isolated environment (e.g. Docker) and returns:
  - Success/Failure.
  - Compiler/runtime errors.
- Integrate this into ReAct loop for code generation/bug fixes.

### 3.6 Asynchronous / Speculative Brains

**Already have:**

- Independent services: `ToolFinderService`, `BrainFinderService`, vector store search.

**Add:**

- Use Java async (e.g. `CompletableFuture`) inside `ChatService` or `DynamicContextAdvisor` to:
  - Fire off tool and brain RAG searches in parallel.
  - Possibly start vector search before Conductor fully decides.

### 3.7 Fast vs Slow Routing (System-1 / System-2)

**Already have:**

- `ConductorAdvisor.isSimpleQuery` fast path for simple queries.

**Add:**

- A tiny **router** at the start of `ChatService.processChat`:
  - Simple/FAQ questions → small model + minimal advisor chain.
  - Complex architectural/refactor questions → full Hybrid Brain + highest-quality model.
- This reduces latency and cost on trivial queries.

### 3.8 Clarification Brain

**Already have:**

- `ConductorAdvisor` computes complexity and ambiguity.

**Add:**

- A `ClarificationAdvisor` or extension to Conductor:
  - If ambiguity exceeds threshold (e.g., multiple possible targets, vague request), **stop** normal chain.
  - Return a **clarifying question** instead of a guess.

### 3.9 Predictive Context Loading

**Already have:**

- Vector stores and code indexers.

**Add:**

- For some topics (e.g. `LoginService`), have an async task pre-fetch related code (e.g. `AuthController`, `UserEntity`) into a "hot cache".
- If user next asks about these, answers can be faster and more detailed.

---

## 4. Recommended Implementation Order

### Step 1 – Clarification & Fast/Slow Routing (Low Risk, High UX Impact)

1. **ClarificationBrain / Logic**
   - Use `AgentPlan` ambiguity to trigger clarifying questions.
   - Implement as a small advisor or extension to `ConductorAdvisor`.

2. **Fast vs Slow Router**
   - Add a simple classifier at entry of `ChatService`:
     - Fast path: simple queries → minimal advisors + cheap model.
     - Slow path: complex queries → full Hybrid Brain + best model.

### Step 2 – User Profile & Reflection (Learning from Mistakes)

3. **UserProfile Service**
   - Implement actual `DeveloperProfile` storage.
   - Record user preferences (language versions, style, level).

4. **Reflection Hook**
   - Extend `SelfRefineV3Advisor` / `SupervisorBrain` to log when user corrects the system.
   - Update profile with "lessons".

### Step 3 – Hierarchical Memory & Conversational Intelligence

5. **Formal Hierarchical Memory**
   - Organize ChatMemory + summaries + project graph as explicit tiers.
   - Use `ConversationalContextManager` (Phase 2 doc) + `ProjectKnowledgeGraphService`.

6. **Visible CoT Streaming**
   - Surface planner/judge “thinking” as streamed thought bubbles to frontend.

### Step 4 – ReAct Loop & Sandbox Code Execution

7. **Small ReAct Loop for Tools**
   - Implement a loop for selected tool flows:
     - Plan → Tool → Observe → Re-plan (max N iterations) before responding.

8. **CodeExecutionTool Integration**
   - Add an isolated code runner tool.
   - Use it within ReAct for code generation tasks.

### Step 5 – Performance Enhancements & Predictive Context

9. **Async / Speculative RAG**
   - Parallelize ToolFinder/BrainFinder/VectorStore queries.

10. **Predictive Context Loading**
    - Preload related code for likely next questions, enhancing perceived speed.

---

## 5. Key Takeaway

Your current system is already architected in a way that **aligns with these AGI optimization ideas**. The roadmap is not asking for a rewrite, but for layered enhancements:

- Start with **Clarification + Routing + Emotional/Conversational phases** for quick UX wins.
- Then add **User Profile + Hierarchical Memory** for deeper understanding.
- Finally, introduce **ReAct loops and sandbox execution** for true agentic behaviour.

This sequence will gradually move you from "smart chatbot" to a **human-like, agentic coding partner**, while preserving your existing performance and structure.
