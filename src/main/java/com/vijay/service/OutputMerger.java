package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 🧠 OUTPUT MERGER - Phase 7
 * 
 * Purpose: Merges outputs from multiple brains, resolves conflicts,
 * combines insights, and creates unified response.
 * 
 * Responsibilities:
 * - Merge outputs from multiple sources
 * - Resolve conflicts between outputs
 * - Combine insights intelligently
 * - Create unified response
 * - Maintain quality and coherence
 */
@Service
public class OutputMerger {
    
    private static final Logger logger = LoggerFactory.getLogger(OutputMerger.class);
    
    /**
     * Merge multiple outputs into unified response
     */
    public MergedResponse mergeOutputs(List<BrainOutput> outputs) {
        if (outputs == null || outputs.isEmpty()) {
            logger.warn("🧠 Output Merger: No outputs to merge");
            return new MergedResponse("", 0.0, new ArrayList<>());
        }
        
        // Sort by quality (highest first)
        List<BrainOutput> sortedOutputs = outputs.stream()
            .sorted((a, b) -> Double.compare(b.quality, a.quality))
            .collect(Collectors.toList());
        
        // Merge strategy: combine top outputs
        StringBuilder mergedContent = new StringBuilder();
        List<String> sources = new ArrayList<>();
        double totalQuality = 0.0;
        
        // Add primary output (highest quality)
        if (!sortedOutputs.isEmpty()) {
            BrainOutput primary = sortedOutputs.get(0);
            mergedContent.append(primary.content);
            sources.add(primary.source);
            totalQuality += primary.quality;
        }
        
        // Add complementary insights from other outputs
        for (int i = 1; i < Math.min(3, sortedOutputs.size()); i++) {
            BrainOutput secondary = sortedOutputs.get(i);
            
            // Check if content is significantly different
            if (!isSimilar(sortedOutputs.get(0).content, secondary.content)) {
                mergedContent.append("\n\n");
                mergedContent.append("Additional insight: ").append(secondary.content);
                sources.add(secondary.source);
                totalQuality += secondary.quality;
            }
        }
        
        double averageQuality = totalQuality / Math.min(3, sortedOutputs.size());
        
        logger.info("🧠 Output Merger: Merged {} outputs (avg quality: {:.2f})", 
            Math.min(3, sortedOutputs.size()), averageQuality);
        
        return new MergedResponse(mergedContent.toString(), averageQuality, sources);
    }
    
    /**
     * Merge outputs with conflict resolution
     */
    public MergedResponse mergeWithConflictResolution(List<BrainOutput> outputs) {
        if (outputs == null || outputs.isEmpty()) {
            return new MergedResponse("", 0.0, new ArrayList<>());
        }
        
        // Identify conflicts
        List<Conflict> conflicts = identifyConflicts(outputs);
        
        if (!conflicts.isEmpty()) {
            logger.info("🧠 Output Merger: Found {} conflicts, resolving...", conflicts.size());
            
            // Resolve conflicts
            for (Conflict conflict : conflicts) {
                resolveConflict(conflict, outputs);
            }
        }
        
        // Merge resolved outputs
        return mergeOutputs(outputs);
    }
    
    /**
     * Identify conflicts between outputs
     */
    private List<Conflict> identifyConflicts(List<BrainOutput> outputs) {
        List<Conflict> conflicts = new ArrayList<>();
        
        for (int i = 0; i < outputs.size(); i++) {
            for (int j = i + 1; j < outputs.size(); j++) {
                BrainOutput output1 = outputs.get(i);
                BrainOutput output2 = outputs.get(j);
                
                if (isConflicting(output1.content, output2.content)) {
                    conflicts.add(new Conflict(output1, output2));
                }
            }
        }
        
        return conflicts;
    }
    
