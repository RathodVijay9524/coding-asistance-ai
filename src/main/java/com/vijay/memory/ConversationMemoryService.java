package com.vijay.memory;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 💾 CONVERSATION MEMORY SERVICE
 * 
 * Stores and retrieves conversation context across messages.
 * Maintains user information, preferences, and message history.
 * Enables persistent memory across conversations.
 * 
 * ✅ CRITICAL FIX: Solves short-term memory loss issue
 */
@Service
@RequiredArgsConstructor
public class ConversationMemoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConversationMemoryService.class);
    
    // Store conversation contexts by conversation ID
    private final Map<String, ConversationContext> conversationMap = new ConcurrentHashMap<>();
    
    // Store user profiles by user ID
    private final Map<String, UserProfile> userProfiles = new ConcurrentHashMap<>();
    
    /**
     * Get or create conversation context
     */
    public ConversationContext getOrCreateContext(String conversationId, String userId) {
        logger.info("💾 Getting or creating context for conversation: {}", conversationId);
        
        return conversationMap.computeIfAbsent(conversationId, id -> {
            logger.info("✅ Created new conversation context: {}", id);
            return new ConversationContext(id, userId);
        });
    }
    
    /**
     * Store user information in conversation context
     */
    public void storeUserInfo(String conversationId, String key, Object value) {
        logger.info("💾 Storing user info: {} = {}", key, value);
        
        ConversationContext context = conversationMap.get(conversationId);
        if (context != null) {
            context.getUserInfo().put(key, value);
            context.setLastAccessed(Instant.now());
            logger.info("✅ User info stored successfully");
        } else {
            logger.warn("⚠️ Conversation context not found: {}", conversationId);
        }
    }
    
    /**
     * Retrieve user information from conversation context
     */
    public Object getUserInfo(String conversationId, String key) {
        ConversationContext context = conversationMap.get(conversationId);
        if (context != null) {
            Object value = context.getUserInfo().get(key);
            logger.info("💾 Retrieved user info: {} = {}", key, value);
            return value;
        }
        logger.warn("⚠️ Conversation context not found: {}", conversationId);
        return null;
    }
    
    /**
     * Add message to conversation history
     */
    public void addMessage(String conversationId, ChatMessage message) {
        logger.info("💾 Adding message to conversation: {}", conversationId);
        
        ConversationContext context = conversationMap.get(conversationId);
        if (context != null) {
            context.getMessageHistory().add(message);
            context.setLastAccessed(Instant.now());
            logger.info("✅ Message added to history");
        } else {
            logger.warn("⚠️ Conversation context not found: {}", conversationId);
        }
    }
    
    /**
     * Get conversation history
     */
    public List<ChatMessage> getConversationHistory(String conversationId) {
        logger.info("💾 Retrieving conversation history: {}", conversationId);
        
        ConversationContext context = conversationMap.get(conversationId);
        if (context != null) {
            logger.info("✅ Retrieved {} messages", context.getMessageHistory().size());
            return new ArrayList<>(context.getMessageHistory());
        }
        logger.warn("⚠️ Conversation context not found: {}", conversationId);
        return new ArrayList<>();
    }
    
    /**
     * Get full conversation context
     */
    public ConversationContext getContext(String conversationId) {
        logger.info("💾 Retrieving full context: {}", conversationId);
        return conversationMap.get(conversationId);
    }
    
    /**
     * Store user profile
     */
    public void storeUserProfile(String userId, UserProfile profile) {
        logger.info("💾 Storing user profile for: {}", userId);
        userProfiles.put(userId, profile);
        logger.info("✅ User profile stored");
    }
    
    /**
     * Get user profile
     */
    public UserProfile getUserProfile(String userId) {
        logger.info("💾 Retrieving user profile for: {}", userId);
        return userProfiles.getOrDefault(userId, new UserProfile(userId));
    }
    
    /**
     * Clear conversation (cleanup)
     */
    public void clearConversation(String conversationId) {
        logger.info("🧹 Clearing conversation: {}", conversationId);
        conversationMap.remove(conversationId);
        logger.info("✅ Conversation cleared");
    }
    
    /**
     * Get all conversations for user
     */
    public List<ConversationContext> getUserConversations(String userId) {
        logger.info("💾 Retrieving all conversations for user: {}", userId);
        
        List<ConversationContext> userConversations = new ArrayList<>();
        for (ConversationContext context : conversationMap.values()) {
            if (userId.equals(context.getUserId())) {
                userConversations.add(context);
            }
        }
        
        logger.info("✅ Retrieved {} conversations", userConversations.size());
        return userConversations;
    }
    
    /**
     * Extract and remember user name from message
     */
    public String extractAndRememberName(String conversationId, String message) {
        logger.info("🔍 Extracting name from message");
        
        String namePattern = "(?i).*my name is\\s+([a-zA-Z]+).*";
        if (message.matches(namePattern)) {
            String name = message.replaceAll("(?i).*my name is\\s+", "")
                                .replaceAll("[^a-zA-Z].*", "")
                                .trim();
            
            if (!name.isEmpty()) {
                storeUserInfo(conversationId, "name", name);
                logger.info("✅ Remembered user name: {}", name);
                return name;
            }
        }
        
        return null;
    }
    
    /**
     * Get remembered user name
     */
    public String getRememberedName(String conversationId) {
        Object name = getUserInfo(conversationId, "name");
        if (name != null) {
            logger.info("👤 Using remembered name: {}", name);
            return (String) name;
        }
        return null;
    }
    
    /**
     * Store conversation metadata
     */
    public void storeMetadata(String conversationId, String key, Object value) {
        logger.info("💾 Storing metadata: {} = {}", key, value);
        
        ConversationContext context = conversationMap.get(conversationId);
        if (context != null) {
            context.getMetadata().put(key, value);
            logger.info("✅ Metadata stored");
        }
    }
    
    /**
     * Get conversation metadata
     */
    public Object getMetadata(String conversationId, String key) {
        ConversationContext context = conversationMap.get(conversationId);
        if (context != null) {
            return context.getMetadata().get(key);
        }
        return null;
    }
    
    // Inner classes
    
    public static class ConversationContext {
        private String conversationId;
        private String userId;
        private Map<String, Object> userInfo;
        private List<ChatMessage> messageHistory;
        private Map<String, Object> metadata;
        private Instant createdAt;
        private Instant lastAccessed;
        
        public ConversationContext(String conversationId, String userId) {
            this.conversationId = conversationId;
            this.userId = userId;
            this.userInfo = new ConcurrentHashMap<>();
            this.messageHistory = Collections.synchronizedList(new ArrayList<>());
            this.metadata = new ConcurrentHashMap<>();
            this.createdAt = Instant.now();
            this.lastAccessed = Instant.now();
        }
        
        // Getters and setters
        public String getConversationId() { return conversationId; }
        public String getUserId() { return userId; }
        public Map<String, Object> getUserInfo() { return userInfo; }
        public List<ChatMessage> getMessageHistory() { return messageHistory; }
        public Map<String, Object> getMetadata() { return metadata; }
        public Instant getCreatedAt() { return createdAt; }
        public Instant getLastAccessed() { return lastAccessed; }
        public void setLastAccessed(Instant lastAccessed) { this.lastAccessed = lastAccessed; }
    }
    
    public static class ChatMessage {
        private String role; // "user" or "assistant"
        private String content;
        private Instant timestamp;
        
        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
            this.timestamp = Instant.now();
        }
        
        // Getters
        public String getRole() { return role; }
        public String getContent() { return content; }
        public Instant getTimestamp() { return timestamp; }
    }
    
    public static class UserProfile {
        private String userId;
        private String name;
        private String email;
        private Map<String, Object> preferences;
        private List<String> conversationIds;
        private Instant createdAt;
        private Instant lastUpdated;
        
        public UserProfile(String userId) {
            this.userId = userId;
            this.preferences = new ConcurrentHashMap<>();
            this.conversationIds = Collections.synchronizedList(new ArrayList<>());
            this.createdAt = Instant.now();
            this.lastUpdated = Instant.now();
        }
        
        // Getters and setters
        public String getUserId() { return userId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public Map<String, Object> getPreferences() { return preferences; }
        public List<String> getConversationIds() { return conversationIds; }
        public Instant getCreatedAt() { return createdAt; }
        public Instant getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }
    }
}
