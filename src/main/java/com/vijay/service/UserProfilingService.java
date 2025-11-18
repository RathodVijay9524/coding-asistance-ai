package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🧠 Brain 5: User Profiling Service
 * 
 * Builds user profiles including:
 * - Expertise level (1-5 scale)
 * - Preferred response format
 * - Specialization areas
 * - Interaction patterns
 * - Query complexity preferences
 */
@Service
public class UserProfilingService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserProfilingService.class);
    
    private final Map<String, UserProfile> userProfiles = new ConcurrentHashMap<>();
    
    /**
     * Get or create user profile
     */
    public UserProfile getUserProfile(String userId) {
        return userProfiles.computeIfAbsent(userId, k -> {
            logger.info("👤 User Profiling: Creating new profile for user: {}", userId);
            return new UserProfile(userId);
        });
    }
    
    /**
     * Record user interaction
     */
    public void recordInteraction(String userId, String queryType, int responseQuality, String specialization) {
        UserProfile profile = getUserProfile(userId);
        profile.recordInteraction(queryType, responseQuality, specialization);
        
        logger.debug("👤 User Profiling: Recorded interaction for {} - Type: {}, Quality: {}", 
            userId, queryType, responseQuality);
    }
    
    /**
     * Record user feedback
     */
    public void recordFeedback(String userId, int rating, String feedback) {
        UserProfile profile = getUserProfile(userId);
        profile.recordFeedback(rating, feedback);
        
        logger.info("👤 User Profiling: Recorded feedback from {} - Rating: {}/5", userId, rating);
    }
    
    /**
     * Update user preferences
     */
    public void updatePreference(String userId, String key, String value) {
        UserProfile profile = getUserProfile(userId);
        profile.setPreference(key, value);
        
        logger.debug("👤 User Profiling: Updated preference for {} - {}: {}", userId, key, value);
    }
    
    /**
     * Detect user expertise level
     */
    public int detectExpertiseLevel(String userId) {
        UserProfile profile = getUserProfile(userId);
        return profile.getExpertiseLevel();
    }
    
    /**
     * Get user specializations
     */
    public List<String> getUserSpecializations(String userId) {
        UserProfile profile = getUserProfile(userId);
        return profile.getSpecializations();
    }
    
    /**
     * Get recommended response format for user
     */
    public String getPreferredResponseFormat(String userId) {
        UserProfile profile = getUserProfile(userId);
        return profile.getPreferredResponseFormat();
    }
    
    /**
     * Get user profile summary
     */
    public UserProfileSummary getProfileSummary(String userId) {
        UserProfile profile = getUserProfile(userId);
        return profile.getSummary();
    }
    
    // Inner classes
    
    public static class UserProfile {
        private final String userId;
        private int expertiseLevel = 2; // 1-5 scale, default intermediate
        private String preferredResponseFormat = "balanced"; // concise, detailed, code-heavy, balanced
        private final Map<String, Integer> specializationScores = new ConcurrentHashMap<>();
        private final List<Interaction> interactions = new ArrayList<>();
        private final List<Feedback> feedbackHistory = new ArrayList<>();
        private final Map<String, String> preferences = new ConcurrentHashMap<>();
        private LocalDateTime lastInteraction;
        private LocalDateTime createdAt;
        
        public UserProfile(String userId) {
            this.userId = userId;
            this.createdAt = LocalDateTime.now();
            this.lastInteraction = LocalDateTime.now();
        }
        
        public void recordInteraction(String queryType, int responseQuality, String specialization) {
            interactions.add(new Interaction(queryType, responseQuality, specialization));
            lastInteraction = LocalDateTime.now();
            
            // Update expertise level based on query complexity and quality
            updateExpertiseLevel(queryType, responseQuality);
            
            // Update specialization scores
            if (specialization != null && !specialization.isEmpty()) {
                specializationScores.merge(specialization, 1, Integer::sum);
            }
        }
        
        public void recordFeedback(int rating, String feedback) {
            feedbackHistory.add(new Feedback(rating, feedback));
            
            // Adjust expertise level based on feedback patterns
            if (feedbackHistory.size() >= 5) {
                double avgRating = feedbackHistory.stream()
                    .mapToInt(f -> f.rating)
                    .average()
                    .orElse(3.0);
                
                if (avgRating >= 4.5) {
                    expertiseLevel = Math.min(5, expertiseLevel + 1);
                } else if (avgRating <= 2.5) {
                    expertiseLevel = Math.max(1, expertiseLevel - 1);
                }
            }
        }
        
        public void setPreference(String key, String value) {
            preferences.put(key, value);
            
            // Update response format preference
            if ("responseFormat".equals(key)) {
                this.preferredResponseFormat = value;
            }
        }
        
        private void updateExpertiseLevel(String queryType, int responseQuality) {
            // Complex queries with high quality responses indicate higher expertise
            if (queryType.equals("ARCHITECTURE") && responseQuality >= 4) {
                expertiseLevel = Math.min(5, expertiseLevel + 1);
            } else if (queryType.equals("DEBUGGING") && responseQuality >= 4) {
                expertiseLevel = Math.min(5, expertiseLevel + 1);
            } else if (responseQuality <= 2) {
                expertiseLevel = Math.max(1, expertiseLevel - 1);
            }
        }
        
        public int getExpertiseLevel() {
            return expertiseLevel;
        }
        
        public List<String> getSpecializations() {
            return specializationScores.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();
        }
        
        public String getPreferredResponseFormat() {
            return preferredResponseFormat;
        }
        
        public String getPreference(String key) {
            return preferences.getOrDefault(key, "default");
        }
        
        public int getInteractionCount() {
            return interactions.size();
        }
        
        public double getAverageResponseQuality() {
            return interactions.stream()
                .mapToInt(i -> i.responseQuality)
                .average()
                .orElse(3.0);
        }
        
        public UserProfileSummary getSummary() {
            UserProfileSummary summary = new UserProfileSummary();
            summary.userId = this.userId;
            summary.expertiseLevel = this.expertiseLevel;
            summary.preferredResponseFormat = this.preferredResponseFormat;
            summary.specializations = getSpecializations();
            summary.interactionCount = getInteractionCount();
            summary.averageQuality = getAverageResponseQuality();
            summary.lastInteraction = this.lastInteraction;
            summary.createdAt = this.createdAt;
            return summary;
        }
    }
    
    public static class Interaction {
        public final String queryType;
        public final int responseQuality;
        public final String specialization;
        public final LocalDateTime timestamp;
        
        public Interaction(String queryType, int responseQuality, String specialization) {
            this.queryType = queryType;
            this.responseQuality = responseQuality;
            this.specialization = specialization;
            this.timestamp = LocalDateTime.now();
        }
    }
    
    public static class Feedback {
        public final int rating;
        public final String feedback;
        public final LocalDateTime timestamp;
        
        public Feedback(int rating, String feedback) {
            this.rating = rating;
            this.feedback = feedback;
            this.timestamp = LocalDateTime.now();
        }
    }
    
    public static class UserProfileSummary {
        public String userId;
        public int expertiseLevel;
        public String preferredResponseFormat;
        public List<String> specializations;
        public int interactionCount;
        public double averageQuality;
        public LocalDateTime lastInteraction;
        public LocalDateTime createdAt;
        
        @Override
        public String toString() {
            return String.format(
                "User: %s | Expertise: %d/5 | Format: %s | Specializations: %s | Interactions: %d | Avg Quality: %.2f",
                userId, expertiseLevel, preferredResponseFormat, specializations, interactionCount, averageQuality
            );
        }
    }
}
