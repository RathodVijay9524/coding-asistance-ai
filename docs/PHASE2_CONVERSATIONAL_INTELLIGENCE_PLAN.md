# Phase 2 – Conversational Intelligence & Flow Plan

This plan describes **Phase 2** of human-like enhancements for the Hybrid Brain architecture:

- Make conversations feel **continuous**, not just single Q&A.
- Improve **conversational flow**: follow-ups, clarifications, deeper dives, topic changes.
- Build on existing **ChatMemory** and advisors without breaking the current flow.

---

## 1. Objectives of Phase 2

- Treat conversations as **threads**, not independent messages.
- Enable the system to:
  - Recognize **follow-up questions** vs **new topics**.
  - Provide **clarifications** and **deeper dives** naturally.
  - Avoid repeating the same explanations.
  - Keep track of the **current topic and user goal**.
- Reuse the existing Hybrid Brain (Conductor, DynamicContext, ToolCall, judges, Personality) and **layer conversational intelligence on top**.

---

## 2. New Concepts & Data Structures

### 2.1 ConversationIntent enum

Represent the role of the current user message in the ongoing dialogue:

- `NEW_TOPIC` – starting a new subject.
- `FOLLOW_UP` – directly related follow-up question.
- `CLARIFICATION` – asking to re-explain or simplify something.
- `DEEPER_DIVE` – asking for more detail or advanced view of the same topic.
- `RELATED_TOPIC` – branching to something connected.

### 2.2 ConversationContext

A higher-level representation of the current conversation thread (not just raw messages).

Example fields:

- `String currentTopic` – high-level topic (e.g. "Tool RAG", "Controller bug").
- `String userGoal` – e.g. "learn", "fix bug", "refactor", "design architecture".
- `ConversationIntent lastIntent` – last message’s role.
- `List<String> keyConcepts` – extracted concepts (classes, patterns, APIs).
- `boolean isOngoingThread` – whether we are in the same topic as before.

### 2.3 Storage & Lifetime

We can keep `ConversationContext` in **two layers**:

1. **Short-term (per request)** – via `GlobalBrainContext` (like EmotionalContext).
2. **Medium-term (per session)** – via a new `ConversationContextStore` keyed by `conversationId`.

This allows:

- Advisors in a single call to share context.
- Different requests in the same session to reuse conversation thread information.

---

## 3. New Service – ConversationalContextManager

### 3.1 Role

Encapsulate logic for:

- Building/updating `ConversationContext` from:
  - Recent messages (via ChatMemory).
  - Current user message.
- Detecting `ConversationIntent`.
- Exposing context to advisors.

### 3.2 Responsibilities

- `ConversationContext getOrCreateContext(conversationId)`:
  - Load existing context from store (if any).
  - Otherwise, create a new one with default values.

- `ConversationIntent detectIntent(String currentMessage, List<RecentTurn> history)`:
  - Simple heuristics:
    - Contains "again", "explain", "I didn’t understand" → `CLARIFICATION`.
    - Contains "more detail", "deeper", "advanced" → `DEEPER_DIVE`.
    - Contains references like "that method", "previous example" → `FOLLOW_UP`.
    - Contains strong topic shift words or new class names → `NEW_TOPIC`.

- `ConversationContext updateContext(ConversationContext ctx, String currentMessage, ConversationIntent intent)`:
  - Update `currentTopic`, `userGoal`, `keyConcepts` as needed.
  - Set `lastIntent`.

- `void saveContext(conversationId, ConversationContext ctx)`.

This service is **framework-agnostic** and can be used by advisors or services.

---

## 4. Advisor Enhancements

### 4.1 DynamicContextAdvisor – Conversational Awareness

`DynamicContextAdvisor` (Brain 1) is already responsible for:

- Reading the `AgentPlan` from Conductor.
- Selecting specialist brains/tools.
- Preparing context for downstream advisors.

**New behavior in Phase 2:**

- Before building context, call `ConversationalContextManager` to:
  - Get the current `ConversationContext` for this `conversationId`.
  - Detect `ConversationIntent` for the current message.
  - Update and save the `ConversationContext`.
