package com.vijay.context;

import com.vijay.dto.ReasoningState;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 🧠 GlobalBrainContext - Shared Context for All Brains
 * 
 * This is a ThreadLocal context that allows all brains to share information
 * without passing it through method parameters.
 * 
 * Usage:
 * - SmartFinder: GlobalBrainContext.setReasoningState(state)
 * - ConductorAdvisor: ReasoningState state = GlobalBrainContext.getReasoningState()
 * - ToolCallAdvisor: state.isToolApproved(toolName)
 * - All brains: GlobalBrainContext.put("key", value)
 * 
 * Benefits:
 * ✅ No parameter passing
 * ✅ Shared context across brains
 * ✅ Easy to add new context
 * ✅ ThreadLocal (safe for concurrent requests)
 */
@Component
public class GlobalBrainContext {
    
    private static final ThreadLocal<ReasoningState> reasoningState = 
        ThreadLocal.withInitial(() -> null);
    
    private static final ThreadLocal<Map<String, Object>> context = 
        ThreadLocal.withInitial(HashMap::new);
    
    /**
     * Set the reasoning state (called by SmartFinder)
     */
    public static void setReasoningState(ReasoningState state) {
        reasoningState.set(state);
    }
    
    /**
     * Get the reasoning state (called by all brains)
     */
    public static ReasoningState getReasoningState() {
        return reasoningState.get();
    }
    
    /**
     * Put a value in the context
     */
    public static void put(String key, Object value) {
        context.get().put(key, value);
    }
    
    /**
     * Get a value from the context
     */
    public static Object get(String key) {
        return context.get().get(key);
    }
    
    /**
     * Get all context
     */
    public static Map<String, Object> getAll() {
        return new HashMap<>(context.get());
    }
    
    /**
     * Check if key exists
     */
    public static boolean containsKey(String key) {
        return context.get().containsKey(key);
    }
    
    /**
     * Clear the context (call at end of request)
     */
    public static void clear() {
        reasoningState.remove();
        context.remove();
    }
    
    /**
     * Get context size
     */
    public static int size() {
        return context.get().size();
    }
    
    /**
     * String representation for debugging
     */
    public static String debug() {
        ReasoningState state = reasoningState.get();
        Map<String, Object> ctx = context.get();
        
        return String.format(
            "GlobalBrainContext{reasoningState=%s, contextSize=%d, context=%s}",
            state,
            ctx.size(),
            ctx
        );
    }
}
