# Optimization Plan – Brains & Tools

This document describes **how to further optimize brains (advisors) and tools** in the existing Hybrid Brain architecture.

It builds on the current design (Tool RAG + Brain RAG + Conductor/ToolCall/Personality/SelfRefine) and focuses on **reducing cost and latency** while keeping or improving quality.

---

## 1. Goals

- **Reduce tokens and latency** for common/simple queries.
- **Use fewer brains and tools per request** without losing quality.
- **Leverage Brain RAG and Tool RAG more aggressively** (per-request selection instead of static chains everywhere).
- **Keep the architecture simple to reason about** (no complex branching all over the code).

---

## 2. Brain (Advisor) Optimization Plan

### 2.1 Use dynamic brain sets more aggressively (B1)

**Current state**

- `BrainIndexerService` + `BrainFinderService` + `AIProviderConfig.buildDynamicChatClient(...)` are implemented.
- Main providers (e.g., `ollamaChatClient`, `openAiChatClient`) still have a **fixed core chain** defined via `.defaultAdvisors(...)`.

**Plan**

- For some query types, build a **per-request ChatClient** that includes:
  - **Always-on core brains:**
    - `ConductorAdvisor` (Brain 0).
    - `ToolCallAdvisor` (Brain 2).
    - `SelfRefineV3Advisor` (Brain 13) – or chosen main judge.
    - `PersonalityAdvisor` (Brain 14).
  - **Specialist brains:** only those returned by `BrainFinderService.findBrainsFor(...)`.
- Implementation direction:
  - Add a method in a service (e.g., `ChatService` or helper) that:
    - Calls `BrainFinderService.findBrainsFor(plan or query)`.
    - Resolves bean instances for those brains from `ApplicationContext`.
    - Calls `AIProviderConfig.buildDynamicChatClient(...)` with that list.
  - Use the dynamic client for selected providers or selected intents (e.g., complex CODE/ARCHITECTURE queries).

**Expected effect**

- Fewer advisors per request, lower token usage and latency.
- Better scalability as more brains are added over time.

---

### 2.2 Extend complexity-based skipping (B2)

**Current state**

- `SelfRefineV3Advisor` already **skips heavy evaluation** when `AgentPlan.getComplexity() <= 3`.

**Plan**

- Extend the same pattern to other brains:
  - In `ConductorAdvisor`:
    - For very low complexity & low ambiguity queries, mark the plan as **fast-path** (already present for simple math/time queries).
  - In `DynamicContextAdvisor`:
    - Only trigger Brain RAG / complex specialist selection when complexity or ambiguity exceeds configured thresholds.
  - Optional: allow feature flags (e.g., properties) to control thresholds per environment.

**Expected effect**

- Simple queries (math, date, short Q&A) incur minimal brain overhead.
- Complex queries still get the full pipeline.

---

### 2.3 Simplify judges usage (B3)

**Current state**

- There are two judge-related advisors:
  - `SelfRefineV3Advisor` – enhanced, heavy judge with refinement.
  - `MultiCriteriaJudgeAdvisor` – simpler multi-criteria judge.

**Plan**

- Decide a **primary judge per provider**:
  - Example: use `SelfRefineV3Advisor` for OpenAI (where external tokens are acceptable) and skip or limit it for local Ollama.
  - Use `MultiCriteriaJudgeAdvisor` only for specific high-risk flows or in debug/QA mode.
- Wire only the chosen judge into `.defaultAdvisors(...)` for production.
- Optional: gate the secondary judge behind a config flag.

**Expected effect**

- Avoid double evaluation passes.
- Concentrate quality checks where they add real value.

---

### 2.4 Conversation-based fast-path (B4)

**Current state**

- `ConductorAdvisor` has a fast-path for simple arithmetic/time queries.

**Plan**

- Extend detection logic for **follow-up/acknowledgement** messages, e.g.:
  - "ok", "yes", "thanks", "continue", short confirmations.
- For such low-value queries:
  - Skip Brain RAG.
  - Skip heavy judges.
  - Possibly bypass Tool RAG entirely.

