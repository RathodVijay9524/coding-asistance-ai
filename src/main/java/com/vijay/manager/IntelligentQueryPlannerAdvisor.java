package com.vijay.manager;

import com.vijay.service.QueryPlanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

@Component
public class IntelligentQueryPlannerAdvisor implements CallAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(IntelligentQueryPlannerAdvisor.class);
    
    private final QueryPlanner queryPlanner;

    public IntelligentQueryPlannerAdvisor(QueryPlanner queryPlanner) {
        this.queryPlanner = queryPlanner;
    }

    @Override
    public String getName() {
        return "IntelligentQueryPlannerAdvisor";
    }

    @Override
    public int getOrder() {
        return 0; // Run FIRST - this creates the search strategy
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("🧠 Brain 0 (Intelligent Query Planner): Creating search strategy...");
        
        try {
            String userQuery = extractUserMessage(request);
            if (userQuery.isEmpty()) {
                logger.info("⚠️ Brain 0: Empty query, proceeding with default flow");
                return chain.nextCall(request);
            }

            // Create intelligent search plan
            QueryPlanner.SearchPlan plan = queryPlanner.createSearchPlan(userQuery);
            
            // Log the comprehensive plan
            logSearchPlan(plan);
            
            // Store plan in request context for other advisors to use
            // Note: In a real implementation, you'd store this in a request-scoped context
            // For now, we'll just log the plan for visibility
            
            // Determine if this needs special handling
            if (shouldUseSpecialHandling(plan)) {
                logger.info("🎯 Brain 0: Complex query detected - activating enhanced processing");
                // In a real implementation, you might modify the request or set flags
            }
            
            return chain.nextCall(request);
            
        } catch (Exception e) {
            logger.error("❌ Brain 0: Error in intelligent query planning - {}", e.getMessage());
            return chain.nextCall(request);
        }
    }

    private void logSearchPlan(QueryPlanner.SearchPlan plan) {
        logger.info("📋 Intelligent Search Plan:");
        logger.info("   🎯 Intent: {} (confidence: {:.2f})", plan.intent, plan.confidence);
        logger.info("   🔍 Strategy: {}", plan.searchStrategy);
        logger.info("   📊 Complexity: {}", plan.complexity);
        
        if (!plan.targetEntities.isEmpty()) {
            logger.info("   🎯 Target Entities: {}", plan.targetEntities);
        }
        
        if (!plan.startingFiles.isEmpty()) {
            logger.info("   📂 Starting Files: {}", plan.startingFiles);
        }
        
        logger.info("   🔗 Search Params: topK={}, maxHops={}, reverseDeps={}", 
            plan.topK, plan.maxHops, plan.includeReverseDeps);
        logger.info("   💰 Token Budget: {} tokens", plan.tokenBudget);
        
        if (!plan.searchKeywords.isEmpty()) {
            logger.info("   🔍 Search Keywords: {}", plan.searchKeywords);
        }
        
        // Strategy-specific insights
        logStrategyInsights(plan);
    }

    private void logStrategyInsights(QueryPlanner.SearchPlan plan) {
        switch (plan.searchStrategy) {
            case "dependency_graph":
                logger.info("   💡 Strategy Insight: Wide exploration of architectural relationships");
                break;
            case "entity_centered":
                logger.info("   💡 Strategy Insight: Focused search around specific entities");
                break;
            case "method_focused":
                logger.info("   💡 Strategy Insight: Deep dive into implementation details");
                break;
            case "error_trace":
                logger.info("   💡 Strategy Insight: Following error paths and exception handling");
                break;
            case "configuration_chain":
                logger.info("   💡 Strategy Insight: Tracing configuration dependencies");
                break;
            case "similarity_search":
                logger.info("   💡 Strategy Insight: Broad similarity-based exploration");
                break;
        }
    }

    private boolean shouldUseSpecialHandling(QueryPlanner.SearchPlan plan) {
        return plan.isComplexQuery() || 
               plan.searchStrategy.equals("dependency_graph") ||
               plan.searchStrategy.equals("error_trace") ||
               !plan.isHighConfidence();
    }

    private String extractUserMessage(ChatClientRequest request) {
        try {
            if (request.prompt() != null && request.prompt().getInstructions() != null) {
                StringBuilder messageText = new StringBuilder();
                for (var message : request.prompt().getInstructions()) {
                    if (message instanceof UserMessage) {
                        UserMessage userMsg = (UserMessage) message;
                        messageText.append(userMsg.getText()).append(" ");
                    }
                }
                return messageText.toString().trim();
            }
            return "";
        } catch (Exception e) {
            logger.debug("Failed to extract user message: {}", e.getMessage());
            return "";
        }
    }
}
