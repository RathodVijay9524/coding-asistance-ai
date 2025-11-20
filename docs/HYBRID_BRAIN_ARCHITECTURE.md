# Coding AI – Hybrid Brain Architecture Documentation

## Part 1 – High‑Level Overview

This application is a **Spring Boot + Spring AI multi‑provider coding assistant**. It exposes a web chatbot (`/chatbot`, `/send`) that can:

- **Talk to multiple LLM providers**: Ollama (local), OpenAI, Anthropic, Google Gemini, Hugging Face.
- **Use tools** (code analysis, project analysis, etc.) discovered from `AiToolProvider` services.
- **Use an advisor/brain chain** to plan, enrich context, execute tools, judge quality, and apply personality.
- **Select tools and brains dynamically using RAG** (vector search on descriptions) instead of hard‑coding everything per request.

Key goals of the architecture:

- **Reduce tokens and latency** by avoiding “run every brain and every tool every time”.
- **Scale to many tools and brains** via vector stores and semantic search.
- **Keep a unified thought‑process** through a single master plan (`AgentPlan`).
- **Preserve conversation memory** using chat memory + conversation IDs.

---

## Part 2 – Main Components

### 2.1 HTTP & Orchestration Layer

- **`ChatBotController`**
  - `GET /` → returns `index` view.
  - `GET /chatbot` → returns `chatbot` view and injects supported providers.
  - `POST /send` → entry point for chat messages.
    - Builds/reads a stable **`conversationId`** from `HttpSession` (e.g. `session_<sessionId>`).
    - Wraps the user message and flags into a `ChatRequest` and calls `ChatService.processChat(provider, request)`.

- **`ChatService` (Dumb Orchestrator)**
  - Responsibilities:
    - Initialize **trace context** (`TraceContext`) for logging and metrics.
    - Ensure a **stable conversation ID** per session.
    - Initialize **`SupervisorBrain`** and **`GlobalBrainContext`** (stores `ReasoningState`, traceId, provider, conversationId).
    - Get the correct **`ChatClient`** for the provider (Ollama/OpenAI/Anthropic/Gemini/HF).
    - Call **`ToolFinderService`** to get *suggested* tools from the user query.
    - Call the **ChatClient** with:
      - A system prompt that instructs the AI to use conversation history.
      - The user message.
      - The **tool names** suggested by ToolFinder.
      - An advisor parameter (`conversationId`) so memory advisors use the correct session.
    - After the ChatClient returns, read the **`AgentPlan`** from `AgentPlanHolder` to know which tools were actually used, and build the final `ChatResponse`.

### 2.2 Provider Configuration & Chat Clients

- **`AIProviderConfig`** wires all chat clients and vector stores:
  - **Chat memory**
    - `ChatMemory` = `MessageWindowChatMemory` (20‑message sliding window) for conversation history.
  - **Vector stores** (using local `OllamaEmbeddingModel` – no external token cost):
    - `summaryVectorStore`: high‑level code summaries.
    - `chunkVectorStore`: code chunks for detailed retrieval.
    - `brainVectorStore`: advisor/brain descriptions for Brain RAG.
  - **Chat clients**
    - `ollamaChatClient` (primary) and similar clients for OpenAI, Anthropic, Google, plus a simpler HF client.
    - Default **advisors (brains)** for Ollama and the others:
      1. `ConversationHistoryAdvisor` (order −2, Ollama only) – logs history loading.
      2. `MessageChatMemoryAdvisor` (order −1) – actually loads previous messages from `ChatMemory`.
      3. `ConductorAdvisor` (Brain 0) – builds the master `AgentPlan`.
      4. `DynamicContextAdvisor` (Brain 1) – uses the plan + Brain RAG/Tool RAG.
      5. `ToolCallAdvisor` (Brain 2) – enforces the tool policy from the plan.
      6. `SelfRefineV3Advisor` (Brain 13) – performs enhanced quality checking.
      7. `PersonalityAdvisor` (Brain 14) – applies personality style.
    - `.defaultTools((Object[]) allToolProviders.toArray(...))` exposes all `AiToolProvider` tools to the ChatClient.
  - **Dynamic ChatClient builder**
    - `buildDynamicChatClient(chatModel, selectedBrainBeans, allToolProviders)` creates a ChatClient with **only a selected subset of advisors**, enabling true Brain RAG when needed.

---

## Part 3 – Tool RAG (ToolFinder Architecture)

### 3.1 Indexing at Startup – `ToolIndexingService`

- Implements `ApplicationRunner`, so it runs when Spring Boot starts.
- Injects:
  - A `VectorStore` (tool vector store).
  - `List<AiToolProvider>` – all beans that implement the marker interface.
