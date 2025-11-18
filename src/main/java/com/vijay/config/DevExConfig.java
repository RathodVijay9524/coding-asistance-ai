package com.vijay.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 🧠 DEVEX CONFIGURATION - Phase 10
 * 
 * Purpose: Configure DevEx features
 * 
 * Properties:
 * - Enable/disable logging
 * - Enable/disable profiling
 * - Enable/disable caching
 * - Redis configuration
 * - Cache TTL settings
 */
@Component
@ConfigurationProperties(prefix = "devex")
public class DevExConfig {
    
    // Logging configuration
    private LoggingConfig logging = new LoggingConfig();
    
    // Profiling configuration
    private ProfilingConfig profiling = new ProfilingConfig();
    
    // Caching configuration
    private CachingConfig caching = new CachingConfig();
    
    // Redis configuration
    private RedisConfig redis = new RedisConfig();
    
    // Getters and setters
    public LoggingConfig getLogging() {
        return logging;
    }
    
    public void setLogging(LoggingConfig logging) {
        this.logging = logging;
    }
    
    public ProfilingConfig getProfiling() {
        return profiling;
    }
    
    public void setProfiling(ProfilingConfig profiling) {
        this.profiling = profiling;
    }
    
    public CachingConfig getCaching() {
        return caching;
    }
    
    public void setCaching(CachingConfig caching) {
        this.caching = caching;
    }
    
    public RedisConfig getRedis() {
        return redis;
    }
    
    public void setRedis(RedisConfig redis) {
        this.redis = redis;
    }
    
    // ============ Inner Classes ============
    
    /**
     * Logging configuration
     */
    public static class LoggingConfig {
        private boolean enabled = true;
        private int maxLogEntries = 10000;
        private boolean logAdvisors = true;
        private boolean logModels = true;
        private boolean logErrors = true;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getMaxLogEntries() {
            return maxLogEntries;
        }
        
        public void setMaxLogEntries(int maxLogEntries) {
            this.maxLogEntries = maxLogEntries;
        }
        
        public boolean isLogAdvisors() {
            return logAdvisors;
        }
        
        public void setLogAdvisors(boolean logAdvisors) {
            this.logAdvisors = logAdvisors;
        }
        
        public boolean isLogModels() {
            return logModels;
        }
        
        public void setLogModels(boolean logModels) {
            this.logModels = logModels;
        }
        
        public boolean isLogErrors() {
            return logErrors;
        }
        
        public void setLogErrors(boolean logErrors) {
            this.logErrors = logErrors;
        }
    }
    
    /**
     * Profiling configuration
     */
    public static class ProfilingConfig {
        private boolean enabled = true;
        private boolean profileMemory = true;
        private boolean profileCPU = true;
        private boolean profileGC = true;
        private long profilingInterval = 1000; // milliseconds
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public boolean isProfileMemory() {
            return profileMemory;
        }
        
        public void setProfileMemory(boolean profileMemory) {
            this.profileMemory = profileMemory;
        }
        
        public boolean isProfileCPU() {
            return profileCPU;
        }
        
        public void setProfileCPU(boolean profileCPU) {
            this.profileCPU = profileCPU;
        }
        
        public boolean isProfileGC() {
            return profileGC;
        }
        
        public void setProfileGC(boolean profileGC) {
            this.profileGC = profileGC;
        }
        
        public long getProfilingInterval() {
            return profilingInterval;
        }
        
        public void setProfilingInterval(long profilingInterval) {
            this.profilingInterval = profilingInterval;
        }
    }
    
    /**
     * Caching configuration
     */
    public static class CachingConfig {
        private boolean enabled = true;
        private int maxSize = 10000;
        private long advisorOutputTTL = 3600; // 1 hour
        private long modelResponseTTL = 7200; // 2 hours
        private long userPreferenceTTL = 3600; // 1 hour
        private long knowledgeGraphTTL = 86400; // 24 hours
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getMaxSize() {
            return maxSize;
        }
        
        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }
        
        public long getAdvisorOutputTTL() {
            return advisorOutputTTL;
        }
        
        public void setAdvisorOutputTTL(long advisorOutputTTL) {
            this.advisorOutputTTL = advisorOutputTTL;
        }
        
        public long getModelResponseTTL() {
            return modelResponseTTL;
        }
        
        public void setModelResponseTTL(long modelResponseTTL) {
            this.modelResponseTTL = modelResponseTTL;
        }
        
        public long getUserPreferenceTTL() {
            return userPreferenceTTL;
        }
        
        public void setUserPreferenceTTL(long userPreferenceTTL) {
            this.userPreferenceTTL = userPreferenceTTL;
        }
        
        public long getKnowledgeGraphTTL() {
            return knowledgeGraphTTL;
        }
        
        public void setKnowledgeGraphTTL(long knowledgeGraphTTL) {
            this.knowledgeGraphTTL = knowledgeGraphTTL;
        }
    }
    
    /**
     * Redis configuration
     */
    public static class RedisConfig {
        private boolean enabled = false;
        private String host = "localhost";
        private int port = 6379;
        private String password = "";
        private int database = 0;
        private long timeout = 2000; // milliseconds
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getHost() {
            return host;
        }
        
        public void setHost(String host) {
            this.host = host;
        }
        
        public int getPort() {
            return port;
        }
        
        public void setPort(int port) {
            this.port = port;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public int getDatabase() {
            return database;
        }
        
        public void setDatabase(int database) {
            this.database = database;
        }
        
        public long getTimeout() {
            return timeout;
        }
        
        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }
    }
}
