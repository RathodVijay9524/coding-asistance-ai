package com.vijay.service;

import com.vijay.dto.WorkingMemoryState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🧠 Working Memory Manager
 * 
 * Maintains short-term memory for human-like thinking:
 * - Last 5 user messages
 * - Last 3 brain outputs
 * - Conversation intent history (last 10)
 * - Emotional tone history (last 10)
 * 
 * This is the "cursor system" that tracks what's currently in focus.
 */
@Service
public class WorkingMemoryManager {
    
    private static final Logger logger = LoggerFactory.getLogger(WorkingMemoryManager.class);
    
    private final Map<String, WorkingMemoryState> workingMemories = new ConcurrentHashMap<>();
    
    public WorkingMemoryManager() {
        logger.info("🧠 Working Memory Manager initialized - Short-term memory for human-like thinking");
    }
    
    /**
     * Get or create working memory for user
     */
    public WorkingMemoryState getWorkingMemory(String userId) {
        return workingMemories.computeIfAbsent(userId, k -> {
            logger.info("📝 Creating working memory for user: {}", userId);
            return new WorkingMemoryState(userId);
        });
    }
    
    /**
     * Record user message in working memory
     */
    public void recordUserMessage(String userId, String message) {
        logger.debug("📝 Recording user message - User: {}", userId);
        
        WorkingMemoryState memory = getWorkingMemory(userId);
        memory.addUserMessage(message);
        
        logger.debug("✅ User message recorded - {}", memory.getSummary());
    }
    
    /**
     * Record brain output in working memory
     */
    public void recordBrainOutput(String userId, String brainName, String output) {
        logger.debug("🧠 Recording brain output - Brain: {}, User: {}", brainName, userId);
        
        WorkingMemoryState memory = getWorkingMemory(userId);
        memory.addBrainOutput(brainName, output);
        
        logger.debug("✅ Brain output recorded - {}", memory.getSummary());
    }
    
    /**
     * Record conversation intent
     */
    public void recordIntent(String userId, String intent, double confidence) {
        logger.debug("🎯 Recording intent - Intent: {}, Confidence: {:.2f}", intent, confidence);
        
        WorkingMemoryState memory = getWorkingMemory(userId);
        memory.addIntent(intent, confidence);
        
        logger.debug("✅ Intent recorded - {}", memory.getSummary());
    }
    
    /**
     * Record emotional tone
     */
    public void recordTone(String userId, String tone, double intensity) {
        logger.debug("😊 Recording tone - Tone: {}, Intensity: {:.2f}", tone, intensity);
        
        WorkingMemoryState memory = getWorkingMemory(userId);
        memory.addTone(tone, intensity);
        
        logger.debug("✅ Tone recorded - {}", memory.getSummary());
    }
    
    /**
     * Get working memory summary
     */
    public String getWorkingMemorySummary(String userId) {
        WorkingMemoryState memory = getWorkingMemory(userId);
        return memory.getSummary();
    }
    
    /**
     * Get last user messages
     */
    public List<WorkingMemoryState.UserMessage> getLastUserMessages(String userId) {
        WorkingMemoryState memory = getWorkingMemory(userId);
        return memory.getLastUserMessages();
    }
    
    /**
     * Get last brain outputs
     */
    public List<WorkingMemoryState.BrainOutput> getLastBrainOutputs(String userId) {
        WorkingMemoryState memory = getWorkingMemory(userId);
        return memory.getLastBrainOutputs();
    }
    
    /**
     * Get intent history
     */
    public List<WorkingMemoryState.ConversationIntent> getIntentHistory(String userId) {
        WorkingMemoryState memory = getWorkingMemory(userId);
        return memory.getIntentHistory();
    }
    
    /**
     * Get tone history
     */
    public List<WorkingMemoryState.EmotionalTone> getToneHistory(String userId) {
        WorkingMemoryState memory = getWorkingMemory(userId);
        return memory.getToneHistory();
    }
    
    /**
     * Get most recent intent
     */
    public String getMostRecentIntent(String userId) {
        WorkingMemoryState memory = getWorkingMemory(userId);
        return memory.getMostRecentIntent();
    }
    
    /**
     * Get dominant tone
     */
    public String getDominantTone(String userId) {
        WorkingMemoryState memory = getWorkingMemory(userId);
        return memory.getDominantTone();
    }
    
    /**
     * Get detailed working memory state
     */
    public String getDetailedState(String userId) {
        WorkingMemoryState memory = getWorkingMemory(userId);
        
        StringBuilder state = new StringBuilder();
        state.append("🧠 WORKING MEMORY STATE:\n");
        state.append("========================\n");
        
        state.append("\n📝 Last 5 User Messages:\n");
        List<WorkingMemoryState.UserMessage> messages = memory.getLastUserMessages();
        for (int i = 0; i < messages.size(); i++) {
            state.append(String.format("  %d. %s\n", i + 1, messages.get(i).text));
        }
        
        state.append("\n🧠 Last 3 Brain Outputs:\n");
        List<WorkingMemoryState.BrainOutput> outputs = memory.getLastBrainOutputs();
        for (int i = 0; i < outputs.size(); i++) {
            state.append(String.format("  %d. [%s] %s\n", i + 1, outputs.get(i).brainName, outputs.get(i).output));
        }
        
        state.append("\n🎯 Intent History:\n");
        List<WorkingMemoryState.ConversationIntent> intents = memory.getIntentHistory();
        for (int i = 0; i < intents.size(); i++) {
            state.append(String.format("  %d. %s (%.2f)\n", i + 1, intents.get(i).intent, intents.get(i).confidence));
        }
        
        state.append("\n😊 Tone History:\n");
        List<WorkingMemoryState.EmotionalTone> tones = memory.getToneHistory();
        for (int i = 0; i < tones.size(); i++) {
            state.append(String.format("  %d. %s (%.2f)\n", i + 1, tones.get(i).tone, tones.get(i).intensity));
        }
        
        return state.toString();
    }
    
    /**
     * Clear working memory for user
     */
    public void clearWorkingMemory(String userId) {
        workingMemories.remove(userId);
        logger.info("🔄 Working memory cleared for user: {}", userId);
    }
}
