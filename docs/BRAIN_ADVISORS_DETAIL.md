# Hybrid Brain – Advisors & Brains Detail

## 1. Advisor/Brain Concept

In this project, a **brain** is implemented as a Spring AI **CallAdvisor** that also implements `IAgentBrain` when it participates in Brain RAG. All brains form a **pipeline** around the main ChatModel.

- **Advisors (`CallAdvisor`)** wrap the core LLM call and can:
  - Read/modify the `ChatClientRequest` and `Prompt`.
  - Inject system instructions.
  - Log or evaluate responses.
  - Persist or read context.
- **Brains (`IAgentBrain`)** are a subset of advisors that:
  - Have a semantic description (`getBrainDescription`).
  - Are indexed into the `brainVectorStore` at startup.
  - Can be selected dynamically via `BrainFinderService`.

The goal is to have **one thought‑stream** but allow multiple specialized brains to plug in, without always running all of them.

---

## 2. Core Advisors in the Default Chain

These are the primary advisors used by the main chat clients (especially `ollamaChatClient` and the OpenAI/Anthropic/Gemini clients).

### 2.1 `ConversationHistoryAdvisor` (order −2)

**Purpose:**

- Provide visibility into **conversation history loading**.

**Key behaviors:**

- Runs before `MessageChatMemoryAdvisor`.
- Reads `conversationId` from `GlobalBrainContext` (set by `ChatService`).
- Extracts the current user message from the `ChatClientRequest`.
- Logs:
  - Trace ID from `TraceContext`.
  - Conversation ID being used.
  - The current user message text.
  - A note that `MessageChatMemoryAdvisor` will load the actual history next.
- Does **not** modify the prompt; it is purely observational/logging.

**Why it exists:**

- Helps debug which conversation ID is used and what history is expected to be loaded.

---

### 2.2 `MessageChatMemoryAdvisor` (order −1)

**Purpose:**

- Built‑in Spring AI advisor that **loads previous messages** from `ChatMemory`.

**Key behaviors:**

- Uses the configured `ChatMemory` (`MessageWindowChatMemory` with `maxMessages = 20`).
- Injects previous user/assistant messages into the prompt so the LLM sees **conversation context**.

**Why it matters:**

- Enables **short‑term memory** for the assistant without manually passing history around.

---

### 2.3 `ConductorAdvisor` (Brain 0 – Master Planner, order 0)

**Purpose:**

- The **single master planner** for each request.
- Replaces older split planning (ThoughtStream + LocalQueryPlanner) with **one unified plan**.

**Inputs:**

- User message text extracted from `ChatClientRequest`.
- `ReasoningState` from `GlobalBrainContext` (contains tool suggestions from `ToolFinderService`).

**Key responsibilities:**

- Analyze the query to estimate:
  - **Complexity** (1–10): length, number of questions, presence of advanced terms.
  - **Ambiguity** (1–10): vague pronouns, short queries, hedging words.
  - **Focus area:** DEBUG / REFACTOR / IMPLEMENTATION / ARCHITECTURE / PERFORMANCE / SECURITY / GENERAL.
  - **Intent:** CALCULATION / DEBUG / REFACTOR / IMPLEMENTATION / EXPLANATION / TESTING / GENERAL.
  - **Strategy:** `FAST_RECALL`, `BALANCED`, or `SLOW_REASONING` based on complexity/ambiguity.
- Decide **which tools are truly required** using:
  - The raw query.
  - Detected intent.
  - The *suggested* tools from `ReasoningState`.
- Decide **which specialist brains** should be involved.
- Build a single **`AgentPlan`** with:
  - Intent, complexity, ambiguity.
  - Focus and ignore areas.
  - Strategy.
  - Approved tools (`requiredTools`).
  - Recommended specialist brains (`selectedBrains`).
  - Confidence score and timestamps.
- Store the `AgentPlan` in **`AgentPlanHolder`** (thread‑local) so other brains can read it.
- Update `ReasoningState` to **approve selected tools**.

**Fast‑path optimization:**