- For each provider:
  - Uses `ToolCallbacks.from(provider)` to detect all `@Tool` methods.
  - For each `ToolCallback`:
    - Reads `tool.getToolDefinition().name()` and `.description()`.
    - Builds a `Document` whose **content** is the description and **metadata** contains `toolName`.
- All documents are added to the tool vector store.
- Result: all tools are **embedded** and indexed for semantic similarity search.

### 3.2 Runtime – `ToolFinderService`

- Input: **user prompt** from `ChatService`.
- Steps:
  1. Builds a `SearchRequest` with `.query(prompt)` and `topK = 3`.
  2. Calls `vectorStore.similaritySearch(request)`.
  3. Maps each `Document` to its `toolName` metadata.
  4. Logs which tools were found.
  5. Creates a `ReasoningState` with the prompt and **suggested tool names** and stores it in `GlobalBrainContext`.
- Output: `List<String>` of tool names, passed to the ChatClient via `.toolNames(...)`.

**Effect:** Only tools that are *semantically similar* to the user’s query are even offered to the LLM. A later brain (ToolCallAdvisor) will further filter which of these tools can actually be executed.

---

## Part 4 – Brain RAG (BrainFinder Architecture)

### 4.1 Brain Interface – `IAgentBrain`

All advisor “brains” implement `IAgentBrain`, which defines:

- `getBrainName()` – Spring bean name used for dynamic lookup.
- `getBrainDescription()` – natural language description for embedding in the brain vector store.
- `getOrder()` – integer describing execution order in the chain (lower = earlier).

### 4.2 Indexing at Startup – `BrainIndexerService`

- Implements `ApplicationRunner`.
- Injects:
  - `@Qualifier("brainVectorStore") VectorStore`.
  - `List<IAgentBrain> allBrains` – all advisor brains that implement the interface.
- On startup:
  - Iterates over all brains.
  - For each brain:
    - Reads `brainName`, `brainDescription`, and `order`.
    - Creates a `Document` with description as content and metadata `brainName` + `order`.
  - Adds all brain documents to `brainVectorStore`.
- Result: the system has a **searchable semantic index of all brains**, ready for Brain RAG.

### 4.3 Runtime Brain Selection – `BrainFinderService`

- Core brain list (always included):
  - `conductorAdvisor` – Brain 0, planner.
  - `toolCallAdvisor` – Brain 2, plan‑aware tool executor.
  - `selfRefineV3Advisor` – Brain 13, judge.
  - `personalityAdvisor` – Brain 14, voice.
- `findBrainsFor(query)`:
  1. Start with the core brain list.
  2. Build `SearchRequest` with `.query(query)` and `topK = 4`.
  3. Call `brainVectorStore.similaritySearch(request)`.
  4. Map each result to `brainName` and add to the list if not already present.
  5. Sort the final list by `order` so execution sequence is correct.
- Output: ordered list of Spring bean names for brains that should participate.

This list can be used together with `AIProviderConfig.buildDynamicChatClient(...)` to construct a ChatClient containing only the selected brains for a given query (Brain RAG).

---

## Part 5 – Core Brains / Advisors (Step‑by‑Step)

### 5.1 `ConversationHistoryAdvisor` (order −2)

- Logs that conversation history is being loaded.
- Reads `conversationId` from `GlobalBrainContext` (set by `ChatService`).
- Logs the current user message and conversation ID.
- Delegates to the next advisor; actual memory loading is done by `MessageChatMemoryAdvisor`.

### 5.2 `MessageChatMemoryAdvisor` (order −1)

- Standard Spring AI advisor that loads previous messages from `ChatMemory` based on the conversation ID.
- Provides **short‑term memory** across turns (20 most recent messages by configuration).

### 5.3 `ConductorAdvisor` (Brain 0 – Master Planner)

- First logical brain in the chain.
- Responsibilities:
  - Extract the **user message** from the request.
  - Analyze **complexity**, **ambiguity**, **focus area**, **intent**, and **strategy**.
  - Read `ReasoningState` from `GlobalBrainContext` (which already contains suggested tools from `ToolFinder`).
  - Decide **which tools are actually required** and which specialist brains should be activated.
  - Build a unified **`AgentPlan`** containing:
    - Intent, complexity, ambiguity.
    - Focus / ignore areas.
    - Reasoning strategy (`FAST_RECALL`, `BALANCED`, `SLOW_REASONING`).
    - `requiredTools` – approved tools.
    - `selectedBrains` – recommended specialist brains.
    - Confidence and timestamps.
  - Store the plan in **`AgentPlanHolder`** (thread‑local), so later brains can read it.
  - Update the `ReasoningState` to mark tool approvals.