**Expected effect**

- Very fast, light responses in ongoing conversations where the user is just acknowledging or asking for continuation.

---

## 3. Tool Optimization Plan

### 3.1 Dynamic topK and similarity threshold (T1)

**Current state**

- `ToolFinderService` uses:
  - `SearchRequest.builder().query(prompt).topK(3).build()`.
  - No `similarityThreshold` is applied.

**Plan**

- Make `topK` and threshold **adaptive**:
  - For simple/short queries:
    - `topK = 1–2`.
  - For complex/code/project queries:
    - `topK = 4–5`.
  - Add `similarityThreshold` (e.g., 0.6–0.7) so weak matches are ignored.
- Implementation idea:
  - In `ToolFinderService.findToolsFor(prompt)`:
    - Use basic heuristics on length/keywords to estimate complexity.
    - Build `SearchRequest` with dynamic `topK` and `.similarityThreshold(...)`.

**Expected effect**

- Fewer irrelevant tools suggested.
- Smaller `.toolNames(...)` list, less prompt noise.

---

### 3.2 Category-aware tools (T2)

**Current state**

- `ToolIndexingService` stores only `toolName` in Document metadata.

**Plan**

- Extend tool metadata with **categories/tags**, for example:
  - `{"toolName": "X", "domain": "code"}`
  - Domains: `code`, `project`, `math`, `weather`, `system`, etc.
- Let `ConductorAdvisor` or `DynamicContextAdvisor` influence ToolFinder:
  - For `intent = CODE` or `focusArea = ARCHITECTURE` → search only tools with `domain=code/project`.
  - For `intent = CALCULATION` → search only `math` tools.
- Implementation direction:
  - Update `ToolIndexingService` to set domains.
  - Update `ToolFinderService` to optionally filter by domain based on intent/focus from the plan.

**Expected effect**

- More precise tool selection per query.
- Easier to add many tools without overwhelming the model.

---

### 3.3 Plan-driven tool narrowing (T3)

**Current state**

- `ConductorAdvisor.identifyRequiredTools(...)` already approves tools using query patterns and suggested tools.

**Plan**

- Tighten the approval logic:
  - For very simple queries: approve **at most 1–2 tools**.
  - For complex analysis: allow more tools but enforce an upper cap (e.g., 4–5).
- Optionally, incorporate similarity score ranking (if available from ToolFinder) so only top tools are kept.

**Expected effect**

- Smaller `requiredTools` list in `AgentPlan`.
- Cleaner and shorter tool execution policies in `ToolCallAdvisor`.

---

## 4. Phased Implementation Proposal

To keep changes safe and incremental, we can implement the plan in phases:

### Phase 1 – Low-risk, high-value tweaks

- Implement **T1** (dynamic `topK` + `similarityThreshold` in `ToolFinderService`).
- Extend **B2** (complexity-based skipping) where obvious.
- Decide the primary judge and wire **B3** (simplify judges for each provider).

### Phase 2 – Dynamic brain sets for selected flows

- Implement **B1** for a subset of flows:
  - Example: for `intent = CODE` and complexity > threshold, build a dynamic ChatClient based on Brain RAG.
- Measure tokens and latency before/after.

### Phase 3 – Category-aware tools and advanced narrowing

- Implement **T2** (tool domains) and **T3** (stronger plan-driven narrowing).
- Update documentation to reflect category-based selection.

### Phase 4 – Conversation-based fast-path enhancements

- Implement **B4** for follow-up/acknowledgement messages.
- Further tune complexity thresholds and patterns based on real usage.

---

## 5. Next Steps

- Choose which **Phase** to start with (recommended: Phase 1).
- For that phase, create small tickets per item (e.g., "Add dynamic topK to ToolFinderService").
- After implementation, measure:
  - Average tokens per request.
  - Average latency per provider.
  - Error rates (HTTP 413, tool errors).

This optimization plan is designed to work **with the existing Hybrid Brain architecture** without large refactors, while giving clear, incremental improvements in performance and scalability.
