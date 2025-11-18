package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🧠 INCREMENTAL SUMMARIZER - Phase 8
 * 
 * Purpose: Only re-summarizes changed chunks, incremental summarization,
 * efficient processing.
 * 
 * Responsibilities:
 * - Summarize only changed chunks
 * - Track chunk summaries
 * - Detect chunk changes
 * - Provide summary cache
 * - Efficient re-summarization
 */
@Service
public class IncrementalSummarizer {
    
    private static final Logger logger = LoggerFactory.getLogger(IncrementalSummarizer.class);
    
    // Summary tracking
    private final Map<String, ChunkSummary> summaryCache = new ConcurrentHashMap<>();
    private final Map<String, String> chunkHashes = new ConcurrentHashMap<>();
    
    /**
     * Summarize changed chunks
     */
    public SummarizationResult summarizeChangedChunks(List<ChunkToSummarize> chunks) {
        SummarizationResult result = new SummarizationResult();
        
        logger.info("🧠 Incremental Summarizer: Checking {} chunks for changes", chunks.size());
        
        for (ChunkToSummarize chunk : chunks) {
            String chunkHash = calculateChunkHash(chunk.content);
            String cachedHash = chunkHashes.get(chunk.id);
            
            // Check if chunk changed
            if (cachedHash == null || !cachedHash.equals(chunkHash)) {
                try {
                    String summary = summarizeChunk(chunk);
                    
                    ChunkSummary chunkSummary = new ChunkSummary(
                        chunk.id,
                        chunk.content,
                        summary,
                        chunkHash
                    );
                    
                    summaryCache.put(chunk.id, chunkSummary);
                    chunkHashes.put(chunk.id, chunkHash);
                    
                    result.summarizedChunks++;
                    result.totalSummaryLength += summary.length();
                } catch (Exception e) {
                    logger.error("🧠 Incremental Summarizer: Error summarizing chunk {}: {}", 
                        chunk.id, e.getMessage());
                    result.errors++;
                }
            } else {
                result.cachedChunks++;
            }
        }
        
        result.totalChunks = chunks.size();
        result.duration = System.currentTimeMillis();
        
        logger.info("🧠 Incremental Summarizer: Summarized {} chunks, cached {}, {} errors in {}ms", 
            result.summarizedChunks, result.cachedChunks, result.errors, result.duration);
        
        return result;
    }
    
    /**
     * Summarize single chunk
     */
    private String summarizeChunk(ChunkToSummarize chunk) {
        String content = chunk.content;
        
        // Extract key information
        StringBuilder summary = new StringBuilder();
        
        // Get first 100 words as summary
        String[] words = content.split("\\s+");
        int wordCount = Math.min(100, words.length);
        
        for (int i = 0; i < wordCount; i++) {
            summary.append(words[i]).append(" ");
        }
        
        if (words.length > 100) {
            summary.append("...");
        }
        
        return summary.toString().trim();
    }
    
    /**
     * Calculate chunk hash
     */
    private String calculateChunkHash(String content) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(content.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            logger.debug("Error calculating chunk hash: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * Get chunk summary
     */
    public String getChunkSummary(String chunkId) {
        ChunkSummary summary = summaryCache.get(chunkId);
        return summary != null ? summary.summary : null;
    }
    
    /**
     * Get all summaries
     */
    public Map<String, String> getAllSummaries() {
        Map<String, String> summaries = new HashMap<>();
        for (Map.Entry<String, ChunkSummary> entry : summaryCache.entrySet()) {
            summaries.put(entry.getKey(), entry.getValue().summary);
        }
        return summaries;
    }
    
    /**
     * Clear summary cache
     */
    public void clearCache() {
        summaryCache.clear();
        chunkHashes.clear();
        logger.info("🧠 Incremental Summarizer: Cleared summary cache");
    }
    
    /**
     * Get summarization statistics
     */
    public SummarizationStatistics getStatistics() {
        return new SummarizationStatistics(
            summaryCache.size(),
            summaryCache.values().stream()
                .mapToInt(s -> s.summary.length())
                .sum()
        );
    }
    
    // ============ Inner Classes ============
    
    /**
     * Chunk to summarize
     */
    public static class ChunkToSummarize {
        public final String id;
        public final String content;
        public final String source;
        
        public ChunkToSummarize(String id, String content, String source) {
            this.id = id;
            this.content = content;
            this.source = source;
        }
    }
    
    /**
     * Chunk summary
     */
    public static class ChunkSummary {
        public final String chunkId;
        public final String originalContent;
        public final String summary;
        public final String contentHash;
        public final long timestamp;
        
        public ChunkSummary(String chunkId, String originalContent, String summary, String contentHash) {
            this.chunkId = chunkId;
            this.originalContent = originalContent;
            this.summary = summary;
            this.contentHash = contentHash;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Summarization result
     */
    public static class SummarizationResult {
        public int totalChunks = 0;
        public int summarizedChunks = 0;
        public int cachedChunks = 0;
        public int errors = 0;
        public int totalSummaryLength = 0;
        public long duration = 0;
        
        public double getEfficiency() {
            return totalChunks > 0 ? (double) cachedChunks / totalChunks * 100 : 0;
        }
    }
    
    /**
     * Summarization statistics
     */
    public static class SummarizationStatistics {
        public final int totalSummaries;
        public final int totalSummaryLength;
        
        public SummarizationStatistics(int totalSummaries, int totalSummaryLength) {
            this.totalSummaries = totalSummaries;
            this.totalSummaryLength = totalSummaryLength;
        }
    }
}
