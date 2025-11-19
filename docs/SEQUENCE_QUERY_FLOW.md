# Request Sequence – Hybrid Brain Architecture

This document shows **sequence-style text diagrams** for typical queries, mapping to the advisors/brains in the system.

---

## 1. Simple Math Query – `"what is 2+4?"`

Scenario: very simple calculation, eligible for the **fast path** in `ConductorAdvisor`.

```text
User
  -> ChatBotController: POST /send (message="what is 2+4?", provider="ollama")
ChatBotController
  -> ChatService.processChat(provider, ChatRequest)

ChatService
  -> TraceContext.initialize()
  -> SupervisorBrain.initializeConversation(userId, conversationId)
  -> GlobalBrainContext.setReasoningState(message)
  -> ToolFinderService.findToolsFor("what is 2+4?")
      => VectorStore: similarity search on tool descriptions
      => Suggested tools (e.g. arithmetic tools) stored in ReasoningState
  -> Get ChatClient for provider (ollamaChatClient)
  -> Call ChatClient.prompt() with:
       - systemPrompt (use conversation history)
       - user(message)
       - toolNames(suggestedToolNames)
       - advisors param (conversationId)

Advisors chain (default for Ollama):

ConversationHistoryAdvisor (order -2)
  -> Reads conversationId from GlobalBrainContext
  -> Logs: conversationId + current user message
  -> nextCall()

MessageChatMemoryAdvisor (order -1)
  -> Loads last N messages from ChatMemory for this conversationId
  -> Injects history into prompt
  -> nextCall()

ConductorAdvisor (Brain 0, order 0)
  -> Extracts user query ("what is 2+4?")
  -> Detects: simple arithmetic query
  -> FAST PATH: builds lightweight AgentPlan
       - intent = SIMPLE/CALCULATION
       - complexity ≈ 1
       - requiredTools: e.g. ["add"]
       - selectedBrains: [conductorAdvisor, toolCallAdvisor, personalityAdvisor]
  -> Stores AgentPlan in AgentPlanHolder
  -> Approves requiredTools in ReasoningState
  -> nextCall()

DynamicContextAdvisor (Brain 1, order 10)
  -> Reads AgentPlan from AgentPlanHolder
  -> Logs intent, complexity, selectedBrains, requiredTools
  -> Prepares context for specialist brains (here minimal)
  -> nextCall()

ToolCallAdvisor (Brain 2, order 20)
  -> Reads AgentPlan + ReasoningState
  -> Determines approved tools (from plan) and rejected tools (suggested - approved)
  -> Builds tool execution policy as SystemMessage
  -> Creates modified ChatClientRequest with policy prepended
  -> nextCall(modifiedRequest)

[Core ChatModel execution]
  -> LLM sees:
       - systemPrompt (history + tool policy)
       - conversation history
       - user message
       - available tools metadata
  -> May call the approved arithmetic tool (e.g. add)
  -> Produces answer: "2+4 equals 6"

SelfRefineV3Advisor (Brain 13, order 1000)
  -> Reads AgentPlan (complexity ≈ 1)
  -> For low complexity, may SKIP heavy evaluation to save cost
  -> nextCall() or pass-through

PersonalityAdvisor (Brain 14, order 800)
  -> Applies mentor-style personality via PersonalityEngine
  -> Returns final, friendly response

ChatService (after ChatClient.call())
  -> Reads response content from ChatClient
  -> Reads AgentPlan from AgentPlanHolder
  -> Extracts actually used tools from plan.requiredTools
  -> Builds ChatResponse(answer="2+4 equals 6", provider, usedTools)
  -> Clears GlobalBrainContext, TraceContext, AgentPlanHolder
  -> Returns ChatResponse to ChatBotController

ChatBotController
  -> Returns JSON response to the frontend
User
  <- Sees: "2+4 equals 6" (fast, low tokens)
```

---

## 2. Code Explanation Query – `"explain the add() method in AIAgentToolService"`

Scenario: more complex, code-related query. The full hybrid chain is used; Tool RAG and Brain RAG matter more.

