package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 🧠 User Preference Evolution
 * 
 * Tracks and evolves user preferences over time.
 * Learns what users prefer and adapts to their changing needs.
 */
@Service
public class UserPreferenceEvolution {
    
    private static final Logger logger = LoggerFactory.getLogger(UserPreferenceEvolution.class);
    
    private final Map<String, Map<String, Double>> userPreferences = new ConcurrentHashMap<>();
    private final Map<String, Integer> userInteractionCounts = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Integer>> preferenceHistory = new ConcurrentHashMap<>();
    
    private static final double LEARNING_RATE = 0.1; // 10% weight to new feedback
    private static final int EVOLUTION_THRESHOLD = 5; // Evolve after 5 interactions
    
    public UserPreferenceEvolution() {
        logger.info("🧠 User Preference Evolution initialized - Dynamic preference learning");
    }
    
    /**
     * Record user preference feedback
     */
    public void recordPreferenceFeedback(String userId, String preference, double score) {
        logger.debug("📝 Recording preference feedback - User: {}, Preference: {}, Score: {}", 
            userId, preference, score);
        
        Map<String, Double> prefs = userPreferences.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        
        // Update preference with exponential moving average
        double currentScore = prefs.getOrDefault(preference, 0.5);
        double newScore = currentScore * (1 - LEARNING_RATE) + score * LEARNING_RATE;
        prefs.put(preference, newScore);
        
        // Track interaction count
        int count = userInteractionCounts.getOrDefault(userId, 0) + 1;
        userInteractionCounts.put(userId, count);
        
        // Record in history
        recordInHistory(userId, preference);
        
        logger.debug("✅ Preference updated - {} now has score {:.2f}", preference, newScore);
    }
    
    /**
     * Get user's top preferences
     */
    public List<String> getTopPreferences(String userId, int limit) {
        logger.debug("🎯 Getting top {} preferences for user: {}", limit, userId);
        
        Map<String, Double> prefs = userPreferences.getOrDefault(userId, new HashMap<>());
        
        return prefs.entrySet().stream()
            .sorted(Comparator.comparingDouble(Map.Entry<String, Double>::getValue).reversed())
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    /**
     * Get preference score for user
     */
    public double getPreferenceScore(String userId, String preference) {
        Map<String, Double> prefs = userPreferences.getOrDefault(userId, new HashMap<>());
        return prefs.getOrDefault(preference, 0.5);
    }
    
    /**
     * Check if user preference has evolved
     */
    public boolean hasPreferenceEvolved(String userId, String preference) {
        int count = userInteractionCounts.getOrDefault(userId, 0);
        return count >= EVOLUTION_THRESHOLD;
    }
    
    /**
     * Get preference trend for user
     */
    public String getPreferenceTrend(String userId, String preference) {
        double score = getPreferenceScore(userId, preference);
        
        if (score >= 0.8) {
            return "📈 Strong preference - Highly valued";
        } else if (score >= 0.6) {
            return "📊 Moderate preference - Generally liked";
        } else if (score >= 0.4) {
            return "⚠️ Weak preference - Neutral";
        } else {
            return "📉 Disliked - Avoid if possible";
        }
    }
    
    /**
     * Get all preferences for user
     */
    public Map<String, Double> getAllPreferences(String userId) {
        return new HashMap<>(userPreferences.getOrDefault(userId, new HashMap<>()));
    }
    
    /**
     * Get preference evolution summary
     */
    public String getEvolutionSummary(String userId) {
        int interactionCount = userInteractionCounts.getOrDefault(userId, 0);
        Map<String, Double> prefs = userPreferences.getOrDefault(userId, new HashMap<>());
        
        if (prefs.isEmpty()) {
            return "No preference data yet for user: " + userId;
        }
        
        double avgPreference = prefs.values().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.5);
        
        return String.format(
            "👤 Preference Evolution: %d interactions | %d preferences tracked | Avg score: %.2f",
            interactionCount, prefs.size(), avgPreference
        );
    }
    
    /**
     * Record preference in history
     */
    private void recordInHistory(String userId, String preference) {
        Map<String, Integer> history = preferenceHistory.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        history.put(preference, history.getOrDefault(preference, 0) + 1);
    }
    
    /**
     * Get most frequently mentioned preferences
     */
    public List<String> getMostFrequentPreferences(String userId, int limit) {
        Map<String, Integer> history = preferenceHistory.getOrDefault(userId, new HashMap<>());
        
        return history.entrySet().stream()
            .sorted(Comparator.comparingInt(Map.Entry<String, Integer>::getValue).reversed())
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    /**
     * Predict next user preference
     */
    public String predictNextPreference(String userId) {
        List<String> topPrefs = getTopPreferences(userId, 3);
        
        if (topPrefs.isEmpty()) {
            return "unknown";
        }
        
        // Return the highest scoring preference
        return topPrefs.get(0);
    }
    
    /**
     * Reset preferences for user
     */
    public void resetPreferences(String userId) {
        userPreferences.remove(userId);
        userInteractionCounts.remove(userId);
        preferenceHistory.remove(userId);
        
        logger.info("🔄 Preferences reset for user: {}", userId);
    }
    
    /**
     * Get interaction count for user
     */
    public int getInteractionCount(String userId) {
        return userInteractionCounts.getOrDefault(userId, 0);
    }
}
