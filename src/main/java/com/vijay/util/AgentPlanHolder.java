package com.vijay.util;

import com.vijay.dto.AgentPlan;

/**
 * 📌 AgentPlanHolder - Thread-Local Storage for Master Plan
 * 
 * Since ChatClientRequest doesn't have a context map, we use thread-local storage
 * to pass the master AgentPlan from ConductorAdvisor to downstream brains.
 * 
 * Usage:
 * - ConductorAdvisor (Brain 0): AgentPlanHolder.setPlan(masterPlan)
 * - DynamicContextAdvisor (Brain 1): AgentPlan plan = AgentPlanHolder.getPlan()
 * - ToolCallAdvisor (Brain 2): AgentPlan plan = AgentPlanHolder.getPlan()
 * - SelfRefineV3Advisor (Brain 13): AgentPlan plan = AgentPlanHolder.getPlan()
 * - PersonalityAdvisor (Brain 14): AgentPlan plan = AgentPlanHolder.getPlan()
 */
public class AgentPlanHolder {
    
    private static final ThreadLocal<AgentPlan> planHolder = new ThreadLocal<>();
    
    /**
     * Store the master plan in thread-local storage
     */
    public static void setPlan(AgentPlan plan) {
        planHolder.set(plan);
    }
    
    /**
     * Retrieve the master plan from thread-local storage
     */
    public static AgentPlan getPlan() {
        return planHolder.get();
    }
    
    /**
     * Check if a plan is available
     */
    public static boolean hasPlan() {
        return planHolder.get() != null;
    }
    
    /**
     * Clear the plan from thread-local storage
     * (Should be called after request is processed)
     */
    public static void clear() {
        planHolder.remove();
    }
}
