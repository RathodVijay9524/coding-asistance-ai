package com.vijay.manager;

/**
 * 🧠 PHASE 8: Brain RAG Architecture
 * 
 * Interface for all advisor brains to implement.
 * Enables semantic search and dynamic brain selection.
 * 
 * This allows us to:
 * 1. Index all brains by their descriptions
 * 2. Find relevant brains for each query (like ToolFinder)
 * 3. Dynamically build advisor chains (only 3-4 brains per query)
 * 4. Save 98% of tokens and avoid HTTP 413 errors
 */
public interface IAgentBrain {
    
    /**
     * Get the brain's Spring bean name (used by ApplicationContext.getBean())
     * This MUST match the @Component bean name or field name in @Bean
     * Examples: "thoughtStreamAdvisor", "localQueryPlannerAdvisor", "selfRefineV3Advisor"
     * 
     * IMPORTANT: This is the ACTUAL bean name that Spring uses, NOT the class name!
     */
    String getBrainName();
    
    /**
     * Get the brain's description for semantic search
     * This is indexed and used to find relevant brains for queries
     * Examples: 
     * - "Plans the query and decides what to do"
     * - "Analyzes user emotions and sentiment"
     * - "Retrieves relevant code from the codebase"
     */
    String getBrainDescription();
    
    /**
     * Get the brain's execution order in the advisor chain
     * Lower numbers execute first, higher numbers execute last
     * Examples: -1 (ThoughtStream), 0 (Planner), 1000 (Judge)
     */
    int getOrder();
}