- For very simple arithmetic or time/date queries:
  - Skips heavy analysis.
  - Builds a small, optimized `AgentPlan` that typically uses only:
    - `conductorAdvisor`, `toolCallAdvisor`, `personalityAdvisor`.
  - Gives ~60% performance improvement for trivial queries.

**Why it matters:**

- Ensures that **all downstream brains share one plan**, avoiding conflicting reasoning.
- Central place where intent, complexity and tool usage are decided.

---

### 2.4 `DynamicContextAdvisor` (Brain 1 – Context Fetcher)

**Purpose:**

- Read the `AgentPlan` created by `ConductorAdvisor` and **assemble the dynamic context** for specialist brains and tools.

**Inputs:**

- `AgentPlan` from `AgentPlanHolder`.
- User query text extracted from the request.

**Key responsibilities:**

- If a master plan exists:
  - Logs intent, complexity, selected brains, required tools from the plan.
  - Calls **`selectBrainsBasedOnPlan`** to decide which specialist brains should actually be activated.
  - Reads **approved tools** from the plan.
  - Stores plan context (specialist brains + tools) for downstream brains.
  - Builds a **context injection string** describing:
    - Which specialist brains are active.
    - Which tools are approved.
    - What the current strategy is.
- If no plan exists:
  - Runs a **fallback mode** that may call `BrainFinderService` / `ToolFinderService` directly and then continues the chain.

**Why it matters:**

- Connects the high‑level plan to concrete advisor and tool behavior.
- Ensures that specialist brains see the right context and know when to wake up.

---

### 2.5 `ToolCallAdvisor` (Brain 2 – Plan‑Aware Tool Executor)

**Purpose:**

- Enforce the **tool policy** defined by the master plan.
- Make sure the LLM only uses tools that the plan approves.

**Inputs:**

- `AgentPlan` from `AgentPlanHolder`.
- `ReasoningState` from `GlobalBrainContext` (contains suggested tools).

**Key responsibilities:**

- Read **approved tools** from `plan.requiredTools`.
- Read **suggested tools** from `ReasoningState` (ToolFinder output).
- Compute **rejected tools** = suggested − approved.
- Construct a **tool execution policy** system message:
  - If no tools are approved:
    - Forbid using any tools; tell the model to answer from knowledge only.
  - If some tools are approved:
    - List approved tools that may be used.
    - List rejected tools that must not be used and warn about errors.
- Prepend that system message to the prompt by creating a new `ChatClientRequest`.
- Pass the modified request further down the advisor chain.

**Why it matters:**

- Prevents the LLM from blindly calling every tool it sees.
- Reduces tool‑related errors and keeps executions aligned with the master plan.

---

### 2.6 `SelfRefineV3Advisor` (Brain 13 – Enhanced Judge, order 1000)

**Purpose:**

- Final **quality gate** for responses.
- Evaluate and optionally refine responses using multiple criteria.

**Inputs & dependencies:**

- The model’s response produced by previous advisors.
- `AgentPlan` from `AgentPlanHolder` (for complexity and intent).
- `SupervisorBrain` for conversation‑level tracking.
- `TokenCountingService`, `ConsistencyCheckService`, `HallucinationDetector`, `OutputMerger`.
- Internal `judgeClient` (OpenAI‑based) for meta‑evaluation and refinement.

**Key responsibilities:**

- For *simple* queries (low complexity per `AgentPlan`):
  - **Skip heavy evaluation** for speed and cost.
- For other queries:
  - Compute:
    - **Clarity**, **relevance**, **helpfulness**.
    - **Consistency** (using `ConsistencyCheckService`).
    - **Hallucination score** (using `HallucinationDetector`).
    - **Code structure validity** (if response contains code).
    - **Token usage** (via `TokenCountingService`).
  - Combine these into an `EnhancedQualityEvaluation`:
    - Applies penalties for hallucinations and inconsistency.
    - Computes a final rating (0–5) and a verdict (Excellent/Good/Poor, etc.).
  - If rating is below threshold and refinements remaining:
    - Ask the `judgeClient` to **refine** the answer with specific instructions (fix consistency, remove questionable claims, improve clarity, fix code).
    - Re‑evaluate the refined answer and keep the better one.
  - Log detailed evaluation metrics via `SupervisorBrain`.

**Why it matters:**

