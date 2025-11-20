# AGI Upgrade – Practical Next Steps

This document distills all AGI / human-like upgrade plans into **concrete, realistic steps** for your current Spring AI Hybrid Brain system, focusing on what will **actually boost the app** in the near and medium term.

---

## 1. Prioritized Roadmap

### 1.1 Near term (do these first)

- **ReAct loop around `ChatClient` using existing advisors’ feedback**
  - Wrap the current linear advisor chain into a small **reason–act–reflect loop**.
  - Use existing advisors (`ConductorAdvisor`, `DynamicContextAdvisor`, `ToolCallAdvisor`, `SelfRefineV3Advisor`, `PersonalityAdvisor`, `SupervisorBrain`) as "internal voices".
  - Let the model:
    - Think / plan.
    - Optionally call tools.
    - Reflect and self-correct once or twice before final answer.

- **Parallelize ToolFinder / BrainFinder / memory using `CompletableFuture` (no virtual threads)**
  - Run these independent lookups in **parallel** to reduce latency:
    - `ToolFinderService.findToolsFor(...)`
    - `BrainFinderService.findBrainsFor(...)`
    - Memory / RAG retrieval (if you add more stores later).
  - Use a bounded `ExecutorService` or Spring `ThreadPoolTaskExecutor` and `CompletableFuture`.

- **Clarification logic + fast/slow routing**
  - Add simple logic to detect:
    - **Simple, direct questions** → fast path: minimal advisors, no tools, 0 or 1 ReAct iteration.
    - **Ambiguous / risky queries** → ask a **clarification question** first.
    - **Complex, multi-step coding tasks** → slow path: full ReAct loop with tools and specialist brains.

- **Hierarchical memory + user profile (Phase 1)**
  - Add:
    - Short-term conversation state (already partly there with `ReasoningState`).
    - Per-user profile (preferences, stack, typical tasks).
    - Simple "session memory": last N tasks / fixes.
  - Use this memory in `ConductorAdvisor` and `PersonalityAdvisor` to make answers feel **personal, stable, and human-like**.

---

### 1.2 Medium term

- **Sandbox compilation / execution tool for code tasks**
  - Add a dedicated tool (e.g., `CodeSandboxTool`) that:
    - Compiles / runs snippets in a sandboxed environment.
    - Captures compiler/runtime errors and feeds them back to the ReAct loop.
  - The agent can then **test its own suggestions** before replying.

- **Visible Chain-of-Thought (CoT) streaming – "thoughts bubble"**
  - You already have internal CoT via `ChainOfThoughtPlannerAdvisor` and others.
  - Add a **UI layer** that:
    - Shows a human-friendly, high-level version of the reasoning path.
    - Streams steps like: *"Understanding your request → Searching tools → Planning edits → Checking consistency"*.
  - Keep sensitive internal CoT hidden; surface only safe summaries.

---

### 1.3 Long term

- **GraphRAG (code graph via `CodeGraphService`)**
   - Problem: vector search is great for **similarity** ("find code like this") but weak for **structure** ("who calls this method? what extends this class?").
   - Add a `CodeGraphService` that indexes code as a **graph**, not just text:
     - **Nodes:** Classes, Methods, Interfaces, Database Tables.
     - **Edges:** `EXTENDS`, `IMPLEMENTS`, `CALLS`, `RETURNS`, `DEPENDS_ON`.
   - Integration with `DynamicContextAdvisor`:
     - Old way: user asks "Fix `OrderService`" → vector DB finds text mentioning `OrderService`.
     - New way: `CodeGraphService` traverses the graph: get `OrderService` + all classes that **call** it + the Repositories it **uses**.
     - The LLM sees the **full impact radius** of a change, reducing hidden regressions.
   - Tech stack:
     - Start with an in-memory graph (e.g. `JGraphT`) built from your existing code indexers.
     - Optionally move to a graph database like **Neo4j** if the project grows or you want complex queries.

- **Agent swarm orchestrator**
  - On top of the Hybrid Brain, add a **"swarm manager"** that can:
    - Spawn multiple specialized agents (e.g., performance, security, UX) for a big task.
    - Merge their suggestions and resolve conflicts.

- **Offline "sleep" learning and shadow agents**
  - After real sessions, run "shadow" agents offline that:
    - Re-solve hard past tasks with more time/tools.
    - Produce better plans and store them as **recipes** in a knowledge base.

- **Dynamic tool synthesis**
  - Allow the system to propose **new tools** (e.g., small scripts) that are then reviewed and added to the Tool RAG.
  - Over time, the toolbox grows automatically based on real usage.

---

## 2. Recommended Next Backend Step (Start Here)

From the above, the **single most impactful and realistic next step** for your current system is:

> **Implement a ReAct loop around `ChatClient` using existing advisors’ feedback.**

Why this first:

- Uses everything you already built (Hybrid Brain, Tool RAG, Brain RAG).
- Immediately improves **quality, self-correction, and human-like behavior**.
- Requires **no UI changes** to start; it is a backend orchestration upgrade.
- Works fine with **classic threads / `CompletableFuture`** – no need for Java 21/25 virtual threads.

**Concrete idea:**

- Refactor `ChatService.processChat(...)` to something like:
  - Build an `AgentLoopContext` (user message, conversationId, reasoning state, plan, iterations count).
  - Run a small loop:
    - Step 1: Call `ChatClient` with advisors and tools to **propose a plan/answer**.
    - Step 2: Let `SelfRefineV3Advisor` / `SupervisorBrain` evaluate the draft.
    - Step 3: If the draft is weak / inconsistent and iteration < N, **ask the model to improve** using its own previous answer and feedback.
    - Step 4: When good enough or max iterations reached, return the final answer.

Once this is stable, the **second backend step** should be:

> **Parallelize `ToolFinderService`, `BrainFinderService`, and memory retrieval using `CompletableFuture`.**

This will directly reduce latency without touching your external UI.

---

## 3. How this fits into existing docs

This doc is a short **navigation guide** on top of:

- `AGI_OPTIMIZATION_SUMMARY.md` – global roadmap.
- `PHASE1_EMOTIONAL_INTELLIGENCE_PLAN.md` – personality and affect.
- `PHASE2_CONVERSATIONAL_INTELLIGENCE_PLAN.md` – dialogue flow.
- `HUMAN_UI_ENHANCEMENTS.md` – front-end improvements.

Use this file as the **starting point** when deciding what to build next:

1. Start with the ReAct loop around `ChatClient`.
2. Then add parallelization for ToolFinder/BrainFinder.
3. Only after that, move to sandbox execution and visible CoT.
4. Then consider the long-term GraphRAG and swarm/orchestration features.
