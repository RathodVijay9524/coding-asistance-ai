package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 🧠 Brain Memory: Conversation Memory Manager
 * 
 * CRITICAL COMPONENT: Eliminates AI amnesia by maintaining conversation context
 * 
 * Features:
 * - Multi-session conversation tracking
 * - Context-aware memory retrieval
 * - Automatic memory pruning and summarization
 * - User preference learning
 * - Query pattern recognition
 * 
 * This transforms the AI from stateless to stateful, enabling:
 * - "Remember when we discussed X?"
 * - "As I mentioned earlier..."
 * - "Based on your previous questions..."
 * - "You typically prefer Y approach..."
 */
@Service
public class ConversationMemoryManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ConversationMemoryManager.class);
    
    // Memory storage
    private final Map<String, ConversationSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, UserProfile> userProfiles = new ConcurrentHashMap<>();
    private final List<ConversationMemory> longTermMemory = new ArrayList<>();
    
    // Memory configuration
    private static final int MAX_SHORT_TERM_MEMORIES = 20;
    private static final int MAX_LONG_TERM_MEMORIES = 100;
    private static final int MEMORY_RELEVANCE_THRESHOLD = 70; // 0-100 scale
    
    /**
     * 🎯 Store a new conversation exchange
     */
    public void storeConversation(String sessionId, String userId, String userQuery, 
                                 String aiResponse, String searchStrategy, double confidence) {
        logger.info("🧠 Storing conversation memory - Session: {}, User: {}", sessionId, userId);
        
        ConversationExchange exchange = new ConversationExchange(
            userQuery, aiResponse, searchStrategy, confidence, LocalDateTime.now()
        );
        
        // Update session memory
        ConversationSession session = activeSessions.computeIfAbsent(sessionId, 
            k -> new ConversationSession(sessionId, userId));
        session.addExchange(exchange);
        
        // Update user profile
        UserProfile profile = userProfiles.computeIfAbsent(userId, 
            k -> new UserProfile(userId));
        profile.updateFromExchange(exchange);
        
        // Check if memory should be promoted to long-term
        if (shouldPromoteToLongTerm(exchange)) {
            promoteToLongTermMemory(sessionId, exchange);
        }
        
        // Prune old memories if needed
        pruneMemoriesIfNeeded();
        
        logger.debug("💾 Memory stored - Session exchanges: {}, User profile updated", 
            session.getExchanges().size());
    }
    
    /**
     * 🔍 Retrieve relevant conversation context for current query
     */
    public ConversationContext getRelevantContext(String sessionId, String userId, String currentQuery) {
        logger.info("🔍 Retrieving conversation context - Session: {}, Query: '{}'", sessionId, currentQuery);
        
        ConversationContext context = new ConversationContext();
        
        // Get current session context
        ConversationSession session = activeSessions.get(sessionId);
        if (session != null) {
            List<ConversationExchange> recentExchanges = session.getRecentExchanges(5);
            context.setRecentExchanges(recentExchanges);
            
            // Find related previous discussions
            List<ConversationExchange> relatedExchanges = findRelatedExchanges(session, currentQuery);
            context.setRelatedExchanges(relatedExchanges);
        }
        
        // Get user preferences and patterns
        UserProfile profile = userProfiles.get(userId);
        if (profile != null) {
            context.setUserPreferences(profile.getPreferences());
            context.setPreferredSearchStrategies(profile.getPreferredStrategies());
            context.setTypicalQueryPatterns(profile.getQueryPatterns());
        }
        
        // Get relevant long-term memories
        List<ConversationMemory> relevantMemories = findRelevantLongTermMemories(currentQuery);
        context.setLongTermMemories(relevantMemories);
        
        logger.info("🧠 Context retrieved - Recent: {}, Related: {}, LongTerm: {}", 
            context.getRecentExchanges().size(), 
            context.getRelatedExchanges().size(),
            context.getLongTermMemories().size());
        
        return context;
    }
    
    /**
     * 📊 Get user's conversation statistics and insights
     */
    public UserInsights getUserInsights(String userId) {
        UserProfile profile = userProfiles.get(userId);
        if (profile == null) {
            return new UserInsights(userId);
        }
        
        UserInsights insights = new UserInsights(userId);
        insights.setTotalQueries(profile.getTotalQueries());
        insights.setPreferredStrategies(profile.getPreferredStrategies());
        insights.setCommonTopics(profile.getCommonTopics());
        insights.setAverageConfidence(profile.getAverageConfidence());
        insights.setLastActiveTime(profile.getLastActiveTime());
        
        return insights;
    }
    
    /**
     * 🧹 Clean up old sessions and optimize memory usage
     */
    public void cleanupOldSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        
        List<String> sessionsToRemove = activeSessions.entrySet().stream()
            .filter(entry -> entry.getValue().getLastActivityTime().isBefore(cutoff))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        for (String sessionId : sessionsToRemove) {
            ConversationSession session = activeSessions.remove(sessionId);
            logger.info("🧹 Cleaned up old session: {} (last activity: {})", 
                sessionId, session.getLastActivityTime());
        }
        
        logger.info("🧹 Memory cleanup complete - Removed {} old sessions", sessionsToRemove.size());
    }
    
    // Private helper methods
    
    private boolean shouldPromoteToLongTerm(ConversationExchange exchange) {
        // Promote if high confidence and contains important keywords
        if (exchange.getConfidence() > 0.8) {
            String query = exchange.getUserQuery().toLowerCase();
            return query.contains("architecture") || query.contains("design") || 
                   query.contains("pattern") || query.contains("implementation") ||
                   query.contains("error") || query.contains("bug");
        }
        return false;
    }
    
    private void promoteToLongTermMemory(String sessionId, ConversationExchange exchange) {
        ConversationMemory memory = new ConversationMemory(
            sessionId, exchange, LocalDateTime.now(), calculateImportanceScore(exchange)
        );
        
        longTermMemory.add(memory);
        logger.debug("📚 Promoted to long-term memory - Importance: {}", memory.getImportanceScore());
    }
    
    private double calculateImportanceScore(ConversationExchange exchange) {
        double score = exchange.getConfidence() * 50; // Base score from confidence
        
        String query = exchange.getUserQuery().toLowerCase();
        // Boost score for important topics
        if (query.contains("architecture")) score += 20;
        if (query.contains("error") || query.contains("bug")) score += 15;
        if (query.contains("design") || query.contains("pattern")) score += 10;
        
        return Math.min(100, score);
    }
    
    private List<ConversationExchange> findRelatedExchanges(ConversationSession session, String currentQuery) {
        return session.getExchanges().stream()
            .filter(exchange -> calculateSimilarity(exchange.getUserQuery(), currentQuery) > 0.6)
            .sorted((a, b) -> Double.compare(
                calculateSimilarity(b.getUserQuery(), currentQuery),
                calculateSimilarity(a.getUserQuery(), currentQuery)
            ))
            .limit(3)
            .collect(Collectors.toList());
    }
    
    private List<ConversationMemory> findRelevantLongTermMemories(String currentQuery) {
        return longTermMemory.stream()
            .filter(memory -> calculateSimilarity(memory.getExchange().getUserQuery(), currentQuery) > 0.5)
            .sorted((a, b) -> Double.compare(b.getImportanceScore(), a.getImportanceScore()))
            .limit(2)
            .collect(Collectors.toList());
    }
    
    private double calculateSimilarity(String query1, String query2) {
        // Simple keyword-based similarity (can be enhanced with embeddings later)
        Set<String> words1 = new HashSet<>(Arrays.asList(query1.toLowerCase().split("\\s+")));
        Set<String> words2 = new HashSet<>(Arrays.asList(query2.toLowerCase().split("\\s+")));
        
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    private void pruneMemoriesIfNeeded() {
        // Prune long-term memories if too many
        if (longTermMemory.size() > MAX_LONG_TERM_MEMORIES) {
            longTermMemory.sort((a, b) -> Double.compare(b.getImportanceScore(), a.getImportanceScore()));
            longTermMemory.subList(MAX_LONG_TERM_MEMORIES, longTermMemory.size()).clear();
            logger.debug("🧹 Pruned long-term memories to {} entries", MAX_LONG_TERM_MEMORIES);
        }
        
        // Prune session memories
        for (ConversationSession session : activeSessions.values()) {
            if (session.getExchanges().size() > MAX_SHORT_TERM_MEMORIES) {
                List<ConversationExchange> exchanges = session.getExchanges();
                session.setExchanges(exchanges.subList(
                    exchanges.size() - MAX_SHORT_TERM_MEMORIES, exchanges.size()));
            }
        }
    }
    
    // Data classes
    
    public static class ConversationExchange {
        private final String userQuery;
        private final String aiResponse;
        private final String searchStrategy;
        private final double confidence;
        private final LocalDateTime timestamp;
        
        public ConversationExchange(String userQuery, String aiResponse, String searchStrategy, 
                                  double confidence, LocalDateTime timestamp) {
            this.userQuery = userQuery;
            this.aiResponse = aiResponse;
            this.searchStrategy = searchStrategy;
            this.confidence = confidence;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getUserQuery() { return userQuery; }
        public String getAiResponse() { return aiResponse; }
        public String getSearchStrategy() { return searchStrategy; }
        public double getConfidence() { return confidence; }
        public LocalDateTime getTimestamp() { return timestamp; }
        
        public String getFormattedTimestamp() {
            return timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
    }
    
    public static class ConversationSession {
        private final String sessionId;
        private final String userId;
        private final LocalDateTime startTime;
        private LocalDateTime lastActivityTime;
        private List<ConversationExchange> exchanges;
        
        public ConversationSession(String sessionId, String userId) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.startTime = LocalDateTime.now();
            this.lastActivityTime = LocalDateTime.now();
            this.exchanges = new ArrayList<>();
        }
        
        public void addExchange(ConversationExchange exchange) {
            exchanges.add(exchange);
            lastActivityTime = LocalDateTime.now();
        }
        
        public List<ConversationExchange> getRecentExchanges(int count) {
            int start = Math.max(0, exchanges.size() - count);
            return new ArrayList<>(exchanges.subList(start, exchanges.size()));
        }
        
        // Getters and setters
        public String getSessionId() { return sessionId; }
        public String getUserId() { return userId; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getLastActivityTime() { return lastActivityTime; }
        public List<ConversationExchange> getExchanges() { return exchanges; }
        public void setExchanges(List<ConversationExchange> exchanges) { this.exchanges = exchanges; }
    }
    
    public static class UserProfile {
        private final String userId;
        private final LocalDateTime createdTime;
        private LocalDateTime lastActiveTime;
        private int totalQueries;
        private Map<String, Integer> strategyUsage;
        private Map<String, Integer> topicFrequency;
        private double totalConfidence;
        private List<String> preferences;
        
        public UserProfile(String userId) {
            this.userId = userId;
            this.createdTime = LocalDateTime.now();
            this.lastActiveTime = LocalDateTime.now();
            this.totalQueries = 0;
            this.strategyUsage = new HashMap<>();
            this.topicFrequency = new HashMap<>();
            this.totalConfidence = 0.0;
            this.preferences = new ArrayList<>();
        }
        
        public void updateFromExchange(ConversationExchange exchange) {
            totalQueries++;
            lastActiveTime = LocalDateTime.now();
            totalConfidence += exchange.getConfidence();
            
            // Update strategy usage
            strategyUsage.merge(exchange.getSearchStrategy(), 1, Integer::sum);
            
            // Update topic frequency (simple keyword extraction)
            String query = exchange.getUserQuery().toLowerCase();
            if (query.contains("architecture")) topicFrequency.merge("architecture", 1, Integer::sum);
            if (query.contains("implementation")) topicFrequency.merge("implementation", 1, Integer::sum);
            if (query.contains("error") || query.contains("bug")) topicFrequency.merge("debugging", 1, Integer::sum);
            if (query.contains("config")) topicFrequency.merge("configuration", 1, Integer::sum);
        }
        
        public List<String> getPreferredStrategies() {
            return strategyUsage.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        }
        
        public List<String> getCommonTopics() {
            return topicFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        }
        
        public double getAverageConfidence() {
            return totalQueries > 0 ? totalConfidence / totalQueries : 0.0;
        }
        
        public List<String> getQueryPatterns() {
            // Simple pattern recognition based on common topics
            List<String> patterns = new ArrayList<>();
            if (topicFrequency.getOrDefault("architecture", 0) > 2) {
                patterns.add("Frequently asks about system architecture");
            }
            if (topicFrequency.getOrDefault("debugging", 0) > 2) {
                patterns.add("Often needs debugging assistance");
            }
            if (getAverageConfidence() > 0.8) {
                patterns.add("Asks clear, well-defined questions");
            }
            return patterns;
        }
        
        // Getters
        public String getUserId() { return userId; }
        public LocalDateTime getCreatedTime() { return createdTime; }
        public LocalDateTime getLastActiveTime() { return lastActiveTime; }
        public int getTotalQueries() { return totalQueries; }
        public List<String> getPreferences() { return preferences; }
    }
    
    public static class ConversationMemory {
        private final String sessionId;
        private final ConversationExchange exchange;
        private final LocalDateTime storedTime;
        private final double importanceScore;
        
        public ConversationMemory(String sessionId, ConversationExchange exchange, 
                                LocalDateTime storedTime, double importanceScore) {
            this.sessionId = sessionId;
            this.exchange = exchange;
            this.storedTime = storedTime;
            this.importanceScore = importanceScore;
        }
        
        // Getters
        public String getSessionId() { return sessionId; }
        public ConversationExchange getExchange() { return exchange; }
        public LocalDateTime getStoredTime() { return storedTime; }
        public double getImportanceScore() { return importanceScore; }
    }
    
    public static class ConversationContext {
        private List<ConversationExchange> recentExchanges = new ArrayList<>();
        private List<ConversationExchange> relatedExchanges = new ArrayList<>();
        private List<ConversationMemory> longTermMemories = new ArrayList<>();
        private List<String> userPreferences = new ArrayList<>();
        private List<String> preferredSearchStrategies = new ArrayList<>();
        private List<String> typicalQueryPatterns = new ArrayList<>();
        
        // Getters and setters
        public List<ConversationExchange> getRecentExchanges() { return recentExchanges; }
        public void setRecentExchanges(List<ConversationExchange> recentExchanges) { this.recentExchanges = recentExchanges; }
        
        public List<ConversationExchange> getRelatedExchanges() { return relatedExchanges; }
        public void setRelatedExchanges(List<ConversationExchange> relatedExchanges) { this.relatedExchanges = relatedExchanges; }
        
        public List<ConversationMemory> getLongTermMemories() { return longTermMemories; }
        public void setLongTermMemories(List<ConversationMemory> longTermMemories) { this.longTermMemories = longTermMemories; }
        
        public List<String> getUserPreferences() { return userPreferences; }
        public void setUserPreferences(List<String> userPreferences) { this.userPreferences = userPreferences; }
        
        public List<String> getPreferredSearchStrategies() { return preferredSearchStrategies; }
        public void setPreferredSearchStrategies(List<String> preferredSearchStrategies) { this.preferredSearchStrategies = preferredSearchStrategies; }
        
        public List<String> getTypicalQueryPatterns() { return typicalQueryPatterns; }
        public void setTypicalQueryPatterns(List<String> typicalQueryPatterns) { this.typicalQueryPatterns = typicalQueryPatterns; }
        
        public boolean hasRelevantContext() {
            return !recentExchanges.isEmpty() || !relatedExchanges.isEmpty() || !longTermMemories.isEmpty();
        }
        
        public String getFormattedContext() {
            StringBuilder context = new StringBuilder();
            
            if (!recentExchanges.isEmpty()) {
                context.append("🕒 **Recent Conversation:**\n");
                for (ConversationExchange exchange : recentExchanges) {
                    context.append(String.format("- [%s] Q: %s\n", 
                        exchange.getFormattedTimestamp(), 
                        exchange.getUserQuery().substring(0, Math.min(60, exchange.getUserQuery().length()))));
                }
                context.append("\n");
            }
            
            if (!relatedExchanges.isEmpty()) {
                context.append("🔗 **Related Previous Discussions:**\n");
                for (ConversationExchange exchange : relatedExchanges) {
                    context.append(String.format("- Q: %s\n", 
                        exchange.getUserQuery().substring(0, Math.min(80, exchange.getUserQuery().length()))));
                }
                context.append("\n");
            }
            
            if (!preferredSearchStrategies.isEmpty()) {
                context.append("🎯 **User typically prefers:** " + String.join(", ", preferredSearchStrategies) + "\n\n");
            }
            
            return context.toString();
        }
    }
    
    public static class UserInsights {
        private final String userId;
        private int totalQueries;
        private List<String> preferredStrategies = new ArrayList<>();
        private List<String> commonTopics = new ArrayList<>();
        private double averageConfidence;
        private LocalDateTime lastActiveTime;
        
        public UserInsights(String userId) {
            this.userId = userId;
        }
        
        // Getters and setters
        public String getUserId() { return userId; }
        public int getTotalQueries() { return totalQueries; }
        public void setTotalQueries(int totalQueries) { this.totalQueries = totalQueries; }
        public List<String> getPreferredStrategies() { return preferredStrategies; }
        public void setPreferredStrategies(List<String> preferredStrategies) { this.preferredStrategies = preferredStrategies; }
        public List<String> getCommonTopics() { return commonTopics; }
        public void setCommonTopics(List<String> commonTopics) { this.commonTopics = commonTopics; }
        public double getAverageConfidence() { return averageConfidence; }
        public void setAverageConfidence(double averageConfidence) { this.averageConfidence = averageConfidence; }
        public LocalDateTime getLastActiveTime() { return lastActiveTime; }
        public void setLastActiveTime(LocalDateTime lastActiveTime) { this.lastActiveTime = lastActiveTime; }
    }
}
