package com.vijay.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🧠 Long-Term Memory - User Profile & Preferences
 * 
 * Purpose: Store persistent user information
 * 
 * Stores:
 * ✅ User name & email
 * ✅ Preferences (language, timezone, etc.)
 * ✅ Projects & tools used
 * ✅ Skills & expertise
 * ✅ Communication style
 * ✅ Custom settings
 * 
 * Benefits:
 * - Personalized responses
 * - Faster context retrieval
 * - Better recommendations
 * - Consistent experience
 */
@Service
public class LongTermMemory {
    
    private static final Logger logger = LoggerFactory.getLogger(LongTermMemory.class);
    
    // User profiles: userId -> UserProfile
    private final Map<String, UserProfile> profiles = new ConcurrentHashMap<>();
    
    /**
     * Get or create user profile
     */
    public UserProfile getOrCreateProfile(String userId) {
        return profiles.computeIfAbsent(userId, k -> {
            logger.info("🧠 Creating new user profile for: {}", userId);
            return new UserProfile(userId);
        });
    }
    
    /**
     * Update user profile
     */
    public void updateProfile(String userId, UserProfile profile) {
        profiles.put(userId, profile);
        logger.info("🧠 Updated profile for user: {}", userId);
    }
    
    /**
     * Get user profile
     */
    public Optional<UserProfile> getProfile(String userId) {
        return Optional.ofNullable(profiles.get(userId));
    }
    
    /**
     * Add project to user's profile
     */
    public void addProject(String userId, String projectName) {
        UserProfile profile = getOrCreateProfile(userId);
        profile.projects.add(projectName);
        logger.info("🧠 Added project '{}' to user {}", projectName, userId);
    }
    
    /**
     * Add tool to user's profile
     */
    public void addTool(String userId, String toolName) {
        UserProfile profile = getOrCreateProfile(userId);
        profile.tools.add(toolName);
        logger.info("🧠 Added tool '{}' to user {}", toolName, userId);
    }
    
    /**
     * Set user preference
     */
    public void setPreference(String userId, String key, String value) {
        UserProfile profile = getOrCreateProfile(userId);
        profile.preferences.put(key, value);
        logger.info("🧠 Set preference '{}={}' for user {}", key, value, userId);
    }
    
    /**
     * Get user preference
     */
    public String getPreference(String userId, String key, String defaultValue) {
        UserProfile profile = getOrCreateProfile(userId);
        return profile.preferences.getOrDefault(key, defaultValue);
    }
    
    /**
     * Get user context for LLM
     */
    public String getContextForLLM(String userId) {
        UserProfile profile = getOrCreateProfile(userId);
        
        StringBuilder context = new StringBuilder();
        context.append("User Profile:\n");
        
        if (profile.name != null) {
            context.append(String.format("- Name: %s\n", profile.name));
        }
        
        if (!profile.projects.isEmpty()) {
            context.append(String.format("- Projects: %s\n", String.join(", ", profile.projects)));
        }
        
        if (!profile.tools.isEmpty()) {
            context.append(String.format("- Tools: %s\n", String.join(", ", profile.tools)));
        }
        
        if (!profile.skills.isEmpty()) {
            context.append(String.format("- Skills: %s\n", String.join(", ", profile.skills)));
        }
        
        if (!profile.preferences.isEmpty()) {
            context.append("- Preferences:\n");
            profile.preferences.forEach((k, v) -> 
                context.append(String.format("  - %s: %s\n", k, v))
            );
        }
        
        return context.toString();
    }
    
    /**
     * User Profile DTO
     */
    public static class UserProfile {
        public String userId;
        public String name;
        public String email;
        public Set<String> projects = new HashSet<>();
        public Set<String> tools = new HashSet<>();
        public Set<String> skills = new HashSet<>();
        public Map<String, String> preferences = new ConcurrentHashMap<>();
        public long createdAt = System.currentTimeMillis();
        public long lastUpdated = System.currentTimeMillis();
        
        public UserProfile(String userId) {
            this.userId = userId;
        }
        
        public void addSkill(String skill) {
            skills.add(skill);
            lastUpdated = System.currentTimeMillis();
        }
        
        public void setName(String name) {
            this.name = name;
            lastUpdated = System.currentTimeMillis();
        }
        
        public void setEmail(String email) {
            this.email = email;
            lastUpdated = System.currentTimeMillis();
        }
        
        @Override
        public String toString() {
            return String.format(
                "UserProfile{userId='%s', name='%s', projects=%d, tools=%d, skills=%d}",
                userId, name, projects.size(), tools.size(), skills.size()
            );
        }
    }
}
