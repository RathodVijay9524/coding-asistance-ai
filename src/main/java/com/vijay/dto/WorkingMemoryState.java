package com.vijay.dto;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 🧠 Working Memory State
 * 
 * Maintains short-term memory for human-like thinking:
 * - Last 5 user messages
 * - Last 3 brain outputs
 * - Conversation intent history
 * - Emotional tone history
 */
public class WorkingMemoryState {
    
    private String userId;
    private LinkedList<UserMessage> lastUserMessages;      // Last 5
    private LinkedList<BrainOutput> lastBrainOutputs;      // Last 3
    private LinkedList<ConversationIntent> intentHistory;  // Last 10
    private LinkedList<EmotionalTone> toneHistory;         // Last 10
    private LocalDateTime lastUpdated;
    
    private static final int MAX_USER_MESSAGES = 5;
    private static final int MAX_BRAIN_OUTPUTS = 3;
    private static final int MAX_INTENT_HISTORY = 10;
    private static final int MAX_TONE_HISTORY = 10;
    
    public WorkingMemoryState(String userId) {
        this.userId = userId;
        this.lastUserMessages = new LinkedList<>();
        this.lastBrainOutputs = new LinkedList<>();
        this.intentHistory = new LinkedList<>();
        this.toneHistory = new LinkedList<>();
        this.lastUpdated = LocalDateTime.now();
    }
    
    // User Messages
    public void addUserMessage(String message) {
        lastUserMessages.addLast(new UserMessage(message, LocalDateTime.now()));
        if (lastUserMessages.size() > MAX_USER_MESSAGES) {
            lastUserMessages.removeFirst();
        }
        this.lastUpdated = LocalDateTime.now();
    }
    
    public List<UserMessage> getLastUserMessages() {
        return new ArrayList<>(lastUserMessages);
    }
    
    // Brain Outputs
    public void addBrainOutput(String brainName, String output) {
        lastBrainOutputs.addLast(new BrainOutput(brainName, output, LocalDateTime.now()));
        if (lastBrainOutputs.size() > MAX_BRAIN_OUTPUTS) {
            lastBrainOutputs.removeFirst();
        }
        this.lastUpdated = LocalDateTime.now();
    }
    
    public List<BrainOutput> getLastBrainOutputs() {
        return new ArrayList<>(lastBrainOutputs);
    }
    
    // Intent History
    public void addIntent(String intent, double confidence) {
        intentHistory.addLast(new ConversationIntent(intent, confidence, LocalDateTime.now()));
        if (intentHistory.size() > MAX_INTENT_HISTORY) {
            intentHistory.removeFirst();
        }
        this.lastUpdated = LocalDateTime.now();
    }
    
    public List<ConversationIntent> getIntentHistory() {
        return new ArrayList<>(intentHistory);
    }
    
    public String getMostRecentIntent() {
        if (intentHistory.isEmpty()) return "unknown";
        return intentHistory.getLast().intent;
    }
    
    // Tone History
    public void addTone(String tone, double intensity) {
        toneHistory.addLast(new EmotionalTone(tone, intensity, LocalDateTime.now()));
        if (toneHistory.size() > MAX_TONE_HISTORY) {
            toneHistory.removeFirst();
        }
        this.lastUpdated = LocalDateTime.now();
    }
    
    public List<EmotionalTone> getToneHistory() {
        return new ArrayList<>(toneHistory);
    }
    
    public String getDominantTone() {
        if (toneHistory.isEmpty()) return "neutral";
        
        Map<String, Integer> toneCounts = new HashMap<>();
        for (EmotionalTone tone : toneHistory) {
            toneCounts.put(tone.tone, toneCounts.getOrDefault(tone.tone, 0) + 1);
        }
        
        return toneCounts.entrySet().stream()
            .max(Comparator.comparingInt(Map.Entry::getValue))
            .map(Map.Entry::getKey)
            .orElse("neutral");
    }
    
    public String getSummary() {
        return String.format(
            "Working Memory: %d messages | %d outputs | Intent: %s | Tone: %s",
            lastUserMessages.size(), lastBrainOutputs.size(), 
            getMostRecentIntent(), getDominantTone()
        );
    }
    
    // Inner classes
    public static class UserMessage {
        public String text;
        public LocalDateTime timestamp;
        
        public UserMessage(String text, LocalDateTime timestamp) {
            this.text = text;
            this.timestamp = timestamp;
        }
    }
    
    public static class BrainOutput {
        public String brainName;
        public String output;
        public LocalDateTime timestamp;
        
        public BrainOutput(String brainName, String output, LocalDateTime timestamp) {
            this.brainName = brainName;
            this.output = output;
            this.timestamp = timestamp;
        }
    }
    
    public static class ConversationIntent {
        public String intent;
        public double confidence;
        public LocalDateTime timestamp;
        
        public ConversationIntent(String intent, double confidence, LocalDateTime timestamp) {
            this.intent = intent;
            this.confidence = confidence;
            this.timestamp = timestamp;
        }
    }
    
    public static class EmotionalTone {
        public String tone;
        public double intensity;
        public LocalDateTime timestamp;
        
        public EmotionalTone(String tone, double intensity, LocalDateTime timestamp) {
            this.tone = tone;
            this.intensity = intensity;
            this.timestamp = timestamp;
        }
    }
    
    // Getters
    public String getUserId() {
        return userId;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
}
