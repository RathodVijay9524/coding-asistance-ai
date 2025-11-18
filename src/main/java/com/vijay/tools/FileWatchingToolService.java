package com.vijay.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.manager.AiToolProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.StandardWatchEventKinds;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 👁️ File Watching Tool Service
 * 
 * Monitors file changes in real-time including:
 * - Start file watching
 * - Stop file watching
 * - Get watcher status
 * - Analyze file changes
 * 
 * Implements AiToolProvider to be accessible from chatbot
 */
@Service
@RequiredArgsConstructor
public class FileWatchingToolService implements AiToolProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(FileWatchingToolService.class);
    private final ObjectMapper objectMapper;
    
    // Store active watchers
    private static final Map<String, WatcherSession> activeSessions = new ConcurrentHashMap<>();
    
    /**
     * Start file watching
     */
    @Tool(description = "Start monitoring files for changes in real-time")
    public String startWatching(
            @ToolParam(description = "Project path to watch") String projectPath,
            @ToolParam(description = "File pattern to watch (e.g., *.java)") String filePattern) {
        
        logger.info("👁️ Starting file watching for: {}", projectPath);
        
        try {
            String sessionId = "watch_" + System.currentTimeMillis();
            
            // Create watcher session
            WatcherSession session = new WatcherSession();
            session.setSessionId(sessionId);
            session.setProjectPath(projectPath);
            session.setFilePattern(filePattern != null ? filePattern : "*.java");
            session.setStartTime(LocalDateTime.now());
            session.setActive(true);
            
            activeSessions.put(sessionId, session);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("sessionId", sessionId);
            result.put("projectPath", projectPath);
            result.put("filePattern", session.getFilePattern());
            result.put("startTime", session.getStartTime().toString());
            result.put("message", "✅ File watching started successfully");
            
            logger.info("✅ File watching started with session: {}", sessionId);
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ File watching start failed: {}", e.getMessage(), e);
            return errorResponse("File watching start failed: " + e.getMessage());
        }
    }
    
    /**
     * Stop file watching
     */
    @Tool(description = "Stop monitoring files")
    public String stopWatching(
            @ToolParam(description = "Session ID to stop") String sessionId) {
        
        logger.info("⏹️ Stopping file watching for session: {}", sessionId);
        
        try {
            WatcherSession session = activeSessions.get(sessionId);
            
            if (session == null) {
                return errorResponse("Session not found: " + sessionId);
            }
            
            session.setActive(false);
            session.setEndTime(LocalDateTime.now());
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("sessionId", sessionId);
            result.put("endTime", session.getEndTime().toString());
            result.put("filesChanged", session.getChangedFiles().size());
            result.put("message", "✅ File watching stopped successfully");
            
            logger.info("✅ File watching stopped for session: {}", sessionId);
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ File watching stop failed: {}", e.getMessage(), e);
            return errorResponse("File watching stop failed: " + e.getMessage());
        }
    }
    
    /**
     * Get watcher status
     */
    @Tool(description = "Get status of file watcher")
    public String getWatcherStatus(
            @ToolParam(description = "Session ID") String sessionId) {
        
        logger.info("📊 Getting watcher status for session: {}", sessionId);
        
        try {
            WatcherSession session = activeSessions.get(sessionId);
            
            if (session == null) {
                return errorResponse("Session not found: " + sessionId);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("sessionId", sessionId);
            result.put("active", session.isActive());
            result.put("projectPath", session.getProjectPath());
            result.put("filePattern", session.getFilePattern());
            result.put("startTime", session.getStartTime().toString());
            result.put("filesChanged", session.getChangedFiles().size());
            result.put("recentChanges", session.getChangedFiles().stream().limit(5).toList());
            
            logger.info("✅ Watcher status retrieved");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Failed to get watcher status: {}", e.getMessage(), e);
            return errorResponse("Failed to get watcher status: " + e.getMessage());
        }
    }
    
    /**
     * Analyze file changes
     */
    @Tool(description = "Analyze specific file changes")
    public String analyzeFileChange(
            @ToolParam(description = "Session ID") String sessionId,
            @ToolParam(description = "File path to analyze") String filePath) {
        
        logger.info("🔍 Analyzing file change: {}", filePath);
        
        try {
            WatcherSession session = activeSessions.get(sessionId);
            
            if (session == null) {
                return errorResponse("Session not found: " + sessionId);
            }
            
            File file = new File(filePath);
            Map<String, Object> analysis = new HashMap<>();
            
            if (file.exists()) {
                analysis.put("fileName", file.getName());
                analysis.put("filePath", filePath);
                analysis.put("fileSize", file.length());
                analysis.put("lastModified", file.lastModified());
                analysis.put("isFile", file.isFile());
                analysis.put("isDirectory", file.isDirectory());
                
                // Analyze file content if it's a Java file
                if (filePath.endsWith(".java")) {
                    String content = Files.readString(Paths.get(filePath));
                    analysis.put("lineCount", content.split("\n").length);
                    analysis.put("hasErrors", content.contains("TODO") || content.contains("FIXME"));
                    analysis.put("complexity", estimateComplexity(content));
                }
            } else {
                analysis.put("status", "file_deleted");
                analysis.put("filePath", filePath);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("sessionId", sessionId);
            result.put("analysis", analysis);
            result.put("timestamp", LocalDateTime.now().toString());
            
            logger.info("✅ File change analysis complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ File change analysis failed: {}", e.getMessage(), e);
            return errorResponse("File change analysis failed: " + e.getMessage());
        }
    }
    
    /**
     * Estimate code complexity
     */
    private String estimateComplexity(String content) {
        int methodCount = content.split("public|private|protected").length;
        int lineCount = content.split("\n").length;
        
        if (methodCount > 20 || lineCount > 500) {
            return "HIGH";
        } else if (methodCount > 10 || lineCount > 250) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
    
    /**
     * Get all active sessions
     */
    @Tool(description = "Get all active file watching sessions")
    public String getActiveSessions() {
        
        logger.info("📋 Getting all active sessions");
        
        try {
            List<Map<String, Object>> sessions = new ArrayList<>();
            
            for (WatcherSession session : activeSessions.values()) {
                Map<String, Object> sessionInfo = new HashMap<>();
                sessionInfo.put("sessionId", session.getSessionId());
                sessionInfo.put("active", session.isActive());
                sessionInfo.put("projectPath", session.getProjectPath());
                sessionInfo.put("filesChanged", session.getChangedFiles().size());
                sessions.add(sessionInfo);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalSessions", sessions.size());
            result.put("sessions", sessions);
            
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Failed to get active sessions: {}", e.getMessage(), e);
            return errorResponse("Failed to get active sessions: " + e.getMessage());
        }
    }
    
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.error("❌ JSON serialization failed: {}", e.getMessage());
            return "{\"error\": \"JSON serialization failed\"}";
        }
    }
    
    private String errorResponse(String message) {
        return "{\"error\": \"" + message.replace("\"", "\\\"") + "\"}";
    }
    
    /**
     * Watcher session class
     */
    private static class WatcherSession {
        private String sessionId;
        private String projectPath;
        private String filePattern;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private boolean active;
        private List<String> changedFiles = new ArrayList<>();
        
        // Getters and setters
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        
        public String getProjectPath() { return projectPath; }
        public void setProjectPath(String projectPath) { this.projectPath = projectPath; }
        
        public String getFilePattern() { return filePattern; }
        public void setFilePattern(String filePattern) { this.filePattern = filePattern; }
        
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        
        public List<String> getChangedFiles() { return changedFiles; }
        public void setChangedFiles(List<String> changedFiles) { this.changedFiles = changedFiles; }
    }
}
