package com.vijay.manager;

import com.vijay.context.GlobalBrainContext;
import com.vijay.context.TraceContext;
import com.vijay.dto.AgentPlan;
import com.vijay.dto.ReasoningState;
import com.vijay.util.AgentPlanHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 🔧 Brain 2: Tool Call Advisor (Plan-Aware Tool Executor)
 * 
 * Purpose: Execute tools based on the master plan from Brain 0
 * 
 * Responsibilities:
 * - Read the master plan from ConductorAdvisor (Brain 0)
 * - Check if tools are required (plan.requiredTools)
 * - Only execute tools if the plan says they're needed
 * - Prevent HTTP 400 crashes from tool execution errors
 * - Log tool execution details
 * 
 * Execution Order: 2 (AFTER Brain 1 - DynamicContextAdvisor)
 * 
 * Key Feature: Plan-Aware
 * - Only runs if plan.requiredTools is not empty
 * - Prevents unnecessary tool execution
 * - Reduces errors and improves performance
 */
@Component
public class ToolCallAdvisor implements CallAdvisor, IAgentBrain {
    
    private static final Logger logger = LoggerFactory.getLogger(ToolCallAdvisor.class);
    
    @Override
    public String getName() {
        return "ToolCallAdvisor";
    }
    
    // ===== IAgentBrain Implementation =====
    @Override
    public String getBrainName() {
        return "toolCallAdvisor";  // ← Spring bean name (lowercase first letter)
    }
    
    @Override
    public String getBrainDescription() {
        return "Plan-aware tool executor. Reads the master plan and only executes tools if they are required. Prevents HTTP 400 errors and improves performance.";
    }
    
    @Override
    public int getOrder() {
        return 20;  // Execute AFTER Brain 1 (DynamicContextAdvisor at order 10)
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        String traceId = TraceContext.getTraceId();
        logger.info("[{}] 🔧 Brain 2 (Tool Call): Enforcing tool approval policy...", traceId);
        
        try {
            // STEP 1: Read the master plan from Brain 0 (ConductorAdvisor)
            AgentPlan masterPlan = AgentPlanHolder.getPlan();
            
            if (masterPlan == null) {
                logger.warn("[{}]    ⚠️ No master plan found - allowing all tools (fallback mode)", traceId);
                return chain.nextCall(request);
            }
            
            // STEP 2: Get approved tools from plan
            List<String> approvedTools = masterPlan.getRequiredTools();
            logger.info("[{}]    ✅ Approved tools from Conductor: {}", traceId, approvedTools);
            
            // STEP 3: Get suggested tools from ReasoningState
            ReasoningState state = GlobalBrainContext.getReasoningState();
            List<String> suggestedTools = (state != null && state.getSuggestedTools() != null) 
                ? state.getSuggestedTools() 
                : List.of();
            
            // STEP 4: Calculate rejected tools
            List<String> rejectedTools = suggestedTools.stream()
                    .filter(tool -> !approvedTools.contains(tool))
                    .collect(Collectors.toList());
            
            if (!rejectedTools.isEmpty()) {
                logger.warn("[{}]    ⛔ Tools REJECTED by Conductor (will be blocked):", traceId);
                for (String tool : rejectedTools) {
                    logger.warn("[{}]       ❌ {}", traceId, tool);
                }
            }
            
            // STEP 5: Build enforcement rules and inject into prompt
            String enforcementRules = buildEnforcementRules(approvedTools, rejectedTools);
            ChatClientRequest modifiedRequest = addEnforcementRulesToRequest(request, enforcementRules);
            
            logger.info("[{}]    ✅ Tool enforcement rules injected into system prompt", traceId);
            logger.info("[{}]    📋 Approved tools: {}", traceId, approvedTools.size());
            logger.info("[{}]    ⛔ Rejected tools: {}", traceId, rejectedTools.size());
            
            // STEP 6: Continue to next advisor with modified request
            ChatClientResponse response = chain.nextCall(modifiedRequest);
            
            logger.info("[{}] ✅ Brain 2: Tool enforcement complete", traceId);
            return response;
            
        } catch (Exception e) {
            logger.error("[{}] ❌ Brain 2: Error in tool enforcement - {}", traceId, e.getMessage(), e);
            // Continue chain even if tool enforcement fails
            return chain.nextCall(request);
        }
    }
    
    /**
     * Build enforcement rules for the system prompt
     */
    private String buildEnforcementRules(List<String> approvedTools, List<String> rejectedTools) {
        StringBuilder rules = new StringBuilder();
        rules.append("\n[TOOL EXECUTION POLICY - ENFORCED BY CONDUCTOR]\n");
        
        if (approvedTools.isEmpty()) {
            rules.append("⚠️ NO TOOLS ARE APPROVED FOR THIS QUERY\n");
            rules.append("You MUST NOT attempt to use any tools.\n");
            rules.append("Answer using your knowledge only.\n");
        } else {
            rules.append("✅ APPROVED TOOLS (you may use these):\n");
            for (String tool : approvedTools) {
                rules.append("   - ").append(tool).append("\n");
            }
            
            if (!rejectedTools.isEmpty()) {
                rules.append("\n⛔ REJECTED TOOLS (you MUST NOT use these):\n");
                for (String tool : rejectedTools) {
                    rules.append("   - ").append(tool).append("\n");
                }
                rules.append("\nAttempting to use rejected tools will cause errors.\n");
            }
        }
        
        rules.append("[END TOOL POLICY]\n");
        return rules.toString();
    }
    
    /**
     * Add enforcement rules to the request's system prompt
     */
    private ChatClientRequest addEnforcementRulesToRequest(ChatClientRequest originalRequest, String enforcementRules) {
        try {
            // Get the original prompt
            Prompt originalPrompt = originalRequest.prompt();
            
            if (originalPrompt == null) {
                logger.warn("Original prompt is null, cannot inject enforcement rules");
                return originalRequest;
            }
            
            // Create new message list with enforcement rules at the beginning
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(enforcementRules));
            
            // Add original messages
            if (originalPrompt.getInstructions() != null) {
                messages.addAll(originalPrompt.getInstructions());
            }
            
            // Create new prompt with enforcement rules
            Prompt modifiedPrompt = new Prompt(messages, originalPrompt.getOptions());
            
            // Create modified request using builder pattern
            return ChatClientRequest.builder()
                    .prompt(modifiedPrompt)
                    .build();
            
        } catch (Exception e) {
            logger.warn("Unable to inject enforcement rules into request: {}", e.getMessage());
            logger.debug("Returning original request without modifications", e);
            return originalRequest;
        }
    }
}
