package com.vijay.config;

import com.vijay.manager.AiToolProvider;
import com.vijay.manager.ConductorAdvisor;
import com.vijay.manager.DynamicContextAdvisor;
import com.vijay.manager.ToolCallAdvisor;
import com.vijay.manager.PersonalityAdvisor;
import com.vijay.manager.SelfRefineV3Advisor;
import com.vijay.service.FileHashTracker;
import com.vijay.service.IncrementalIndexer;
import com.vijay.service.IncrementalSummarizer;
import com.vijay.service.IncrementalGraphCalculator;
import com.vijay.tools.AIAgentToolService;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.huggingface.HuggingfaceChatModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AIProviderConfig {

    private static final Logger logger = LoggerFactory.getLogger(AIProviderConfig.class);

    // Chat Memory for conversation context
    @Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
    }
    @Bean
    public ToolCallingManager toolCallingManager() {
        System.out.println("tool callback working");
        return ToolCallingManager.builder()
                .toolExecutionExceptionProcessor((toolName) -> {
                    System.out.println("--- TOOL EXECUTION FAILED: " + toolName + " ---");
                    return "Tool execution failed: ";
                })
                .build();
    }

    // Code Understanding Vector Stores (Using Local Ollama!)
    // Note: OllamaEmbeddingModel will be auto-configured by Spring AI from application.properties
    @Bean
    @Qualifier("summaryVectorStore")
    public VectorStore summaryVectorStore(OllamaEmbeddingModel embeddingModel) {
        logger.info("🚀 Creating Summary Vector Store using LOCAL Ollama embeddings (nomic-embed-text) - NO TOKENS USED!");
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    @Qualifier("chunkVectorStore") 
    public VectorStore chunkVectorStore(OllamaEmbeddingModel embeddingModel) {
        logger.info("🚀 Creating Chunk Vector Store using LOCAL Ollama embeddings (nomic-embed-text) - NO TOKENS USED!");
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    // 🧠 PHASE 8: Brain RAG Vector Store
    @Bean
    @Qualifier("brainVectorStore")
    public VectorStore brainVectorStore(OllamaEmbeddingModel embeddingModel) {
        logger.info("🧠 Creating Brain Vector Store for semantic brain selection (RAG-based advisor chain)");
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    // LOCAL OLLAMA CLIENT (Token-Free!) 🚀

    @Bean(name = "ollamaChatClient")
    @Primary
    ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel,
                                ChatMemory chatMemory,
                               com.vijay.manager.ConversationHistoryAdvisor conversationHistory,
                               ConductorAdvisor conductor,
                               DynamicContextAdvisor dynamicContext,
                               ToolCallAdvisor toolCall,
                               SelfRefineV3Advisor judge,
                               PersonalityAdvisor personality,
                                java.util.List<AiToolProvider> allToolProviders
                               ) {
        logger.info("🎼 Creating UNIFIED CONDUCTOR Chat Client - 5 Core Brains + 25 AI Tools + Dynamic RAG");
        logger.info("   Brain 0: ConductorAdvisor (The Unified Master Planner) ⭐");
        logger.info("   Brain 1: DynamicContextAdvisor (The Context Fetcher)");
        logger.info("   Brain 2: ToolCallAdvisor (Plan-Aware Tool Executor) ⭐");
        logger.info("   Brain 13: SelfRefineV3Advisor (The Judge)");
        logger.info("   Brain 14: PersonalityAdvisor (The Voice)");
        logger.info("   + 25 AI Tools:");
        logger.info("     Week 1: ProjectAnalysis, CodeGeneration, CodeQuality, CodeReview, TestGeneration");
        logger.info("     Week 2: Refactoring, BugDetection, PerformanceAnalysis, SecurityScanning, Documentation");
        logger.info("     Week 3: SpringConfig, SpringContext, SpringBestPractices, SpringDependency, FileWatching, LiveFeedback, ChangeAnalysis");
        logger.info("     Week 4: DatabaseSchema, MigrationScript, QueryOptimization, DockerConfig, CICDPipeline, EnvironmentConfig, NLToCode, GenerateFromDescription");
        logger.info("   + Specialist Brains (3-12) dynamically selected via RAG");
        
        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(
                        conversationHistory,  // Order: -2 - Load & log conversation history
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),  // Order: -1 - LOAD HISTORY
                    conductor,          // Brain 0: Unified Conductor (order: 0) - Creates ONE master plan
                    dynamicContext,     // Brain 1: Dynamic Context (order: 1) - Reads plan, fetches specialist context
                    toolCall,           // Brain 2: Tool Call (order: 2) - Reads plan, executes tools if needed
                    judge,              // Brain 13: Self-Refine V3 (order: 1000) - Final quality gate
                    personality         // Brain 14: Personality (order: 800) - Response personality
                )
                .defaultTools((Object[]) allToolProviders.toArray(new AiToolProvider[0]))
                .build();
    }

    // OpenAI client (backup for complex reasoning when needed)
    @Bean(name = "openAiChatClient")
    ChatClient openAiChatClient(OpenAiChatModel openAiChatModel,
                                ChatMemory chatMemory,
                                ConductorAdvisor conductor,
                                DynamicContextAdvisor dynamicContext,
                                ToolCallAdvisor toolCall,
                                SelfRefineV3Advisor judge,
                                PersonalityAdvisor personality,
                                java.util.List<AiToolProvider> allToolProviders) {
        logger.info("🧠 Creating OpenAI Chat Client - Multi-Brain Architecture v7.0 (Supervisor Brain + Self-Refine V3)");
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),  // Order: -1 - LOAD HISTORY
                        conductor,          // Brain 0: Unified Conductor (order: 0) - Creates ONE master plan
                        dynamicContext,     // Brain 1: Dynamic Context (order: 1) - Reads plan, fetches specialist context
                        toolCall,           // Brain 2: Tool Call (order: 2) - Reads plan, executes tools if needed
                        judge,              // Brain 13: Self-Refine (order: 1000) - Evaluates quality
                        personality
                )
                .defaultTools((Object[]) allToolProviders.toArray(new AiToolProvider[0]))  // All tools from all providers
                .build();
    }

    @Bean(name = "anthropicChatClient")
    ChatClient anthropicChatClient(AnthropicChatModel anthropicChatModel,
                                   ChatMemory chatMemory,
                                   ConductorAdvisor conductor,
                                   DynamicContextAdvisor dynamicContext,
                                   ToolCallAdvisor toolCall,
                                   SelfRefineV3Advisor judge,
                                   PersonalityAdvisor personality,
                                   java.util.List<AiToolProvider> allToolProviders) {
        logger.info("Creating Anthropic Chat Client with MCP tools");
        return ChatClient.builder(anthropicChatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),  // Order: -1 - LOAD HISTORY
                        conductor,          // Brain 0: Unified Conductor (order: 0) - Creates ONE master plan
                        dynamicContext,     // Brain 1: Dynamic Context (order: 1) - Reads plan, fetches specialist context
                        toolCall,           // Brain 2: Tool Call (order: 2) - Reads plan, executes tools if needed
                        judge,              // Brain 13: Self-Refine (order: 1000) - Evaluates quality
                        personality
                )
                .defaultTools((Object[]) allToolProviders.toArray(new AiToolProvider[0]))  // All tools from all providers
                .build();
    }

    @Bean(name = "googleChatClient")
    ChatClient geminChatClient(GoogleGenAiChatModel googleGenAiChatModel,
                                ChatMemory chatMemory,
                               ConductorAdvisor conductor,
                               DynamicContextAdvisor dynamicContext,
                               ToolCallAdvisor toolCall,
                               SelfRefineV3Advisor judge,
                               PersonalityAdvisor personality,
                               java.util.List<AiToolProvider> allToolProviders) {
        logger.info("Creating google Chat Client with MCP tools");
        return ChatClient.builder(googleGenAiChatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),  // Order: -1 - LOAD HISTORY
                        conductor,          // Brain 0: Unified Conductor (order: 0) - Creates ONE master plan
                        dynamicContext,     // Brain 1: Dynamic Context (order: 1) - Reads plan, fetches specialist context
                        toolCall,           // Brain 2: Tool Call (order: 2) - Reads plan, executes tools if needed
                        judge,              // Brain 13: Self-Refine (order: 1000) - Evaluates quality
                        personality
                )
                .defaultTools((Object[]) allToolProviders.toArray(new AiToolProvider[0]))  // All tools from all providers
                .build();
    }


    @Bean(name = "haggingFaceChatClient")
    ChatClient huggingfaceChatClient(HuggingfaceChatModel huggingfaceChatModel,
                                     ChatMemory chatMemory,
                                     java.util.List<AiToolProvider> allToolProviders) {
        logger.info("Creating HaggingFace Chat Client with MCP tools");
        return ChatClient.builder(huggingfaceChatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()  // Memory
                )
                .defaultTools((Object[]) allToolProviders.toArray(new AiToolProvider[0]))  // All tools from all providers
                .build();
    }

    // ============ PHASE 8: INCREMENTAL INDEXING SERVICES ============

    /**
     * Initialize Phase 8 services for incremental indexing
     */
    public void initializePhase8Services(FileHashTracker fileHashTracker,
                                        IncrementalIndexer incrementalIndexer,
                                        IncrementalSummarizer incrementalSummarizer,
                                        IncrementalGraphCalculator incrementalGraphCalculator) {
        logger.info("🚀 Initializing Phase 8: Incremental Indexing Services");
        logger.info("   ✅ FileHashTracker - File change detection");
        logger.info("   ✅ IncrementalIndexer - Incremental chunk indexing");
        logger.info("   ✅ IncrementalSummarizer - Incremental summarization");
        logger.info("   ✅ IncrementalGraphCalculator - Incremental graph updates");
        logger.info("🎯 Phase 8 Services Ready!");
    }

    // ============ PHASE 8: DYNAMIC BRAIN SELECTION ============

    /**
     * Build a ChatClient with ONLY the selected brains
     * This is the key to Brain RAG - instead of running all 13 brains,
     * we only run the 3-4 most relevant brains for this query
     * 
     * @param chatModel The chat model to use (OpenAI, Ollama, etc.)
     * @param selectedBrainBeans List of brain advisor beans to include
     * @param allToolProviders The tool service for function calling
     * @return ChatClient with only selected brains
     */
    public ChatClient buildDynamicChatClient(
            org.springframework.ai.chat.model.ChatModel chatModel,
            java.util.List<org.springframework.ai.chat.client.advisor.api.CallAdvisor> selectedBrainBeans,
            java.util.List<AiToolProvider> allToolProviders) {
        
        logger.info("🔧 Building dynamic ChatClient with {} selected brains", selectedBrainBeans.size());
        
        if (selectedBrainBeans.isEmpty()) {
            logger.warn("⚠️ No brains selected, building client with no advisors");
            return ChatClient.builder(chatModel)
                    .defaultTools((Object[]) allToolProviders.toArray(new AiToolProvider[0]))
                    .build();
        }
        
        // Convert list to array for defaultAdvisors()
        org.springframework.ai.chat.client.advisor.api.CallAdvisor[] advisorArray = 
            selectedBrainBeans.toArray(new org.springframework.ai.chat.client.advisor.api.CallAdvisor[0]);
        
        logger.info("✅ Dynamic ChatClient built with {} advisors", advisorArray.length);
        
        return ChatClient.builder(chatModel)
                .defaultAdvisors(advisorArray)
                .defaultTools((Object[]) allToolProviders.toArray(new AiToolProvider[0])) // All tools available
                .build();
    }

}
