package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🧠 FILE HASH TRACKER - Phase 8
 * 
 * Purpose: Tracks file hashes, detects file changes, stores hash history,
 * manages hash cache for incremental indexing.
 * 
 * Responsibilities:
 * - Calculate file hash (MD5)
 * - Track file hashes over time
 * - Detect file changes
 * - Store hash history
 * - Manage hash cache
 * - Provide change detection
 */
@Service
public class FileHashTracker {
    
    private static final Logger logger = LoggerFactory.getLogger(FileHashTracker.class);
    
    // Hash tracking
    private final Map<String, FileHashRecord> fileHashes = new ConcurrentHashMap<>();
    private final Map<String, List<HashHistoryEntry>> hashHistory = new ConcurrentHashMap<>();
    
    /**
     * Calculate MD5 hash of file
     */
    public String calculateFileHash(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                logger.warn("🧠 File Hash Tracker: File not found: {}", filePath);
                return null;
            }
            
            byte[] fileBytes = Files.readAllBytes(path);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(fileBytes);
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            logger.error("🧠 File Hash Tracker: Error calculating hash for {}: {}", filePath, e.getMessage());
            return null;
        }
    }
    
    /**
     * Track file hash
     */
    public void trackFileHash(String filePath) {
        String hash = calculateFileHash(filePath);
        if (hash == null) {
            return;
        }
        
        FileHashRecord record = new FileHashRecord(filePath, hash);
        fileHashes.put(filePath, record);
        
        // Add to history
        hashHistory.computeIfAbsent(filePath, k -> Collections.synchronizedList(new ArrayList<>()))
            .add(new HashHistoryEntry(hash, System.currentTimeMillis()));
        
        logger.debug("🧠 File Hash Tracker: Tracked hash for {}: {}", filePath, hash);
    }
    
    /**
     * Check if file has changed
     */
    public boolean hasFileChanged(String filePath) {
        String currentHash = calculateFileHash(filePath);
        if (currentHash == null) {
            return false;
        }
        
        FileHashRecord record = fileHashes.get(filePath);
        if (record == null) {
            // File not tracked before, so it's new
            trackFileHash(filePath);
            return true;
        }
        
        boolean changed = !record.hash.equals(currentHash);
        
        if (changed) {
            logger.info("🧠 File Hash Tracker: File changed: {} (old: {}, new: {})", 
                filePath, record.hash, currentHash);
            trackFileHash(filePath);
        }
        
        return changed;
    }
    
    /**
     * Get file hash
     */
    public String getFileHash(String filePath) {
        FileHashRecord record = fileHashes.get(filePath);
        return record != null ? record.hash : null;
    }
    
    /**
     * Get previous file hash
     */
    public String getPreviousFileHash(String filePath) {
        List<HashHistoryEntry> history = hashHistory.get(filePath);
        if (history == null || history.size() < 2) {
            return null;
        }
        
        // Return second-to-last hash
        return history.get(history.size() - 2).hash;
    }
    
    /**
     * Get hash history for file
     */
    public List<HashHistoryEntry> getHashHistory(String filePath) {
        List<HashHistoryEntry> history = hashHistory.get(filePath);
        return history != null ? new ArrayList<>(history) : new ArrayList<>();
    }
    
    /**
     * Check if file is new (not tracked before)
     */
    public boolean isNewFile(String filePath) {
        return !fileHashes.containsKey(filePath);
    }
    
    /**
     * Get all tracked files
     */
    public Set<String> getAllTrackedFiles() {
        return new HashSet<>(fileHashes.keySet());
    }
    
    /**
     * Get changed files
     */
    public List<String> getChangedFiles(List<String> filePaths) {
        List<String> changedFiles = new ArrayList<>();
        
        for (String filePath : filePaths) {
            if (hasFileChanged(filePath)) {
                changedFiles.add(filePath);
            }
        }
        
        logger.info("🧠 File Hash Tracker: Found {} changed files out of {}", 
            changedFiles.size(), filePaths.size());
        
        return changedFiles;
    }
    
    /**
     * Get new files
     */
    public List<String> getNewFiles(List<String> filePaths) {
        List<String> newFiles = new ArrayList<>();
        
        for (String filePath : filePaths) {
            if (isNewFile(filePath)) {
                newFiles.add(filePath);
                trackFileHash(filePath);
            }
        }
        
        logger.info("🧠 File Hash Tracker: Found {} new files out of {}", 
            newFiles.size(), filePaths.size());
        
        return newFiles;
    }
    
    /**
     * Clear hash cache
     */
    public void clearHashCache() {
        fileHashes.clear();
        hashHistory.clear();
        logger.info("🧠 File Hash Tracker: Cleared hash cache");
    }
    
    /**
     * Get tracker statistics
     */
    public TrackerStatistics getStatistics() {
        return new TrackerStatistics(
            fileHashes.size(),
            hashHistory.size(),
            hashHistory.values().stream().mapToInt(List::size).sum()
        );
    }
    
    // ============ Inner Classes ============
    
    /**
     * File hash record
     */
    public static class FileHashRecord {
        public final String filePath;
        public final String hash;
        public final long timestamp;
        
        public FileHashRecord(String filePath, String hash) {
            this.filePath = filePath;
            this.hash = hash;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Hash history entry
     */
    public static class HashHistoryEntry {
        public final String hash;
        public final long timestamp;
        
        public HashHistoryEntry(String hash, long timestamp) {
            this.hash = hash;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * Tracker statistics
     */
    public static class TrackerStatistics {
        public final int trackedFiles;
        public final int filesWithHistory;
        public final int totalHistoryEntries;
        
        public TrackerStatistics(int trackedFiles, int filesWithHistory, int totalHistoryEntries) {
            this.trackedFiles = trackedFiles;
            this.filesWithHistory = filesWithHistory;
            this.totalHistoryEntries = totalHistoryEntries;
        }
    }
}
