# Executive Summary – Coding AI Hybrid Brain

## What this system is

Coding AI is a **Spring Boot + Spring AI** web chatbot that can talk to multiple AI providers (Ollama, OpenAI, Anthropic, Google Gemini, Hugging Face) and use many **code/project tools** (analysis, refactor, documentation, etc.).

It is designed as a **multi-brain, tool-augmented coding assistant** for working on real projects efficiently.

---

## Key idea

Instead of running *all* tools and *all* advisor "brains" for every question, the system uses:

- **Tool RAG** – semantic search over tool descriptions to pick only 2–3 relevant tools.
- **Brain RAG + planning** – semantic search + a master planner to pick only the relevant brains.

A single **master plan** (`AgentPlan`) drives which tools and brains are used, per request.

---

## High-level flow

1. **User sends a message** to `/send` via the web UI.
2. **`ChatBotController`** passes the message and provider to **`ChatService`**.
3. **`ChatService`**:
   - Creates/uses a stable **conversationId** per browser session.
   - Initializes tracing and global context.
   - Calls **`ToolFinderService`**, which uses a **tool vector store** to find 2–3 tools whose descriptions best match the query.
   - Calls the appropriate **`ChatClient`** for the selected provider, passing:
     - A system prompt (how to behave + use history).
     - The user message.
     - The suggested tool names.
     - A `conversationId` parameter for memory.
4. Around the ChatClient, a chain of **advisor brains** runs:
   - **ConversationHistory + ChatMemory** – load recent messages.
   - **ConductorAdvisor (Brain 0)** – builds the **AgentPlan** (intent, complexity, required tools, specialist brains).
   - **DynamicContextAdvisor (Brain 1)** – uses the plan and Brain RAG to decide which specialist brains/tools actually matter and injects context.
   - **ToolCallAdvisor (Brain 2)** – enforces a **tool policy** (approved vs rejected tools) so the model cannot call arbitrary tools.
   - **Specialist brains** – only the ones selected by the plan/Brain RAG.
   - **SelfRefineV3Advisor (Brain 13)** – final quality gate; evaluates clarity, relevance, hallucination risk, code structure, and can trigger refinement.
   - **PersonalityAdvisor (Brain 14)** – applies a consistent mentor-style personality.
   - **SupervisorBrain** – tracks conversation state and brain performance.
5. The ChatClient returns the final response text.
6. **`ChatService`** reads the `AgentPlan` to see which tools were actually used, builds a `ChatResponse`, clears contexts, and returns the result to the UI.

---

## Startup indexing (how RAG is enabled)

On application startup:

- **ToolIndexingService**:
  - Scans all `AiToolProvider` beans.
  - Indexes every `@Tool` method description into a **tool vector store**.
- **BrainIndexerService**:
  - Scans all advisor brains that implement `IAgentBrain`.
  - Indexes their descriptions into a **brain vector store**.

This makes both tools and brains **searchable by meaning**, so ToolFinder and BrainFinder can pick the most relevant ones per query.

---

## Benefits (Before → After)

Previously (static chain, all brains every time):

- **Brains per query:** 13 (all of them).
- **Token usage:** ~10,000+ tokens per request.
- **Latency:** ~5–10 seconds.
- **Errors:** HTTP 413 (“Request too large”) issues due to huge prompts.
- **Quality:** Sometimes confused responses from too many brains.

With the **Hybrid Brain + Tool/Brain RAG** architecture:

- **Brains per query:** typically **3–4** active brains (plus memory).
- **Token usage:** ~**2,000–3,000** tokens per request.
- **Latency:** ~**1–2 seconds** for many queries.
- **Errors:** HTTP 413 effectively eliminated by smaller, focused prompts.
- **Quality:**
  - Single master plan avoids conflicting planners.
  - Plan-aware tool usage reduces tool noise and errors.
  - Final judge + personality give focused, high-quality, human-like answers.

---

## How to use this summary

This document is intended for **architects and stakeholders** who need a quick understanding of:

- What the system does.
- How the main flow works.
- Why the Hybrid Brain architecture is better (tokens, speed, quality).

For deeper technical detail, see:

- `HYBRID_BRAIN_ARCHITECTURE.md` – full architecture.
- `BRAIN_ADVISORS_DETAIL.md` – per-brain/advisor explanation.
- `BRAIN_ADVISORS_TABLE.md` – one-page brain/advisor table.
- `SEQUENCE_QUERY_FLOW.md` – sequence-style flow diagrams.
