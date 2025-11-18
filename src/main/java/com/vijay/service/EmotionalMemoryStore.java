package com.vijay.service;

import com.vijay.dto.EmotionalContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🧠 Emotional Memory Store
 * 
 * Stores and tracks emotional patterns over time for each user.
 * Used to understand emotional trends and adapt responses accordingly.
 */
@Service
public class EmotionalMemoryStore {
    
    private static final Logger logger = LoggerFactory.getLogger(EmotionalMemoryStore.class);
    
    private final Map<String, List<EmotionalContext>> userEmotionalHistory = new ConcurrentHashMap<>();
    private static final int MAX_EMOTIONAL_MEMORIES = 50;
    
    /**
     * Store emotional context for a user
     */
    public void storeEmotionalContext(String userId, EmotionalContext context) {
        logger.debug("💾 Storing emotional context for user: {}", userId);
        
        List<EmotionalContext> history = userEmotionalHistory.computeIfAbsent(userId, k -> new ArrayList<>());
        history.add(context);
        
        // Keep only recent memories
        if (history.size() > MAX_EMOTIONAL_MEMORIES) {
            history.remove(0);
        }
        
        logger.debug("📊 User {} emotional history size: {}", userId, history.size());
    }
    
    /**
     * Get emotional history for a user
     */
    public List<EmotionalContext> getEmotionalHistory(String userId) {
        return userEmotionalHistory.getOrDefault(userId, new ArrayList<>());
    }
    
    /**
     * Get average emotional state over recent interactions
     */
    public String getAverageEmotionalTrend(String userId) {
        List<EmotionalContext> history = getEmotionalHistory(userId);
        
        if (history.isEmpty()) {
            return "NEUTRAL";
        }
        
        // Get last 10 interactions
        int count = Math.min(10, history.size());
        List<EmotionalContext> recent = history.subList(history.size() - count, history.size());
        
        // Count emotional states
        Map<String, Integer> stateCounts = new HashMap<>();
        for (EmotionalContext context : recent) {
            String state = context.getCurrentState().toString();
            stateCounts.put(state, stateCounts.getOrDefault(state, 0) + 1);
        }
        
        // Find most common state
        return stateCounts.entrySet().stream()
            .max(Comparator.comparingInt(Map.Entry::getValue))
            .map(Map.Entry::getKey)
            .orElse("NEUTRAL");
    }
    
    /**
     * Get average emotional intensity
     */
    public int getAverageEmotionalIntensity(String userId) {
        List<EmotionalContext> history = getEmotionalHistory(userId);
        
        if (history.isEmpty()) {
            return 0;
        }
        
        int count = Math.min(10, history.size());
        List<EmotionalContext> recent = history.subList(history.size() - count, history.size());
        
        int totalIntensity = recent.stream()
            .mapToInt(EmotionalContext::getEmotionalIntensity)
            .sum();
        
        return totalIntensity / count;
    }
    
    /**
     * Check if user is experiencing emotional fatigue (consistently high intensity)
     */
    public boolean isUserEmotionallyFatigued(String userId) {
        List<EmotionalContext> history = getEmotionalHistory(userId);
        
        if (history.size() < 5) {
            return false;
        }
        
        int count = Math.min(5, history.size());
        List<EmotionalContext> recent = history.subList(history.size() - count, history.size());
        
        // If average intensity > 70 for last 5 interactions, user is fatigued
        double avgIntensity = recent.stream()
            .mapToInt(EmotionalContext::getEmotionalIntensity)
            .average()
            .orElse(0);
        
        return avgIntensity > 70;
    }
    
    /**
     * Clear emotional history for a user
     */
    public void clearEmotionalHistory(String userId) {
        userEmotionalHistory.remove(userId);
        logger.debug("🗑️ Cleared emotional history for user: {}", userId);
    }
}