- Makes responses more stable and reliable.
- Adds a dedicated quality‑control brain at the end of the chain.

---

### 2.7 `PersonalityAdvisor` (Brain 14 – Voice, order 800)

**Purpose:**

- Apply a consistent **personality and communication style** to the final response.

**Inputs & dependencies:**

- Response text from previous advisors.
- `PersonalityEngine` (encapsulates traits and style rules).

**Key responsibilities:**

- Call `personalityEngine.applyPersonality(responseText)` to:
  - Adjust tone (mentor‑like, supportive, clear).
  - Maintain consistent style and phrasing.
- Log the personality profile:
  - Archetype.
  - Style summary.
  - Flags such as empathetic, patient, helpful.
- Return the (possibly) personalized response.

**Why it matters:**

- Keeps interactions feeling like the same assistant over time.
- Makes technical answers more readable and user‑friendly.

---

### 2.8 `SupervisorBrain`

**Purpose:**

- A global "control tower" for conversations.

**Key responsibilities:**

- Maintain a `ConversationState` per `conversationId`:
  - Tracks outputs from different brains.
  - Tracks quality and consistency over time.
- Maintain `BrainPerformance` metrics per brain:
  - Record quality scores on each execution.
  - Enable future optimization or routing decisions.
- Provide configuration constants:
  - Max re‑evaluation cycles.
  - Consistency and quality thresholds.
- Integrate with `SelfRefineV3Advisor` to log final status after evaluation.

**Why it matters:**

- Allows the system to reason about conversations as a whole, not just single answers.

---

## 3. Supporting / Optional Advisors

Some advisors are not wired into the main hybrid chain by default, but provide **additional planning and judging capabilities**.

### 3.1 `ChainOfThoughtPlannerAdvisor` (Brain 0 variant)

**Purpose:**

- Experimental planner that performs **deep chain‑of‑thought analysis** of a query using a dedicated OpenAI model.

**Key behaviors:**

- Uses a separate `thinkerClient` (`ChatClient` over `OpenAiChatModel`).
- Builds a structured prompt asking the model to:
  - Decompose the query.
  - Analyze context and complexity.
  - Classify intent (CODE / TOOLS / GENERAL / CORRECTION / COMPLEX).
  - Estimate confidence, complexity, ambiguity, special handling.
- Parses the structured response into an internal `ThoughtProcess` (analysis, intent, scores).
- Logs the internal monologue (analysis, reasoning, intent, confidence, etc.).
- Then forwards the request to the rest of the chain without changing the prompt.

**Use case:**

- When extra interpretability of the model’s reasoning is needed, or for experimenting with deeper planning.

---

### 3.2 `MultiCriteriaJudgeAdvisor` (Alternate Judge)

**Purpose:**

- An additional, more compact **multi‑criteria judge** that evaluates responses on several axes.

**Key behaviors:**

- Uses an internal `judgeClient` (OpenAI) with a specific evaluation prompt.
- For each response, asks the judge model to score:
  - **Clarity** (1–5).
  - **Relevance** (1–5).
  - **Factual accuracy** (1–5).
  - **Helpfulness** (1–5).
  - **Overall** (1–5).
  - And list specific issues, especially factual ones.
- Parses the judge output and logs:
  - Individual scores.
  - Overall score.
  - Factual issues.
- Compares scores against thresholds:
  - `MIN_OVERALL_SCORE`.
  - `MIN_FACTUAL_SCORE`.
- Emits warnings or errors in logs when thresholds are not met.

**Use case:**

- Can be used together with or instead of `SelfRefineV3Advisor` when a lighter, evaluation‑only judge is desired.

---

## 4. How to Read This in the Code

- Advisors/brains live mainly under `com.vijay.manager`.
- The **default active chain** is defined in `AIProviderConfig` via `.defaultAdvisors(...)` for each ChatClient.
- Any advisor that also implements `IAgentBrain` is automatically picked up by `BrainIndexerService` and becomes part of the **Brain RAG** universe.

Together, these advisors form the **Hybrid Brain**: a pipeline that plans, selects tools and brains, enforces tool usage, evaluates quality, and maintains a consistent personality and conversation experience.
