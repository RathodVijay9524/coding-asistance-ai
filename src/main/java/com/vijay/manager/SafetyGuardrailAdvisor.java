package com.vijay.manager;

import com.vijay.context.GlobalBrainContext;
import com.vijay.context.TraceContext;
import com.vijay.dto.ReasoningState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 🛡️ Safety Guardrail Advisor - Prevents Dangerous Tool Execution
 * 
 * Purpose: Safety checks before tool execution
 * 
 * Responsibilities:
 * - Identify dangerous tools
 * - Check if user has permission
 * - Block dangerous tools without approval
 * - Log security events
 * - Enforce safety policies
 * 
 * Execution Order: 1.5 (AFTER Brain 1, BEFORE Brain 2)
 * 
 * Dangerous Tools:
 * - deleteFile, deleteDirectory
 * - executeCommand, runScript
 * - modifyDatabase, dropTable
 * - sendEmail, sendSMS
 * - deployCode, restartServer
 */
@Component
public class SafetyGuardrailAdvisor implements CallAdvisor, IAgentBrain {
    
    private static final Logger logger = LoggerFactory.getLogger(SafetyGuardrailAdvisor.class);
    
    // Dangerous tools that require explicit approval
    private static final Set<String> DANGEROUS_TOOLS = new HashSet<>();
    
    static {
        // File operations
        DANGEROUS_TOOLS.add("deleteFile");
        DANGEROUS_TOOLS.add("deleteDirectory");
        DANGEROUS_TOOLS.add("modifyFile");
        
        // System operations
        DANGEROUS_TOOLS.add("executeCommand");
        DANGEROUS_TOOLS.add("runScript");
        DANGEROUS_TOOLS.add("restartServer");
        DANGEROUS_TOOLS.add("shutdownServer");
        
        // Database operations
        DANGEROUS_TOOLS.add("modifyDatabase");
        DANGEROUS_TOOLS.add("dropTable");
        DANGEROUS_TOOLS.add("deleteRecord");
        DANGEROUS_TOOLS.add("truncateTable");
        
        // Communication
        DANGEROUS_TOOLS.add("sendEmail");
        DANGEROUS_TOOLS.add("sendSMS");
        DANGEROUS_TOOLS.add("sendNotification");
        
        // Deployment
        DANGEROUS_TOOLS.add("deployCode");
        DANGEROUS_TOOLS.add("releaseVersion");
        DANGEROUS_TOOLS.add("rollbackVersion");
    }
    
    @Override
    public String getName() {
        return "SafetyGuardrailAdvisor";
    }
    
    @Override
    public String getBrainName() {
        return "safetyGuardrailAdvisor";
    }
    
    @Override
    public String getBrainDescription() {
        return "Safety guardrail that prevents dangerous tool execution without explicit approval. Checks permissions and enforces safety policies.";
    }
    
    @Override
    public int getOrder() {
        return 1;  // Run AFTER Brain 0 (Conductor), BEFORE Brain 2 (ToolCall)
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        String traceId = TraceContext.getTraceId();
        logger.info("[{}] 🛡️ Safety Guardrail: Checking for dangerous tools...", traceId);
        
        try {
            // STEP 1: Get ReasoningState
            ReasoningState state = GlobalBrainContext.getReasoningState();
            
            if (state == null || !state.hasApprovedTools()) {
                logger.info("[{}]    ℹ️ No approved tools to check", traceId);
                return chain.nextCall(request);
            }
            
            // STEP 2: Check each approved tool
            boolean hasDangerousTools = false;
            for (String tool : state.getApprovedTools()) {
                if (isDangerousTool(tool)) {
                    hasDangerousTools = true;
                    logger.warn("[{}]    ⚠️ DANGEROUS TOOL DETECTED: {}", traceId, tool);
                    
                    // Check if user has permission
                    if (!hasUserApproval(tool)) {
                        logger.error("[{}]    ❌ BLOCKED: {} requires explicit user approval", traceId, tool);
                        // Remove from approved tools
                        state.getApprovedTools().remove(tool);
                    } else {
                        logger.info("[{}]    ✅ APPROVED: {} has user permission", traceId, tool);
                    }
                }
            }
            
            // STEP 3: Log summary
            if (hasDangerousTools) {
                logger.warn("[{}]    📊 Dangerous tools found. Remaining approved: {}", 
                    traceId, state.getApprovedTools());
            } else {
                logger.info("[{}]    ✅ No dangerous tools detected", traceId);
            }
            
            // STEP 4: Continue to next advisor
            logger.info("[{}] 🛡️ Safety check complete", traceId);
            return chain.nextCall(request);
            
        } catch (Exception e) {
            logger.error("[{}] ❌ Safety Guardrail: Error - {}", traceId, e.getMessage());
            // Continue chain even if safety check fails
            return chain.nextCall(request);
        }
    }
    
    /**
     * Check if a tool is dangerous
     */
    private boolean isDangerousTool(String toolName) {
        return DANGEROUS_TOOLS.contains(toolName);
    }
    
    /**
     * Check if user has explicit approval for dangerous tool
     * 
     * In production, this would check:
     * - User role/permissions
     * - Approval workflow
     * - Audit logs
     * - Time-based restrictions
     */
    private boolean hasUserApproval(String toolName) {
        // For now, block all dangerous tools without explicit approval
        // In production, implement proper permission checking
        return false;
    }
    
    /**
     * Get list of dangerous tools
     */
    public Set<String> getDangerousTools() {
        return new HashSet<>(DANGEROUS_TOOLS);
    }
    
    /**
     * Check if a tool is dangerous
     */
    public boolean checkToolSafety(String toolName) {
        return !isDangerousTool(toolName);
    }
}
