package com.vijay.dashboard;

import com.vijay.cache.SmartCacheManager;
import com.vijay.memory.LongTermMemory;
import com.vijay.memory.ShortTermMemory;
import com.vijay.personality.PersonalityEngineV2;
import com.vijay.token.TokenBudgetManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 📊 Real-Time Logs Dashboard - Monitor system metrics
 * 
 * Endpoints:
 * GET /dashboard/metrics - All metrics
 * GET /dashboard/cache - Cache statistics
 * GET /dashboard/tokens - Token usage
 * GET /dashboard/memory - Memory statistics
 * GET /dashboard/personality - Personality metrics
 * GET /dashboard/logs - Recent logs
 */
@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    
    private final SmartCacheManager cacheManager;
    private final TokenBudgetManager tokenBudgetManager;
    private final ShortTermMemory shortTermMemory;
    private final PersonalityEngineV2 personalityEngine;
    
    // Recent logs storage
    private final Deque<LogEntry> recentLogs = new LinkedList<>();
    private static final int MAX_LOGS = 100;
    
    public DashboardController(
            SmartCacheManager cacheManager,
            TokenBudgetManager tokenBudgetManager,
            ShortTermMemory shortTermMemory,
            PersonalityEngineV2 personalityEngine) {
        this.cacheManager = cacheManager;
        this.tokenBudgetManager = tokenBudgetManager;
        this.shortTermMemory = shortTermMemory;
        this.personalityEngine = personalityEngine;
    }
    
    /**
     * Get all metrics
     */
    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        
        metrics.put("timestamp", System.currentTimeMillis());
        metrics.put("cache", cacheManager.getStats());
        metrics.put("tokens", tokenBudgetManager.getStatus());
        metrics.put("memory", getMemoryStats());
        metrics.put("logs", new ArrayList<>(recentLogs));
        
        logger.info("📊 Dashboard metrics retrieved");
        return metrics;
    }
    
    /**
     * Get cache statistics
     */
    @GetMapping("/cache")
    public Map<String, Object> getCacheStats() {
        SmartCacheManager.CacheStats stats = cacheManager.getStats();
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("hits", stats.hits);
        response.put("misses", stats.misses);
        response.put("hitRate", String.format("%.1f%%", stats.hitRate));
        response.put("size", stats.size);
        response.put("tokensSaved", stats.tokensSaved);
        
        logger.info("💾 Cache stats: {}", stats);
        return response;
    }
    
    /**
     * Get token budget status
     */
    @GetMapping("/tokens")
    public TokenBudgetManager.BudgetStatus getTokenStatus() {
        TokenBudgetManager.BudgetStatus status = tokenBudgetManager.getStatus();
        logger.info("💰 Token status: {}", status);
        return status;
    }
    
    /**
     * Get memory statistics
     */
    @GetMapping("/memory")
    public Map<String, Object> getMemoryStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("shortTermMessages", shortTermMemory.size());
        stats.put("timestamp", System.currentTimeMillis());
        
        logger.info("💭 Memory stats retrieved");
        return stats;
    }
    
    /**
     * Get personality metrics
     */
    @GetMapping("/personality")
    public Map<String, Object> getPersonalityMetrics(
            @RequestParam(defaultValue = "user1") String userId,
            @RequestParam(defaultValue = "explain how this works") String query) {
        
        PersonalityEngineV2.PersonalityMetrics metrics = personalityEngine.getMetrics(userId, query);
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("mode", metrics.mode);
        response.put("description", metrics.description);
        response.put("helpfulness", String.format("%.0f/10", metrics.helpfulness * 10));
        response.put("humor", String.format("%.0f/10", metrics.humor * 10));
        
        logger.info("🎭 Personality metrics: {}", metrics);
        return response;
    }
    
    /**
     * Get recent logs
     */
    @GetMapping("/logs")
    public List<LogEntry> getRecentLogs(
            @RequestParam(defaultValue = "20") int limit) {
        
        List<LogEntry> logs = new ArrayList<>(recentLogs);
        if (logs.size() > limit) {
            logs = logs.subList(logs.size() - limit, logs.size());
        }
        
        logger.info("📋 Retrieved {} recent logs", logs.size());
        return logs;
    }
    
    /**
     * Add log entry (internal use)
     */
    public void addLog(String level, String component, String message) {
        LogEntry entry = new LogEntry(level, component, message);
        recentLogs.addLast(entry);
        
        // Keep only recent logs
        while (recentLogs.size() > MAX_LOGS) {
            recentLogs.removeFirst();
        }
    }
    
    /**
     * Clear all logs
     */
    @PostMapping("/logs/clear")
    public Map<String, String> clearLogs() {
        int size = recentLogs.size();
        recentLogs.clear();
        
        Map<String, String> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("cleared", String.valueOf(size));
        
        logger.info("🧹 Cleared {} log entries", size);
        return response;
    }
    
    /**
     * Get dashboard HTML UI
     */
    @GetMapping("/ui")
    public String getDashboardUI() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>AI System Dashboard</title>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #1a1a1a; color: #fff; }
                    .container { max-width: 1400px; margin: 0 auto; padding: 20px; }
                    .header { text-align: center; margin-bottom: 30px; }
                    .header h1 { font-size: 2.5em; color: #00d4ff; }
                    .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; margin-bottom: 30px; }
                    .card { background: #2a2a2a; border: 2px solid #00d4ff; border-radius: 10px; padding: 20px; }
                    .card h2 { color: #00d4ff; margin-bottom: 15px; font-size: 1.3em; }
                    .metric { display: flex; justify-content: space-between; margin: 10px 0; padding: 8px; background: #1a1a1a; border-radius: 5px; }
                    .metric-label { font-weight: bold; }
                    .metric-value { color: #00ff00; }
                    .warning { color: #ff6600; }
                    .error { color: #ff0000; }
                    .success { color: #00ff00; }
                    .logs { background: #2a2a2a; border: 2px solid #00d4ff; border-radius: 10px; padding: 20px; }
                    .log-entry { background: #1a1a1a; padding: 10px; margin: 5px 0; border-left: 3px solid #00d4ff; border-radius: 3px; font-family: monospace; font-size: 0.9em; }
                    .refresh-btn { background: #00d4ff; color: #000; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; font-weight: bold; }
                    .refresh-btn:hover { background: #00ffff; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🤖 AI System Dashboard</h1>
                        <p>Real-time monitoring and metrics</p>
                        <button class="refresh-btn" onclick="refreshMetrics()">🔄 Refresh</button>
                    </div>
                    
                    <div class="grid">
                        <div class="card">
                            <h2>💾 Cache</h2>
                            <div id="cache-stats"></div>
                        </div>
                        <div class="card">
                            <h2>💰 Tokens</h2>
                            <div id="token-stats"></div>
                        </div>
                        <div class="card">
                            <h2>💭 Memory</h2>
                            <div id="memory-stats"></div>
                        </div>
                        <div class="card">
                            <h2>🎭 Personality</h2>
                            <div id="personality-stats"></div>
                        </div>
                    </div>
                    
                    <div class="logs">
                        <h2>📋 Recent Logs</h2>
                        <div id="logs-container"></div>
                    </div>
                </div>
                
                <script>
                    async function refreshMetrics() {
                        try {
                            const metrics = await fetch('/dashboard/metrics').then(r => r.json());
                            
                            // Update cache
                            document.getElementById('cache-stats').innerHTML = `
                                <div class="metric">
                                    <span class="metric-label">Hits:</span>
                                    <span class="metric-value">${metrics.cache.hits}</span>
                                </div>
                                <div class="metric">
                                    <span class="metric-label">Hit Rate:</span>
                                    <span class="metric-value">${(metrics.cache.hitRate * 100).toFixed(1)}%</span>
                                </div>
                                <div class="metric">
                                    <span class="metric-label">Tokens Saved:</span>
                                    <span class="metric-value">${metrics.cache.tokensSaved}</span>
                                </div>
                            `;
                            
                            // Update tokens
                            const tokenPercent = (metrics.tokens.usagePercent).toFixed(1);
                            const tokenClass = tokenPercent > 80 ? 'warning' : 'success';
                            document.getElementById('token-stats').innerHTML = `
                                <div class="metric">
                                    <span class="metric-label">Used:</span>
                                    <span class="metric-value">${metrics.tokens.used}/${metrics.tokens.total}</span>
                                </div>
                                <div class="metric">
                                    <span class="metric-label">Usage:</span>
                                    <span class="metric-value ${tokenClass}">${tokenPercent}%</span>
                                </div>
                            `;
                            
                            // Update memory
                            document.getElementById('memory-stats').innerHTML = `
                                <div class="metric">
                                    <span class="metric-label">Short-term:</span>
                                    <span class="metric-value">${metrics.memory.shortTermMessages} messages</span>
                                </div>
                            `;
                            
                            // Update personality
                            const personality = await fetch('/dashboard/personality').then(r => r.json());
                            document.getElementById('personality-stats').innerHTML = `
                                <div class="metric">
                                    <span class="metric-label">Mode:</span>
                                    <span class="metric-value">${personality.mode}</span>
                                </div>
                                <div class="metric">
                                    <span class="metric-label">Helpfulness:</span>
                                    <span class="metric-value">${personality.helpfulness}</span>
                                </div>
                            `;
                            
                            // Update logs
                            const logs = await fetch('/dashboard/logs?limit=10').then(r => r.json());
                            const logsHtml = logs.map(log => `
                                <div class="log-entry">
                                    <strong>[${log.level}]</strong> ${log.component}: ${log.message}
                                </div>
                            `).join('');
                            document.getElementById('logs-container').innerHTML = logsHtml;
                            
                        } catch (error) {
                            console.error('Error fetching metrics:', error);
                        }
                    }
                    
                    // Auto-refresh every 5 seconds
                    refreshMetrics();
                    setInterval(refreshMetrics, 5000);
                </script>
            </body>
            </html>
            """;
    }
    
    /**
     * Log entry DTO
     */
    public static class LogEntry {
        public String level;
        public String component;
        public String message;
        public long timestamp;
        
        public LogEntry(String level, String component, String message) {
            this.level = level;
            this.component = component;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
