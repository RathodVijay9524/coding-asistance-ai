package com.vijay.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 💭 Short-Term Memory - Last 10 messages
 * 
 * Purpose: Remember recent conversation context
 * 
 * Features:
 * ✅ Stores last 10 messages
 * ✅ Auto-expires after 30 minutes
 * ✅ Thread-safe
 * ✅ Easy context retrieval
 * 
 * Benefits:
 * - Better conversation continuity
 * - Faster context retrieval
 * - Reduced token usage (don't repeat context)
 */
@Service
public class ShortTermMemory {
    
    private static final Logger logger = LoggerFactory.getLogger(ShortTermMemory.class);
    private static final int MAX_MESSAGES = 10;
    private static final long EXPIRY_TIME_MS = 30 * 60 * 1000;  // 30 minutes
    
    private final Deque<MemoryMessage> messages = new ConcurrentLinkedDeque<>();
    
    /**
     * Add a message to short-term memory
     */
    public void addMessage(String userId, String role, String content) {
        MemoryMessage msg = new MemoryMessage(userId, role, content);
        messages.addLast(msg);
        
        // Keep only last 10 messages
        while (messages.size() > MAX_MESSAGES) {
            messages.removeFirst();
        }
        
        logger.info("💭 Short-Term Memory: Added {} message (total: {})", role, messages.size());
    }
    
    /**
     * Get last N messages
     */
    public List<MemoryMessage> getLastMessages(int count) {
        List<MemoryMessage> result = new ArrayList<>();
        int i = 0;
        
        Iterator<MemoryMessage> iterator = messages.descendingIterator();
        while (iterator.hasNext() && i < count) {
            MemoryMessage msg = iterator.next();
            
            // Check if expired
            if (System.currentTimeMillis() - msg.timestamp > EXPIRY_TIME_MS) {
                continue;
            }
            
            result.add(0, msg);
            i++;
        }
        
        logger.info("💭 Retrieved {} recent messages from short-term memory", result.size());
        return result;
    }
    
    /**
     * Get conversation context (formatted for LLM)
     */
    public String getContextForLLM() {
        List<MemoryMessage> recent = getLastMessages(5);
        
        if (recent.isEmpty()) {
            return "";
        }
        
        StringBuilder context = new StringBuilder();
        context.append("Recent conversation context:\n");
        
        for (MemoryMessage msg : recent) {
            context.append(String.format("- %s: %s\n", msg.role, msg.content));
        }
        
        return context.toString();
    }
    
    /**
     * Clear all messages
     */
    public void clear() {
        messages.clear();
        logger.info("💭 Short-Term Memory cleared");
    }
    
    /**
     * Get memory size
     */
    public int size() {
        return messages.size();
    }
    
    /**
     * Memory message DTO
     */
    public static class MemoryMessage {
        public String userId;
        public String role;           // "user", "assistant", "system"
        public String content;
        public long timestamp;
        
        public MemoryMessage(String userId, String role, String content) {
            this.userId = userId;
            this.role = role;
            this.content = content;
            this.timestamp = System.currentTimeMillis();
        }
        
        @Override
        public String toString() {
            return String.format("%s: %s", role, content.substring(0, Math.min(50, content.length())));
        }
    }
}
