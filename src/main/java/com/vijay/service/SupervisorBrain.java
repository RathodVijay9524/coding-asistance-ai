package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 🧠 SUPERVISOR BRAIN - Phase 7
 * 
 * Purpose: Orchestrates all brains, tracks global state, manages output merging,
 * controls re-evaluations, and enforces consistency across the system.
 * 
 * Responsibilities:
 * - Track global system state
 * - Manage output merging from multiple brains
 * - Control re-evaluation cycles
 * - Enforce consistency across responses
 * - Monitor brain performance
 * - Manage resource allocation
 * - Track conversation flow
 */
@Service
public class SupervisorBrain {
    
    private static final Logger logger = LoggerFactory.getLogger(SupervisorBrain.class);
    
    // Global state tracking
    private final Map<String, ConversationState> conversationStates = new ConcurrentHashMap<>();
    private final Map<String, BrainPerformance> brainPerformance = new ConcurrentHashMap<>();
    
    // Configuration
    private static final int MAX_REEVALUATION_CYCLES = 3;
    private static final double CONSISTENCY_THRESHOLD = 0.85;
    private static final double QUALITY_THRESHOLD = 0.75;
    
    /**
     * Initialize supervisor for a new conversation
     */
    public void initializeConversation(String userId, String conversationId) {
        ConversationState state = new ConversationState(userId, conversationId);
        conversationStates.put(conversationId, state);
        logger.info("🧠 Supervisor: Initialized conversation {} for user {}", conversationId, userId);
    }
    
    /**
     * Track brain output
     */
    public void recordBrainOutput(String conversationId, String brainName, String output, double quality) {
        ConversationState state = conversationStates.get(conversationId);
        if (state != null) {
            state.recordBrainOutput(brainName, output, quality);
            
            // Update brain performance
            BrainPerformance perf = brainPerformance.computeIfAbsent(brainName, k -> new BrainPerformance(brainName));
            perf.recordExecution(quality);
            
            logger.debug("🧠 Supervisor: Recorded output from {} (quality: {:.2f})", brainName, quality);
        }
    }
    
    /**
     * Merge outputs from multiple brains
     */
    public MergedOutput mergeOutputs(String conversationId) {
        ConversationState state = conversationStates.get(conversationId);
        if (state == null) {
            logger.warn("🧠 Supervisor: No state found for conversation {}", conversationId);
            return new MergedOutput("", 0.0);
        }
        
        List<BrainOutput> outputs = state.getBrainOutputs();
        if (outputs.isEmpty()) {
            logger.warn("🧠 Supervisor: No brain outputs to merge");
            return new MergedOutput("", 0.0);
        }
        
        // Sort by quality (highest first)
        outputs.sort((a, b) -> Double.compare(b.quality, a.quality));
        
        // Merge strategy: weighted combination of top outputs
        StringBuilder mergedContent = new StringBuilder();
        double totalQuality = 0.0;
        int count = 0;
        
        for (BrainOutput output : outputs) {
            if (count < 3) { // Take top 3 outputs
                mergedContent.append(output.output).append("\n\n");
                totalQuality += output.quality;
                count++;
            }
        }
        
        double averageQuality = count > 0 ? totalQuality / count : 0.0;
        
        logger.info("🧠 Supervisor: Merged {} brain outputs (avg quality: {:.2f})", count, averageQuality);
        
        return new MergedOutput(mergedContent.toString().trim(), averageQuality);
    }
    
    /**
     * Check if re-evaluation is needed
     */
    public boolean shouldReevaluate(String conversationId, double currentQuality) {
        ConversationState state = conversationStates.get(conversationId);
        if (state == null) {
            return false;
        }
        
        // Re-evaluate if quality is below threshold and we haven't exceeded max cycles
        boolean needsReevaluation = currentQuality < QUALITY_THRESHOLD && 
                                   state.getReevaluationCycles() < MAX_REEVALUATION_CYCLES;
        
        if (needsReevaluation) {
            state.incrementReevaluationCycles();
            logger.info("🧠 Supervisor: Triggering re-evaluation (quality: {:.2f}, cycle: {})", 
                currentQuality, state.getReevaluationCycles());
        }
        
        return needsReevaluation;
    }
    
    /**
     * Check consistency across outputs
     */
    public ConsistencyReport checkConsistency(String conversationId) {
        ConversationState state = conversationStates.get(conversationId);
        if (state == null) {
            return new ConsistencyReport(0.0, new ArrayList<>());
        }
        
        List<BrainOutput> outputs = state.getBrainOutputs();
        if (outputs.size() < 2) {
            return new ConsistencyReport(1.0, new ArrayList<>());
        }
        
        List<String> inconsistencies = new ArrayList<>();
        double totalSimilarity = 0.0;
        int comparisons = 0;
        
        // Compare each pair of outputs
        for (int i = 0; i < outputs.size(); i++) {
            for (int j = i + 1; j < outputs.size(); j++) {
                double similarity = calculateSimilarity(outputs.get(i).output, outputs.get(j).output);
                totalSimilarity += similarity;
                comparisons++;
                
                if (similarity < 0.5) {
                    inconsistencies.add(String.format(
                        "Low similarity between %s and %s (%.2f)",
                        outputs.get(i).brainName, outputs.get(j).brainName, similarity
                    ));
                }
            }
        }
        
        double averageSimilarity = comparisons > 0 ? totalSimilarity / comparisons : 1.0;
        
        logger.info("🧠 Supervisor: Consistency check - similarity: {:.2f}, inconsistencies: {}", 
            averageSimilarity, inconsistencies.size());
        
        return new ConsistencyReport(averageSimilarity, inconsistencies);
    }
    