- Use `ConversationIntent` and `ConversationContext` to adjust behavior:
  - `FOLLOW_UP`:
    - Emphasize continuity: "Building on what we discussed...".
    - Bring in previous conclusions more strongly.
  - `CLARIFICATION`:
    - Reduce complexity; use simpler wording and more examples.
  - `DEEPER_DIVE`:
    - Increase depth: add internals, edge cases, trade-offs.
  - `NEW_TOPIC`:
    - Treat as a fresh plan; reset some context.
  - `RELATED_TOPIC`:
    - Show how this relates to earlier topic.

The actual text/styling can be implemented via additional context injection or hints passed to downstream advisors and the LLM.

### 4.2 ConversationHistoryAdvisor – Logging Context

Extend `ConversationHistoryAdvisor` (already at order −2) to log:

- Current `ConversationIntent` (if previously known for this conversationId).
- Current `ConversationContext.currentTopic` and `userGoal`.

This is primarily for debugging and observability, not for business logic.

---

## 5. Using ChatMemory More Intelligently

You already have `MessageChatMemoryAdvisor` + `ChatMemory`. Phase 2 builds on top of this:

- Instead of only replaying raw messages, derive:
  - Topics.
  - High-level steps taken.
  - What was already explained.

Phase 2 does **not** require changing `ChatMemory` internals; it only **interprets** history via `ConversationalContextManager`.

---

## 6. Integration with Existing Flow

### 6.1 Current (simplified) flow

- `ConversationHistoryAdvisor` (−2)
- `MessageChatMemoryAdvisor` (−1)
- `EmotionalContextAdvisor` (Phase 1)
- `ConductorAdvisor` (0)
- `DynamicContextAdvisor` (1)
- `ToolCallAdvisor` (2)
- ...
- `SelfRefineV3Advisor` (1000)
- `PersonalityAdvisor` (800)

### 6.2 Phase 2 additions

- `DynamicContextAdvisor` now calls `ConversationalContextManager` to:
  - Load/update `ConversationContext`.
  - Use `ConversationIntent` to shape context and hints.

- `ConversationHistoryAdvisor` logs conversation-level information from `ConversationContext` where available.

No changes are required for Conductor, ToolCall, or judges in Phase 2; they simply operate with improved context.

---

## 7. Implementation Steps Checklist (Phase 2)

1. **Model & context**
   - [ ] Define `ConversationIntent` enum.
   - [ ] Define `ConversationContext` class.
   - [ ] Implement `ConversationContextStore` (per conversationId; in-memory or persistent).

2. **Service** – ConversationalContextManager
   - [ ] Implement creation/loading of `ConversationContext` for a conversationId.
   - [ ] Implement `detectIntent(currentMessage, history)` using simple heuristics.
   - [ ] Implement `updateContext(ctx, currentMessage, intent)`.
   - [ ] Implement saving back to store.

3. **DynamicContextAdvisor enhancement**
   - [ ] Inject `ConversationalContextManager`.
   - [ ] Inside `adviseCall`, retrieve/update `ConversationContext` using `conversationId` from `GlobalBrainContext`.
   - [ ] Use `ConversationIntent` to adjust context injection and logging.

4. **ConversationHistoryAdvisor enhancement** (optional but useful)
   - [ ] Read `ConversationContext` (if available) and log current topic/goal.

5. **Testing & tuning**
   - [ ] Test sequences:
     - NEW_TOPIC → FOLLOW_UP → CLARIFICATION.
     - NEW_TOPIC → DEEPER_DIVE.
     - NEW_TOPIC → RELATED_TOPIC.
   - [ ] Verify:
     - Context is carried across multiple messages.
     - Responses refer back to previous messages appropriately.
     - No significant performance regression.

---

## 8. Future Link to Phase 3 (Developer Profiling)

Phase 2 lays the foundation for personalization by:

- Providing structured understanding of **what** the user is doing over multiple turns (goals, topics).
- Tracking learning-oriented vs production-oriented conversations.

Phase 3 (Developer Profile) can then add:

- Long-term preferences and strengths/weaknesses per developer.
- Integration of `DeveloperProfile` into ConversationContext and `AgentPlan`.

For now, Phase 2 focuses on **better conversation flow** using structures that already blend well with the Hybrid Brain and ChatMemory.
