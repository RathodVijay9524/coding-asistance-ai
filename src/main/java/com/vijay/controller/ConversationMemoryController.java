package com.vijay.controller;

import com.vijay.memory.ConversationMemoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 💾 CONVERSATION MEMORY CONTROLLER
 * 
 * REST endpoints for conversation memory management.
 * Enables retrieval and storage of conversation context.
 * 
 * ✅ CRITICAL FIX: Solves short-term memory loss issue
 */
@RestController
@RequestMapping("/api/memory")
@RequiredArgsConstructor
public class ConversationMemoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(ConversationMemoryController.class);
    private final ConversationMemoryService memoryService;
    
    /**
     * Get conversation history
     */
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<?> getConversationHistory(
            @PathVariable String conversationId) {
        
        logger.info("📖 Getting conversation history: {}", conversationId);
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            ConversationMemoryService.ConversationContext context = 
                memoryService.getContext(conversationId);
            
            if (context == null) {
                result.put("status", "error");
                result.put("message", "Conversation not found");
                return ResponseEntity.ok(result);
            }
            
            result.put("status", "success");
            result.put("conversationId", conversationId);
            result.put("userId", context.getUserId());
            result.put("messageCount", context.getMessageHistory().size());
            result.put("messages", context.getMessageHistory());
            result.put("userInfo", context.getUserInfo());
            result.put("createdAt", context.getCreatedAt());
            result.put("lastAccessed", context.getLastAccessed());
            
            logger.info("✅ Retrieved conversation history");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Error retrieving conversation: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.ok(error);
        }
    }
    
    /**
     * Store user information
     */
    @PostMapping("/user-info")
    public ResponseEntity<?> storeUserInfo(
            @RequestParam String conversationId,
            @RequestParam String key,
            @RequestBody Object value) {
        
        logger.info("💾 Storing user info: {} = {}", key, value);
        
        try {
            memoryService.storeUserInfo(conversationId, key, value);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "User info stored");
            result.put("key", key);
            result.put("value", value);
            
            logger.info("✅ User info stored successfully");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Error storing user info: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.ok(error);
        }
    }
    
    /**
     * Get user information
     */
    @GetMapping("/user-info")
    public ResponseEntity<?> getUserInfo(
            @RequestParam String conversationId,
            @RequestParam String key) {
        
        logger.info("💾 Retrieving user info: {}", key);
        
        try {
            Object value = memoryService.getUserInfo(conversationId, key);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("key", key);
            result.put("value", value);
            
            logger.info("✅ User info retrieved");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Error retrieving user info: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.ok(error);
        }
    }
    
    /**
     * Get user profile
     */
    @GetMapping("/user-profile/{userId}")
    public ResponseEntity<?> getUserProfile(
            @PathVariable String userId) {
        
        logger.info("👤 Retrieving user profile: {}", userId);
        
        try {
            ConversationMemoryService.UserProfile profile = 
                memoryService.getUserProfile(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("userId", profile.getUserId());
            result.put("name", profile.getName());
            result.put("email", profile.getEmail());
            result.put("preferences", profile.getPreferences());
            result.put("conversationCount", profile.getConversationIds().size());
            result.put("createdAt", profile.getCreatedAt());
            result.put("lastUpdated", profile.getLastUpdated());
            
            logger.info("✅ User profile retrieved");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Error retrieving user profile: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.ok(error);
        }
    }
    
    /**
     * Get all conversations for user
     */
    @GetMapping("/conversations/{userId}")
    public ResponseEntity<?> getUserConversations(
            @PathVariable String userId) {
        
        logger.info("📖 Retrieving all conversations for user: {}", userId);
        
        try {
            List<ConversationMemoryService.ConversationContext> conversations = 
                memoryService.getUserConversations(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("userId", userId);
            result.put("conversationCount", conversations.size());
            result.put("conversations", conversations);
            
            logger.info("✅ Retrieved {} conversations", conversations.size());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Error retrieving conversations: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.ok(error);
        }
    }
    
    /**
     * Extract and remember user name
     */
    @PostMapping("/remember-name")
    public ResponseEntity<?> rememberName(
            @RequestParam String conversationId,
            @RequestBody String message) {
        
        logger.info("🔍 Extracting name from message");
        
        try {
            String name = memoryService.extractAndRememberName(conversationId, message);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "Name extraction processed");
            result.put("extractedName", name);
            
            if (name != null) {
                logger.info("✅ Name remembered: {}", name);
            } else {
                logger.info("ℹ️ No name found in message");
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Error extracting name: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.ok(error);
        }
    }
    
    /**
     * Get remembered user name
     */
    @GetMapping("/remembered-name/{conversationId}")
    public ResponseEntity<?> getRememberedName(
            @PathVariable String conversationId) {
        
        logger.info("👤 Retrieving remembered name");
        
        try {
            String name = memoryService.getRememberedName(conversationId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("name", name);
            
            logger.info("✅ Retrieved remembered name");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Error retrieving name: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.ok(error);
        }
    }
    
    /**
     * Store metadata
     */
    @PostMapping("/metadata")
    public ResponseEntity<?> storeMetadata(
            @RequestParam String conversationId,
            @RequestParam String key,
            @RequestBody Object value) {
        
        logger.info("💾 Storing metadata: {} = {}", key, value);
        
        try {
            memoryService.storeMetadata(conversationId, key, value);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "Metadata stored");
            result.put("key", key);
            
            logger.info("✅ Metadata stored");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ Error storing metadata: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.ok(error);
        }
    }
    
    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        logger.info("🏥 Memory service health check");
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "healthy");
        result.put("service", "ConversationMemoryService");
        result.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(result);
    }
}