    /**
     * Check if two outputs are conflicting
     */
    private boolean isConflicting(String content1, String content2) {
        String lower1 = content1.toLowerCase();
        String lower2 = content2.toLowerCase();
        
        // Check for direct contradictions
        if (lower1.contains("yes") && lower2.contains("no")) {
            return true;
        }
        
        if (lower1.contains("always") && lower2.contains("never")) {
            return true;
        }
        
        if (lower1.contains("must") && lower2.contains("must not")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Resolve conflict between two outputs
     */
    private void resolveConflict(Conflict conflict, List<BrainOutput> outputs) {
        // Resolution strategy: prefer higher quality output
        if (conflict.output1.quality > conflict.output2.quality) {
            logger.info("🧠 Output Merger: Resolved conflict - preferring output1 (quality: {:.2f})", 
                conflict.output1.quality);
        } else {
            logger.info("🧠 Output Merger: Resolved conflict - preferring output2 (quality: {:.2f})", 
                conflict.output2.quality);
        }
    }
    
    /**
     * Check if two outputs are similar
     */
    private boolean isSimilar(String content1, String content2) {
        if (content1 == null || content2 == null) {
            return false;
        }
        
        String[] words1 = content1.toLowerCase().split("\\s+");
        String[] words2 = content2.toLowerCase().split("\\s+");
        
        Set<String> set1 = new HashSet<>(Arrays.asList(words1));
        Set<String> set2 = new HashSet<>(Arrays.asList(words2));
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        double similarity = union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
        
        return similarity > 0.6; // 60% similarity threshold
    }
    
    /**
     * Combine insights from multiple outputs
     */
    public String combineInsights(List<BrainOutput> outputs) {
        if (outputs == null || outputs.isEmpty()) {
            return "";
        }
        
        StringBuilder combined = new StringBuilder();
        
        // Add main insight
        if (!outputs.isEmpty()) {
            combined.append(outputs.get(0).content);
        }
        
        // Add complementary insights
        for (int i = 1; i < outputs.size(); i++) {
            BrainOutput output = outputs.get(i);
            
            if (!isSimilar(outputs.get(0).content, output.content)) {
                combined.append("\n\n");
                combined.append("Additional perspective: ");
                combined.append(output.content);
            }
        }
        
        logger.info("🧠 Output Merger: Combined {} insights", outputs.size());
        
        return combined.toString();
    }
    
    /**
     * Create unified response with metadata
     */
    public UnifiedResponse createUnifiedResponse(List<BrainOutput> outputs, String userId) {
        MergedResponse merged = mergeWithConflictResolution(outputs);
        
        UnifiedResponse response = new UnifiedResponse(
            userId,
            merged.content,
            merged.quality,
            merged.sources,
            System.currentTimeMillis()
        );
        
        logger.info("🧠 Output Merger: Created unified response (quality: {:.2f}, sources: {})", 
            merged.quality, merged.sources.size());
        
        return response;
    }
    
    /**
     * Log merger statistics
     */
    public void logMergerStatistics(List<BrainOutput> outputs) {
        if (outputs == null || outputs.isEmpty()) {
            return;
        }
        
        double avgQuality = outputs.stream()
            .mapToDouble(o -> o.quality)
            .average()
            .orElse(0.0);
        
        double maxQuality = outputs.stream()
            .mapToDouble(o -> o.quality)
            .max()
            .orElse(0.0);
        
        double minQuality = outputs.stream()
            .mapToDouble(o -> o.quality)
            .min()
            .orElse(0.0);
        
        logger.info("🧠 Output Merger Statistics:");
        logger.info("   📊 Outputs: {}", outputs.size());
        logger.info("   📈 Avg Quality: {:.2f}", avgQuality);
        logger.info("   ⬆️ Max Quality: {:.2f}", maxQuality);
        logger.info("   ⬇️ Min Quality: {:.2f}", minQuality);
    }
    
    // ============ Inner Classes ============
    
    /**
     * Brain output to merge
     */
    public static class BrainOutput {
        public final String source;
        public final String content;
        public final double quality;
        
        public BrainOutput(String source, String content, double quality) {
            this.source = source;
            this.content = content;
            this.quality = quality;
        }
    }
    
    /**
     * Merged response
     */
    public static class MergedResponse {
        public final String content;
        public final double quality;
        public final List<String> sources;
        
        public MergedResponse(String content, double quality, List<String> sources) {
            this.content = content;
            this.quality = quality;
            this.sources = sources;
        }
    }
    
    /**
     * Unified response with metadata
     */
    public static class UnifiedResponse {
        public final String userId;
        public final String content;
        public final double quality;
        public final List<String> sources;
        public final long timestamp;
        
        public UnifiedResponse(String userId, String content, double quality, 
                              List<String> sources, long timestamp) {
            this.userId = userId;
            this.content = content;
            this.quality = quality;
            this.sources = sources;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * Conflict between outputs
     */
    private static class Conflict {
        final BrainOutput output1;
        final BrainOutput output2;
        
        Conflict(BrainOutput output1, BrainOutput output2) {
            this.output1 = output1;
            this.output2 = output2;
        }
    }
}
