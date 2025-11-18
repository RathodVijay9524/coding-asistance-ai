package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * 🔍 Real-time File Watcher Service
 * 
 * Monitors source code changes and automatically reindexes:
 * - Detects new/modified/deleted files
 * - Triggers incremental reindexing
 * - Updates embeddings in real-time
 * - Maintains cache consistency
 * - Zero downtime updates
 */
@Service
public class FileWatcherService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileWatcherService.class);
    
    @Autowired(required = false)
    private CodeChunkIndexer codeChunkIndexer;
    
    @Autowired(required = false)
    private CodeSummaryIndexer codeSummaryIndexer;
    
    @Autowired(required = false)
    private EmbeddingCacheManager cacheManager;
    
    private WatchService watchService;
    private ExecutorService executorService;
    private Map<WatchKey, Path> keys = new ConcurrentHashMap<>();
    private volatile boolean running = false;
    
    // Debounce rapid file changes
    private final Map<String, Long> lastModifiedTime = new ConcurrentHashMap<>();
    private static final long DEBOUNCE_DELAY_MS = 1000; // 1 second
    
    // Track file changes
    private final Set<String> modifiedFiles = ConcurrentHashMap.newKeySet();
    private final Set<String> newFiles = ConcurrentHashMap.newKeySet();
    private final Set<String> deletedFiles = ConcurrentHashMap.newKeySet();
    
    public FileWatcherService() {
        logger.info("🔍 File Watcher Service initialized");
    }
    
    /**
     * Start watching source files for changes
     */
    @PostConstruct
    public void startWatching() {
        logger.info("🔍 Starting real-time file watcher...");
        
        try {
            watchService = FileSystems.getDefault().newWatchService();
            executorService = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "FileWatcher");
                t.setDaemon(true);
                return t;
            });
            
            // Register source directories
            Path srcPath = Paths.get("src/main/java");
            if (Files.exists(srcPath)) {
                registerDirectory(srcPath);
                logger.info("✅ Registered watch on: {}", srcPath);
            }
            
            running = true;
            executorService.submit(this::watchLoop);
            logger.info("✅ File watcher started successfully");
            
        } catch (IOException e) {
            logger.error("❌ Failed to start file watcher: {}", e.getMessage());
        }
    }
    
    /**
     * Register directory for watching (recursive)
     */
    private void registerDirectory(Path dir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                if (Files.isDirectory(path) && !path.getFileName().toString().startsWith(".")) {
                    WatchKey key = path.register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE);
                    keys.put(key, path);
                    registerDirectory(path); // Recursive registration
                }
            }
        }
    }
    
    /**
     * Main watch loop
     */
    private void watchLoop() {
        logger.info("🔄 File watch loop started");
        
        while (running) {
            try {
                WatchKey key = watchService.poll(1, TimeUnit.SECONDS);
                if (key == null) continue;
                
                Path dir = keys.get(key);
                if (dir == null) continue;
                
                // Process events
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path filename = (Path) event.context();
                    Path fullPath = dir.resolve(filename);
                    
                    // Skip non-Java files and test files
                    if (!fullPath.toString().endsWith(".java") || fullPath.toString().contains("test")) {
                        continue;
                    }
                    
                    // Handle different event types
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        handleFileCreated(fullPath);
                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        handleFileModified(fullPath);
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        handleFileDeleted(fullPath);
                    }
                }
                
                // Reset key
                if (!key.reset()) {
                    keys.remove(key);
                }
                
            } catch (InterruptedException e) {
                logger.debug("File watch loop interrupted");
                break;
            } catch (Exception e) {
                logger.error("❌ Error in watch loop: {}", e.getMessage());
            }
        }
        
        logger.info("🛑 File watch loop stopped");
    }
    
    /**
     * Handle file creation
     */
    private void handleFileCreated(Path filePath) {
        String fileName = filePath.toString();
        logger.info("📄 New file detected: {}", fileName);
        
        // Debounce
        if (shouldDebounce(fileName)) {
            logger.debug("⏳ Debouncing file creation: {}", fileName);
            return;
        }
        
        newFiles.add(fileName);
        modifiedFiles.remove(fileName);
        
        logger.info("✨ File created - will reindex: {}", fileName);
        scheduleReindex();
    }
    
    /**
     * Handle file modification
     */
    private void handleFileModified(Path filePath) {
        String fileName = filePath.toString();
        logger.debug("✏️ File modified: {}", fileName);
        
        // Debounce rapid changes
        if (shouldDebounce(fileName)) {
            logger.debug("⏳ Debouncing file modification: {}", fileName);
            return;
        }
        
        modifiedFiles.add(fileName);
        newFiles.remove(fileName);
        
        logger.info("🔄 File modified - will reindex: {}", fileName);
        scheduleReindex();
    }
    
    /**
     * Handle file deletion
     */
    private void handleFileDeleted(Path filePath) {
        String fileName = filePath.toString();
        logger.info("🗑️ File deleted: {}", fileName);
        
        deletedFiles.add(fileName);
        modifiedFiles.remove(fileName);
        newFiles.remove(fileName);
        
        logger.info("🗑️ File deleted - will reindex: {}", fileName);
        scheduleReindex();
    }
    
    /**
     * Check if file change should be debounced
     */
    private boolean shouldDebounce(String fileName) {
        Long lastTime = lastModifiedTime.get(fileName);
        long now = System.currentTimeMillis();
        
        if (lastTime != null && (now - lastTime) < DEBOUNCE_DELAY_MS) {
            return true;
        }
        
        lastModifiedTime.put(fileName, now);
        return false;
    }
    
    /**
     * Schedule reindexing (batched)
     */
    private void scheduleReindex() {
        // Wait for file changes to settle
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Batch reindex
        if (!modifiedFiles.isEmpty() || !newFiles.isEmpty() || !deletedFiles.isEmpty()) {
            performReindex();
        }
    }
    
    /**
     * Perform incremental reindexing
     */
    private void performReindex() {
        logger.info("🔄 Performing incremental reindex...");
        logger.info("   📝 Modified: {} files", modifiedFiles.size());
        logger.info("   ✨ New: {} files", newFiles.size());
        logger.info("   🗑️ Deleted: {} files", deletedFiles.size());
        
        try {
            // Clear cache to force reindexing
            if (cacheManager != null) {
                logger.info("💾 Clearing cache for reindex...");
                cacheManager.clearCache();
            }
            
            // Trigger reindexing
            if (codeChunkIndexer != null) {
                logger.info("🧩 Reindexing code chunks...");
                codeChunkIndexer.indexCodeChunks();
            }
            
            if (codeSummaryIndexer != null) {
                logger.info("📚 Reindexing code summaries...");
                codeSummaryIndexer.indexCodeSummaries();
            }
            
            // Clear tracking
            modifiedFiles.clear();
            newFiles.clear();
            deletedFiles.clear();
            
            logger.info("✅ Incremental reindex completed successfully");
            
        } catch (Exception e) {
            logger.error("❌ Error during reindex: {}", e.getMessage());
        }
    }
    
    /**
     * Get file change statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("watching", running);
        stats.put("modifiedFiles", modifiedFiles.size());
        stats.put("newFiles", newFiles.size());
        stats.put("deletedFiles", deletedFiles.size());
        stats.put("totalChanges", modifiedFiles.size() + newFiles.size() + deletedFiles.size());
        stats.put("debounceDelayMs", DEBOUNCE_DELAY_MS);
        return stats;
    }
    
    /**
     * Get list of modified files
     */
    public Set<String> getModifiedFiles() {
        return new HashSet<>(modifiedFiles);
    }
    
    /**
     * Get list of new files
     */
    public Set<String> getNewFiles() {
        return new HashSet<>(newFiles);
    }
    
    /**
     * Get list of deleted files
     */
    public Set<String> getDeletedFiles() {
        return new HashSet<>(deletedFiles);
    }
    
    /**
     * Force immediate reindex
     */
    public void forceReindex() {
        logger.info("⚡ Force reindex requested");
        performReindex();
    }
    
    /**
     * Stop watching
     */
    @PreDestroy
    public void stopWatching() {
        logger.info("🛑 Stopping file watcher...");
        running = false;
        
        try {
            if (watchService != null) {
                watchService.close();
            }
            if (executorService != null) {
                executorService.shutdown();
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            }
            logger.info("✅ File watcher stopped");
        } catch (Exception e) {
            logger.error("❌ Error stopping file watcher: {}", e.getMessage());
        }
    }
    
    /**
     * Check if watcher is running
     */
    public boolean isRunning() {
        return running;
    }
}