- Has a **fast‑path** for very simple arithmetic and time/date queries where it can build a lightweight plan using only a small subset of brains.

### 5.4 `DynamicContextAdvisor` (Brain 1 – Context Fetcher)

- Reads the master `AgentPlan` from `AgentPlanHolder`.
- If the plan is missing, falls back to a simpler context discovery mode.
- Responsibilities:
  - Extract the user query from the request.
  - Decide which **specialist brains** to actually activate based on the plan (`selectBrainsBasedOnPlan`).
  - Use **BrainFinderService** and **ToolFinderService** if needed to enrich or cross‑check selections.
  - Build a **context injection** string that describes which brains and tools are available and what the current strategy is.
  - Store context information for downstream advisors.
- After preparing context, it delegates to the next advisor.

### 5.5 `ToolCallAdvisor` (Brain 2 – Plan‑Aware Tool Executor)

- Reads the master `AgentPlan` from `AgentPlanHolder`.
- Reads the `ReasoningState` (which contains suggested tools) from `GlobalBrainContext`.
- Responsibilities:
  - Compute **approved tools** (from `plan.requiredTools`).
  - Compute **rejected tools** = suggested tools − approved tools.
  - Inject a **tool execution policy** as a `SystemMessage` into the prompt:
    - If no tools are approved → explicitly forbid tool usage.
    - If some tools are approved → list which are allowed and which are forbidden.
  - Forward a modified `ChatClientRequest` containing these enforcement rules.
- Effect: the LLM receives clear instructions about which tools it may or may not call, preventing unnecessary or dangerous tool invocations.

### 5.6 `SelfRefineV3Advisor` (Brain 13 – Enhanced Judge)

- Final quality gate (order 1000).
- Injected dependencies: `SupervisorBrain`, `TokenCountingService`, `ConsistencyCheckService`, `HallucinationDetector`, `OutputMerger`, plus its own `judgeClient` (OpenAI‑based).
- Responsibilities:
  - For **simple queries** (low complexity from `AgentPlan`), it may **skip heavy evaluation** to save cost and latency.
  - For other queries:
    - Obtain the model’s response from the chain.
    - Run **clarity**, **relevance**, and **helpfulness** scoring.
    - Run a **consistency check** (contradictions, issues).
    - Run a **hallucination detector**.
    - Validate **code structure** when the response contains code.
    - Count tokens and record usage per user.
    - Combine all into a `EnhancedQualityEvaluation` with penalties.
    - If the rating is below threshold, attempt **refinement** by prompting an internal judge LLM to improve the answer and then re‑evaluate.
  - Interact with `SupervisorBrain` to track conversation‑level status.

### 5.7 `PersonalityAdvisor` (Brain 14 – Voice)

- Runs late (order 800, i.e. after main content is generated but before or alongside final quality checks).
- Uses a `PersonalityEngine` to:
  - Apply a consistent **personality archetype** (e.g., mentor‑style).
  - Maintain **tone and communication style** across responses.
  - Optionally add empathetic and supportive phrasing.
- Logs personality profile and traits (empathetic, patient, helpful).

### 5.8 `SupervisorBrain`

- Tracks **global conversation state** and **brain performance** (quality scores per brain).
- Used by `ChatService` (initialization) and `SelfRefineV3Advisor` (logging and quality tracking).
- Holds configuration for max re‑evaluation cycles and quality/consistency thresholds.

---

## Part 6 – End‑to‑End Request & Data Flow

### 6.1 Startup (Indexing Phase)

1. Spring Boot application starts.
2. `ToolIndexingService.run()`:
   - Finds all `AiToolProvider` beans and indexes their `@Tool` methods into the **tool vector store**.
3. `BrainIndexerService.run()`:
   - Finds all `IAgentBrain` implementations and indexes their descriptions into the **brain vector store**.
4. `AIProviderConfig` builds:
   - Vector stores for summaries, chunks, and brains.
   - Chat clients for all providers with default advisors and tools.

### 6.2 Runtime (When a User Sends a Message)

1. **HTTP Layer**
   - `POST /send` in `ChatBotController` receives `message` + `provider`.
   - A stable `conversationId` is read/created from `HttpSession`.
   - A `ChatRequest` is built and passed to `ChatService.processChat`.

2. **ChatService Setup**
   - Initializes `TraceContext` and logs.
   - Initializes `SupervisorBrain` with `userId` and `conversationId`.
   - Creates a `ReasoningState` with the user query and stores it in `GlobalBrainContext` along with `traceId`, `provider`, and `conversationId`.
   - Resolves the proper `ChatClient` for the provider.

