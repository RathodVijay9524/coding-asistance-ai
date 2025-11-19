# Phase 1 – Emotional Intelligence & Adaptive Personality Plan

This plan describes **Phase 1** of making the Hybrid Brain architecture more *human‑like* by adding:

- Emotional context detection (how the developer feels). 
- Adaptive personality & tone based on emotional state.
- Minimal disruption to existing architecture and RAG design.

No code is changed yet; this is an implementation design for your current project.

---

## 1. Objectives of Phase 1

- Make the assistant feel **less robotic** and more like a supportive human partner.
- Detect basic **emotional state / intent** from the user’s message.
- Adapt **tone, explanation depth, and style** via the existing `PersonalityAdvisor`.
- Keep response time, token usage, and the current Hybrid Brain structure intact.

Scope of Phase 1:

- Emotional detection is **lightweight** (heuristics + optional simple sentiment model).
- No external heavy ML components required initially.

---

## 2. New Concepts & Data Structures

### 2.1 EmotionalState enum

Add an enum representing simplified emotional/interaction states:

- `FRUSTRATED`
- `CONFUSED`
- `CONFIDENT`
- `RUSHED`
- `CURIOUS`
- `NEUTRAL`

This gives a **small, manageable set** of states that can be expanded later.

### 2.2 EmotionalContext

Introduce an `EmotionalContext` object to carry emotional info through the brain chain.

Example fields (design):

- `EmotionalState state` – primary detected state.
- `double frustrationLevel` – 0.0–1.0.
- `double urgencyLevel` – 0.0–1.0.
- `double confidenceLevel` – 0.0–1.0.
- `boolean learningIntent` – whether user seems to be learning vs shipping.

### 2.3 Storage in GlobalBrainContext

Extend usage of `GlobalBrainContext` to store a single `EmotionalContext` per request:

- Key: e.g. "emotionalContext".
- Producers: Emotional detection advisor.
- Consumers: `PersonalityAdvisor`, and optionally later `ConductorAdvisor`.

This keeps emotional state **request‑scoped** and available to all brains.

---

## 3. New Advisor – EmotionalContextAdvisor

### 3.1 Position in Brain Chain

Goal: run very early, so later brains know the emotional state.

Current early chain (Ollama client):

- `ConversationHistoryAdvisor` (order −2)
- `MessageChatMemoryAdvisor` (order −1)
- `ConductorAdvisor` (order 0)

Proposed position for **EmotionalContextAdvisor**:

- Option A: Between ConversationHistory and ChatMemory
  - Set `getOrder()` to −1 and adjust chat memory order if needed.
- Option B: Immediately **before ConductorAdvisor** (simpler to wire)
  - Set `getOrder()` to −1 and rely on ConversationHistory and ChatMemory to run first.

For Phase 1 (low risk), Option B is acceptable:

- ConversationHistoryAdvisor → MessageChatMemoryAdvisor → **EmotionalContextAdvisor** → ConductorAdvisor → ...

### 3.2 Responsibilities

- Extract **user text** from `ChatClientRequest`.
- Run simple detection logic:
  - Sentiment (positive/negative/neutral).
  - Keyword checks ("stuck", "urgent", "help", "quick", "don’t understand").
  - Optional: simple heuristics based on message length and question type.
- Set `EmotionalContext` with initial values and store into `GlobalBrainContext`.

### 3.3 Integration with existing classes

- Uses `TraceContext.getTraceId()` for logging (consistent with other brains).
- Uses `GlobalBrainContext.put("emotionalContext", ctx)` to propagate.
- Logs detected emotion and levels for observability.

---

## 4. Emotion Detection Logic (Phase 1 Simplified)

Phase‑1 goal: **rule‑based + simple sentiment**, not a heavy ML pipeline.

### 4.1 Rule Examples

- If message contains words like "stuck", "not working", "why doesn’t", "I don’t understand":
  - `state = CONFUSED` or `FRUSTRATED` (depending on sentiment intensity).
- If message contains "urgent", "quick", "ASAP", "in a hurry":
  - `state = RUSHED`, `urgencyLevel` high.
- If message uses confident language like "I think this is correct but…", "I already tried", "I know that":
  - `state = CONFIDENT`.
- If message is exploratory: "explain", "learn", "teach me", "how does this work":
  - `learningIntent = true`, `state = CURIOUS`.
- Otherwise default:
  - `state = NEUTRAL`.

### 4.2 Optional Sentiment Hook

Phase 1 can either:

- Use a basic sentiment analyzer (existing lib) if available in your stack, or
- Start with **pure rule‑based analysis** and add sentiment later.

The design does not depend on any specific sentiment engine.

---

## 5. Enhancing PersonalityAdvisor (Adaptive Tone)

