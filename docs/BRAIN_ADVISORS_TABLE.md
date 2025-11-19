# Hybrid Brain – Brains & Advisors Cheat Sheet

This table summarizes the main advisors/brains, their execution order, role, and whether they are part of the core default chain.

Lower `order` values run earlier in the advisor chain.

| Order | Name                        | Bean / ID                | Type          | Core in default chain? | Short purpose                                                |
|------:|-----------------------------|---------------------------|---------------|------------------------|--------------------------------------------------------------|
|   -2  | ConversationHistoryAdvisor  | `conversationHistory`     | CallAdvisor   | Ollama only            | Logs conversation ID & current message before history load. |
|   -1  | MessageChatMemoryAdvisor    | (Spring AI advisor)       | CallAdvisor   | Yes                    | Loads last N messages from `ChatMemory` into the prompt.    |
|    0  | ConductorAdvisor (Brain 0)  | `conductorAdvisor`        | IAgentBrain   | Yes                    | Unified master planner, builds `AgentPlan`.                 |
|   10  | DynamicContextAdvisor (B1)  | `dynamicContextAdvisor`   | IAgentBrain   | Yes                    | Reads plan, selects specialist brains/tools, builds context.|
|   20  | ToolCallAdvisor (B2)        | `toolCallAdvisor`         | IAgentBrain   | Yes                    | Enforces tool policy (approved vs rejected tools).          |
|  800  | PersonalityAdvisor (B14)    | `personalityAdvisor`      | IAgentBrain   | Yes                    | Applies consistent personality/tone to responses.           |
| 1000  | SelfRefineV3Advisor (B13)   | `selfRefineV3Advisor`     | IAgentBrain   | Yes                    | Enhanced judge; multi-check quality evaluation & refinement.|
| 1000  | MultiCriteriaJudgeAdvisor   | `multiCriteriaJudgeAdvisor`| CallAdvisor  | Optional               | Alternate judge: clarity, relevance, factual, helpfulness.  |
|   —   | SupervisorBrain             | `supervisorBrain`         | Service       | Yes                    | Tracks conversation state and brain performance.            |
|   —   | ChainOfThoughtPlannerAdvisor| `chainOfThoughtPlannerAdvisor` | CallAdvisor | Optional           | Deep chain-of-thought planner used for analysis/experiments.|

Notes:

- `IAgentBrain` implementations are indexed into `brainVectorStore` by `BrainIndexerService` and can be selected by **Brain RAG**.
- Not all advisors are always active in every ChatClient; the core chain is defined in `AIProviderConfig` via `.defaultAdvisors(...)`.
- `SupervisorBrain` is not a CallAdvisor; it is a supporting service used mainly by `ChatService` and `SelfRefineV3Advisor`.