3. **Tool RAG (ToolFinderService)**
   - `findToolsFor(message)` performs vector search on the tool descriptions.
   - Suggested tool names are stored in `ReasoningState` (inside `GlobalBrainContext`).
   - `ChatService` passes these tool names into the ChatClient via `.toolNames(...)`.

4. **Advisor Chain Execution (Brains)**
   - Advisors run in order:
     - `ConversationHistoryAdvisor` logs and prepares history loading.
     - `MessageChatMemoryAdvisor` loads conversation history from `ChatMemory`.
     - `ConductorAdvisor` builds the **master AgentPlan** using the user query and suggested tools.
     - `DynamicContextAdvisor` reads the plan and prepares specialist brain + tool context.
     - `ToolCallAdvisor` injects **tool execution policy** based on the plan.
     - (Specialist brains, when present, act according to the plan and injected context.)
     - `SelfRefineV3Advisor` evaluates and potentially refines the answer.
     - `PersonalityAdvisor` adjusts tone and style.

5. **Response Return**
   - The ChatClient returns the final response text.
   - `ChatService`:
     - Reads the final `AgentPlan` from `AgentPlanHolder`.
     - Extracts the **actually used tools** from the plan.
     - Builds a `ChatResponse` containing the content, provider, and tool list.
     - Clears all contexts (`GlobalBrainContext`, `TraceContext`, `AgentPlanHolder`).
   - `ChatBotController` returns the `ChatResponse` as JSON to the UI.

---

## Part 7 – Before vs After (What This Architecture Achieves)

Originally, the system ran a **static chain of ~13 brains for every query**:

- Every advisor ran regardless of the query type.
- Token usage per request could easily reach **10,000+ tokens**.
- Response times were typically **5–10 seconds**.
- Large prompts caused **HTTP 413 (Request Too Large)** errors.
- Having too many brains active sometimes produced **confused or conflicting responses**.

With the **Tool RAG + Brain RAG + Hybrid Brain** architecture:

- **Brains per query**:
  - Core brains always run (Conductor, DynamicContext, ToolCall, Judge, Personality).
  - Specialist brains are **selected via Brain RAG** (semantic search on `brainVectorStore`) and/or by the master plan.
  - Effective brains per query drop from 13 → **3–4 active brains**, plus memory.
- **Tool selection**:
  - Only tools that are **semantically similar** to the query are suggested.
  - The **Conductor** and **ToolCallAdvisor** enforce that only approved tools can run.
- **Token usage**:
  - Reduced from ~10,000+ tokens to ~**2,000–3,000** per request in typical flows.
- **Response time**:
  - Reduced from **5–10 seconds** to roughly **1–2 seconds** in many cases.
- **Errors and stability**:
  - HTTP 413 errors are effectively **eliminated** because prompts are smaller and more focused.
  - Tool execution errors are reduced by enforcing a plan‑aware tool policy.
- **Quality and focus**:
  - A single **master plan** avoids the “split brain” problem of conflicting planners.
  - `SelfRefineV3Advisor` and (optionally) `MultiCriteriaJudgeAdvisor` provide explicit quality checks.
  - `PersonalityAdvisor` keeps the interaction style consistent and human‑friendly.

**Net result:** the system becomes **faster, cheaper, more scalable, and easier to extend**, while keeping responses focused, high‑quality, and tailored to each query.

## Part 8 – Future Upgrade: GraphRAG / CodeGraphService

In addition to vector-based RAG (Tool RAG and Brain RAG), a future upgrade is to add a **graph-based view of your codebase**. This captures not just *similarity* but also *structure*:

- **Nodes:** Classes, Methods, Interfaces, Database Tables.
- **Edges:** `EXTENDS`, `IMPLEMENTS`, `CALLS`, `RETURNS`, `DEPENDS_ON`.

### 8.1 `CodeGraphService`

- New service responsible for building and querying a **code graph** from your existing indexes / source analysis.
- Can be implemented initially with an in-memory graph library (e.g. JGraphT) and later migrated to a graph database (e.g. Neo4j) if needed.

### 8.2 Integration with `DynamicContextAdvisor`

- **Old way:** User asks "Fix `OrderService`" → vector stores return chunks mentioning `OrderService`.
- **New way:** `DynamicContextAdvisor` also calls `CodeGraphService` to:
  - Find the `OrderService` node.
  - Traverse to **callers** (who uses it) and **dependencies** (repositories, entities, other services).
  - Inject this **impact radius** into the LLM context.

Result: when planning a change, the LLM sees **all affected pieces** (callers, repositories, downstream services), reducing regression risk and making the assistant behave more like a human engineer who understands the structure of the system, not just isolated files.