    /**
     * Calculate similarity between two texts (simple word overlap)
     */
    private double calculateSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null || text1.isEmpty() || text2.isEmpty()) {
            return 0.0;
        }
        
        String[] words1 = text1.toLowerCase().split("\\s+");
        String[] words2 = text2.toLowerCase().split("\\s+");
        
        Set<String> set1 = new HashSet<>(Arrays.asList(words1));
        Set<String> set2 = new HashSet<>(Arrays.asList(words2));
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    /**
     * Get brain performance statistics
     */
    public BrainPerformance getBrainPerformance(String brainName) {
        return brainPerformance.get(brainName);
    }
    
    /**
     * Get all brain performance statistics
     */
    public Map<String, BrainPerformance> getAllBrainPerformance() {
        return new HashMap<>(brainPerformance);
    }
    
    /**
     * Get conversation state
     */
    public ConversationState getConversationState(String conversationId) {
        return conversationStates.get(conversationId);
    }
    
    /**
     * Log supervisor status
     */
    public void logStatus(String conversationId) {
        ConversationState state = conversationStates.get(conversationId);
        if (state != null) {
            logger.info("🧠 Supervisor Status for conversation {}:", conversationId);
            logger.info("   📊 Brain outputs: {}", state.getBrainOutputs().size());
            logger.info("   🔄 Re-evaluation cycles: {}", state.getReevaluationCycles());
            logger.info("   ⏱️ Duration: {}ms", state.getDuration());
        }
    }
    
    /**
     * Clean up old conversation states
     */
    public void cleanupOldConversations(long maxAgeMs) {
        long now = System.currentTimeMillis();
        conversationStates.entrySet().removeIf(entry -> 
            (now - entry.getValue().getCreatedTime()) > maxAgeMs
        );
        logger.debug("🧠 Supervisor: Cleaned up old conversations");
    }
    
    // ============ Inner Classes ============
    
    /**
     * Tracks state for a single conversation
     */
    public static class ConversationState {
        private final String userId;
        private final String conversationId;
        private final long createdTime;
        private final List<BrainOutput> brainOutputs = Collections.synchronizedList(new ArrayList<>());
        private final AtomicInteger reevaluationCycles = new AtomicInteger(0);
        
        public ConversationState(String userId, String conversationId) {
            this.userId = userId;
            this.conversationId = conversationId;
            this.createdTime = System.currentTimeMillis();
        }
        
        public void recordBrainOutput(String brainName, String output, double quality) {
            brainOutputs.add(new BrainOutput(brainName, output, quality));
        }
        
        public List<BrainOutput> getBrainOutputs() {
            return new ArrayList<>(brainOutputs);
        }
        
        public void incrementReevaluationCycles() {
            reevaluationCycles.incrementAndGet();
        }
        
        public int getReevaluationCycles() {
            return reevaluationCycles.get();
        }
        
        public long getDuration() {
            return System.currentTimeMillis() - createdTime;
        }
        
        public long getCreatedTime() {
            return createdTime;
        }
    }
    
    /**
     * Represents output from a single brain
     */
    public static class BrainOutput {
        public final String brainName;
        public final String output;
        public final double quality;
        
        public BrainOutput(String brainName, String output, double quality) {
            this.brainName = brainName;
            this.output = output;
            this.quality = quality;
        }
    }
    
    /**
     * Result of merging multiple outputs
     */
    public static class MergedOutput {
        public final String content;
        public final double quality;
        
        public MergedOutput(String content, double quality) {
            this.content = content;
            this.quality = quality;
        }
    }
    
    /**
     * Tracks performance of a single brain
     */
    public static class BrainPerformance {
        private final String brainName;
        private final AtomicInteger executionCount = new AtomicInteger(0);
        private double totalQuality = 0.0;
        private double minQuality = 1.0;
        private double maxQuality = 0.0;
        
        public BrainPerformance(String brainName) {
            this.brainName = brainName;
        }
        
        public synchronized void recordExecution(double quality) {
            executionCount.incrementAndGet();
            totalQuality += quality;
            minQuality = Math.min(minQuality, quality);
            maxQuality = Math.max(maxQuality, quality);
        }
        
        public double getAverageQuality() {
            int count = executionCount.get();
            return count > 0 ? totalQuality / count : 0.0;
        }
        
        public int getExecutionCount() {
            return executionCount.get();
        }
        
        public double getMinQuality() {
            return minQuality;
        }
        
        public double getMaxQuality() {
            return maxQuality;
        }
        
        public String getBrainName() {
            return brainName;
        }
    }
    
    /**
     * Report on consistency across outputs
     */
    public static class ConsistencyReport {
        public final double averageSimilarity;
        public final List<String> inconsistencies;
        
        public ConsistencyReport(double averageSimilarity, List<String> inconsistencies) {
            this.averageSimilarity = averageSimilarity;
            this.inconsistencies = inconsistencies;
        }
        
        public boolean isConsistent() {
            return averageSimilarity >= 0.85 && inconsistencies.isEmpty();
        }
    }
}
