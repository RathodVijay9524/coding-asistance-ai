package com.vijay.brain;

/**
 * IAgentBrain - Interface for all 13 "Brain" Advisors
 * 
 * Each brain (advisor) must implement this interface to be discoverable
 * by the BrainFinderService for dynamic brain selection (Brain RAG).
 * 
 * This allows the system to:
 * 1. Index all brains at startup
 * 2. Semantically search for relevant brains per query
 * 3. Dynamically build ChatClient with only selected brains
 * 4. Reduce token usage by 80% and response time by 75%
 */
public interface IAgentBrain {
    
    /**
     * Get the unique name of this brain
     * Example: "SelfRefineV3Advisor", "ThoughtStreamAdvisor"
     */
    String getBrainName();
    
    /**
     * Get a detailed description of what this brain does
     * This is used for semantic search to find relevant brains
     * 
     * Example: "This brain is a 'Quality Assurance Judge'. It runs last. 
     * It evaluates the AI's final answer for quality, factual accuracy, 
     * consistency, and tone. If the quality is too low, it forces the AI 
     * to re-evaluate and improve its answer."
     */
    String getBrainDescription();
    
    /**
     * Get the execution order of this brain in the advisor chain
     * Lower numbers run first, higher numbers run last
     * 
     * Example orders:
     * -1: ThoughtStreamAdvisor (cursor/attention - runs first)
     * 0: LocalQueryPlannerAdvisor (planner)
     * 1000: SelfRefineV3Advisor (judge - runs last)
     */
    int getOrder();
}
