package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🧠 TIMELINE VISUALIZER - Phase 10
 * 
 * Purpose: Create timeline visualization of advisor execution
 * 
 * Responsibilities:
 * - Track advisor execution times
 * - Generate timeline data
 * - Export to JSON/CSV
 * - Provide visualization
 */
@Service
public class TimelineVisualizer {
    
    private static final Logger logger = LoggerFactory.getLogger(TimelineVisualizer.class);
    
    // Timeline tracking
    private final Map<String, TimelineEvent> activeAdvisors = new ConcurrentHashMap<>();
    private final List<TimelineEvent> completedEvents = Collections.synchronizedList(new ArrayList<>());
    private long startTime = 0;
    
    /**
     * Start advisor execution
     */
    public void startAdvisor(String advisorName, int order) {
        long currentTime = System.currentTimeMillis();
        if (startTime == 0) {
            startTime = currentTime;
        }
        
        TimelineEvent event = new TimelineEvent(advisorName, order, currentTime - startTime);
        activeAdvisors.put(advisorName, event);
        
        logger.debug("⏱️ Timeline: Started {} at {}ms", advisorName, event.startMs);
    }
    
    /**
     * End advisor execution
     */
    public void endAdvisor(String advisorName) {
        TimelineEvent event = activeAdvisors.remove(advisorName);
        if (event != null) {
            long currentTime = System.currentTimeMillis();
            event.endMs = currentTime - startTime;
            event.durationMs = event.endMs - event.startMs;
            
            completedEvents.add(event);
            
            logger.debug("⏱️ Timeline: Ended {} after {}ms", advisorName, event.durationMs);
        }
    }
    
    /**
     * Get timeline events
     */
    public List<TimelineEvent> getTimeline() {
        return new ArrayList<>(completedEvents);
    }
    
    /**
     * Export to JSON
     */
    public String exportToJSON() {
        StringBuilder json = new StringBuilder("[\n");
        
        for (int i = 0; i < completedEvents.size(); i++) {
            TimelineEvent event = completedEvents.get(i);
            json.append("  {\n");
            json.append("    \"advisor\": \"").append(event.advisorName).append("\",\n");
            json.append("    \"order\": ").append(event.order).append(",\n");
            json.append("    \"start_ms\": ").append(event.startMs).append(",\n");
            json.append("    \"end_ms\": ").append(event.endMs).append(",\n");
            json.append("    \"duration_ms\": ").append(event.durationMs).append("\n");
            json.append("  }");
            
            if (i < completedEvents.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("]");
        return json.toString();
    }
    
    /**
     * Export to CSV
     */
    public String exportToCSV() {
        StringBuilder csv = new StringBuilder("advisor,order,start_ms,end_ms,duration_ms\n");
        
        for (TimelineEvent event : completedEvents) {
            csv.append(event.advisorName).append(",");
            csv.append(event.order).append(",");
            csv.append(event.startMs).append(",");
            csv.append(event.endMs).append(",");
            csv.append(event.durationMs).append("\n");
        }
        
        return csv.toString();
    }
    
    /**
     * Reset timeline
     */
    public void reset() {
        activeAdvisors.clear();
        completedEvents.clear();
        startTime = 0;
        logger.info("🔄 Timeline reset");
    }
    
    /**
     * Get total duration
     */
    public long getTotalDuration() {
        if (completedEvents.isEmpty()) {
            return 0;
        }
        
        long maxEnd = completedEvents.stream()
            .mapToLong(e -> e.endMs)
            .max()
            .orElse(0);
        
        return maxEnd;
    }
    
    /**
     * Get timeline statistics
     */
    public TimelineStatistics getStatistics() {
        long totalDuration = getTotalDuration();
        double avgDuration = completedEvents.stream()
            .mapToLong(e -> e.durationMs)
            .average()
            .orElse(0);
        
        long maxDuration = completedEvents.stream()
            .mapToLong(e -> e.durationMs)
            .max()
            .orElse(0);
        
        return new TimelineStatistics(
            completedEvents.size(),
            totalDuration,
            avgDuration,
            maxDuration
        );
    }
    
    // ============ Inner Classes ============
    
    /**
     * Timeline event
     */
    public static class TimelineEvent {
        public final String advisorName;
        public final int order;
        public final long startMs;
        public long endMs;
        public long durationMs;
        
        public TimelineEvent(String advisorName, int order, long startMs) {
            this.advisorName = advisorName;
            this.order = order;
            this.startMs = startMs;
            this.endMs = 0;
            this.durationMs = 0;
        }
    }
    
    /**
     * Timeline statistics
     */
    public static class TimelineStatistics {
        public final int eventCount;
        public final long totalDuration;
        public final double averageDuration;
        public final long maxDuration;
        
        public TimelineStatistics(int eventCount, long totalDuration, double averageDuration, long maxDuration) {
            this.eventCount = eventCount;
            this.totalDuration = totalDuration;
            this.averageDuration = averageDuration;
            this.maxDuration = maxDuration;
        }
    }
}