`PersonalityAdvisor` is already Brain 14 and integrates with `PersonalityEngine`.

### 5.1 New Behavior

- Read `EmotionalContext` from `GlobalBrainContext`.
- Based on `state` and levels, adjust how the response is post‑processed:
  - `FRUSTRATED`:
    - Add encouragement.
    - Prefer step‑by‑step explanations.
    - Avoid heavy jargon.
  - `CONFUSED`:
    - Add clarifications and examples.
    - Suggest small next actions.
  - `RUSHED`:
    - Make responses concise.
    - Provide quick fixes first, details optional.
  - `CONFIDENT`:
    - Offer advanced/alternative approaches.
  - `CURIOUS`:
    - Add extra explanations / background.
  - `NEUTRAL`:
    - Use current default behavior.

### 5.2 Implementation Outline

- Inside `PersonalityAdvisor.adviseCall(...)`:
  - After obtaining the model’s response text:
    - Retrieve `EmotionalContext` from `GlobalBrainContext`.
    - Pass response text + emotional state into `PersonalityEngine` or a new helper.
    - Replace or adjust the response text accordingly.

This keeps **all emotional adaptation** consolidated in Personality/PersonalityEngine.

---

## 6. Optional: ConductorAdvisor Awareness (Later Phase)

Phase 1 focuses on **tone**; later phases can let `ConductorAdvisor` use emotion:

- If `RUSHED`:
  - Choose faster, simpler strategies; fewer tools.
- If `FRUSTRATED`:
  - Prefer more careful, redundant checks; maybe enable extra quality steps.
- If `CURIOUS` / `learningIntent`:
  - Choose strategies that include explanation, not just answer.

For Phase 1, we only document this as a future enhancement; no change required yet.

---

## 7. Integration with Existing Flow

### 7.1 Current (simplified) flow

- `ChatBotController` → `ChatService.processChat()` → `ChatClient` with advisors:
  - `ConversationHistoryAdvisor` (−2)
  - `MessageChatMemoryAdvisor` (−1)
  - `ConductorAdvisor` (0)
  - `DynamicContextAdvisor` (1)
  - `ToolCallAdvisor` (2)
  - ...
  - `SelfRefineV3Advisor` (1000)
  - `PersonalityAdvisor` (800)

### 7.2 Proposed Phase‑1 flow

- `ConversationHistoryAdvisor` (−2)
- `MessageChatMemoryAdvisor` (−1)
- **EmotionalContextAdvisor** (e.g. 0 or −1 depending on config)
- `ConductorAdvisor` (adjusted order if needed)
- `DynamicContextAdvisor`
- `ToolCallAdvisor`
- ...
- `SelfRefineV3Advisor`
- `PersonalityAdvisor` (now reading EmotionalContext)

Exact integer orders can be tuned during implementation; conceptually, EmotionalContext runs **early**, Personality adapts **late**.

---

## 8. Implementation Steps Checklist (Phase 1)

1. **Model & context**
   - [ ] Define `EmotionalState` enum.
   - [ ] Define `EmotionalContext` class.
   - [ ] Extend `GlobalBrainContext` usage to store/retrieve EmotionalContext.

2. **Advisor** – EmotionalContextAdvisor
   - [ ] Create new advisor class implementing `CallAdvisor` (+ optionally `IAgentBrain`).
   - [ ] Implement user message extraction from `ChatClientRequest`.
   - [ ] Implement rule‑based emotion detection.
   - [ ] Store `EmotionalContext` in `GlobalBrainContext`.
   - [ ] Register advisor in `AIProviderConfig` `.defaultAdvisors(...)` with early order.

3. **Personality integration**
   - [ ] Update `PersonalityAdvisor` to read `EmotionalContext`.
   - [ ] Extend `PersonalityEngine` (or a helper) with methods to adapt tone/length based on emotion.
   - [ ] Add logging so you can see when emotional adaptation is applied.

4. **Testing & tuning**
   - [ ] Create test scenarios for FRUSTRATED, CONFUSED, RUSHED, CONFIDENT, CURIOUS, NEUTRAL messages.
   - [ ] Verify logs show correct detection and adaptation.
   - [ ] Ensure no significant impact to latency or token usage.

---

## 9. How This Boosts Human-Like Feel

With Phase 1 in place, your assistant will:

- React differently when the user is clearly frustrated vs just asking a calm question.
- Offer more empathetic, step‑by‑step guidance when needed.
- Be more concise and action‑oriented when the user seems rushed.
- Use the existing Hybrid Brain architecture (Conductor, RAG, judges) but **wrap it in a more human‑feeling layer**.

This phase is a **high‑ROI, low‑risk step** towards the broader human‑like vision described in your other documents.