```text
User
  -> ChatBotController: POST /send (message, provider="ollama" or "openai")
ChatBotController
  -> ChatService.processChat(provider, ChatRequest)

ChatService
  -> TraceContext.initialize()
  -> SupervisorBrain.initializeConversation(userId, conversationId)
  -> GlobalBrainContext.setReasoningState(userQuery)
  -> ToolFinderService.findToolsFor(userQuery)
      => VectorStore: semantic search over all indexed @Tool methods
      => Suggested tools include code/project analysis tools
      => Suggested tool names stored in ReasoningState
  -> Get ChatClient for provider
  -> Call ChatClient.prompt() with systemPrompt + user + toolNames(suggestedTools)

Advisors chain:

ConversationHistoryAdvisor (order -2)
  -> Logs conversationId and current message
  -> nextCall()

MessageChatMemoryAdvisor (order -1)
  -> Loads past conversation messages into prompt
  -> nextCall()

ConductorAdvisor (Brain 0, order 0)
  -> Extracts user query (code question)
  -> Analyzes complexity/ambiguity (MEDIUM/HIGH)
  -> intent ≈ EXPLANATION / CODE
  -> focusArea = ARCHITECTURE / IMPLEMENTATION
  -> Reads ReasoningState.suggestedTools (code analysis tools)
  -> identifyRequiredTools(...):
       - For analysis/code queries, trusts ToolFinder’s suggestions
       - Approves all relevant analysis tools
  -> identifySpecialistBrains(...):
       - May include brains for advanced capabilities, quality, learning, etc.
  -> Builds full AgentPlan with intent, strategy (BALANCED/SLOW_REASONING), requiredTools, selectedBrains
  -> Stores AgentPlan in AgentPlanHolder
  -> Approves requiredTools in ReasoningState
  -> nextCall()

DynamicContextAdvisor (Brain 1, order 10)
  -> Reads AgentPlan (intent, complexity, selectedBrains, tools)
  -> May call BrainFinderService.findBrainsFor(plan/intent) to refine specialist brains
  -> Builds context injection describing specialist brains + tools
  -> Stores plan context for downstream advisors
  -> nextCall()

ToolCallAdvisor (Brain 2, order 20)
  -> Reads AgentPlan + ReasoningState
  -> Determines approved vs rejected tools
  -> Injects tool execution policy into prompt as SystemMessage
  -> nextCall(modifiedRequest)

[Specialist brains]
  -> Depending on AgentPlan + Brain RAG results, selected brains (e.g. advanced capabilities, quality, etc.) run and contribute to reasoning.

[Core ChatModel execution]
  -> LLM uses:
       - conversation history
       - user code question
       - context from DynamicContextAdvisor
       - approved tools (e.g. a tool that can read and summarise AIAgentToolService code)
  -> May call code analysis tools to fetch/inspect `add()`
  -> Produces detailed explanation of `add()` behavior

SelfRefineV3Advisor (Brain 13, order 1000)
  -> Receives the detailed explanation
  -> Performs enhanced evaluation:
       - clarity, relevance, helpfulness
       - consistency, hallucination risk
       - code structure validity (if code snippets present)
       - token usage
  -> If rating is low, triggers refinement via internal judgeClient
  -> Logs evaluation and status via SupervisorBrain
  -> Returns final, high-quality explanation

PersonalityAdvisor (Brain 14, order 800)
  -> Applies mentor-style personality to the explanation
  -> Ensures tone is helpful and clear

ChatService
  -> Reads final response + AgentPlan
  -> Extracts actually used tools from plan.requiredTools
  -> Builds ChatResponse(answer, provider, usedTools)
  -> Clears GlobalBrainContext, TraceContext, AgentPlanHolder

ChatBotController
  -> Returns JSON response to frontend
User
  <- Sees a detailed, friendly explanation of `add()` using only relevant tools/brains
```

---

These sequences correspond to the architecture diagrams and the implementation in:

- `ChatBotController`, `ChatService`
- `ToolFinderService`, `BrainFinderService`, `BrainIndexerService`, `ToolIndexingService`
- Advisors/brains under `com.vijay.manager`
- Chat client configuration in `AIProviderConfig`.
