package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 🔮 Code Completion Engine
 * 
 * Context-aware code completion:
 * - Suggest next code line
 * - Suggest method names
 * - Suggest variable names
 * - Context-aware completion
 */
@Component
public class CodeCompletionEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(CodeCompletionEngine.class);
    
    /**
     * Suggest next code line
     */
    public List<String> suggestNextLine(String code, String context, String language) {
        if (code == null || code.isEmpty()) {
            logger.warn("⚠️ Empty code provided");
            return new ArrayList<>();
        }
        
        logger.debug("🔮 Suggesting next line (language={})", language);
        
        List<String> suggestions = new ArrayList<>();
        String lastLine = getLastLine(code);
        
        // Based on last line, suggest next
        if (lastLine.contains("if (")) {
            suggestions.add("    // Handle condition");
            suggestions.add("    // TODO: implement logic");
        }
        
        if (lastLine.contains("for (")) {
            suggestions.add("    // Loop body");
            suggestions.add("    // Process item");
        }
        
        if (lastLine.contains("try {")) {
            suggestions.add("    // Try block");
            suggestions.add("} catch (Exception e) {");
        }
        
        if (lastLine.contains("public ")) {
            suggestions.add("    // Method implementation");
            suggestions.add("    return null;");
        }
        
        if (lastLine.contains("class ")) {
            suggestions.add("    // Class fields");
            suggestions.add("    // Constructor");
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("    // Continue implementation");
        }
        
        logger.debug("✅ Suggested {} next lines", suggestions.size());
        return suggestions;
    }
    
    /**
     * Suggest method names
     */
    public List<String> suggestMethodNames(String context, String language) {
        logger.debug("🔮 Suggesting method names");
        
        List<String> suggestions = new ArrayList<>();
        String lowerContext = context.toLowerCase();
        
        // Based on context keywords
        if (lowerContext.contains("get") || lowerContext.contains("retrieve")) {
            suggestions.add("get");
            suggestions.add("fetch");
            suggestions.add("retrieve");
            suggestions.add("load");
        }
        
        if (lowerContext.contains("set") || lowerContext.contains("update")) {
            suggestions.add("set");
            suggestions.add("update");
            suggestions.add("modify");
            suggestions.add("change");
        }
        
        if (lowerContext.contains("create") || lowerContext.contains("new")) {
            suggestions.add("create");
            suggestions.add("initialize");
            suggestions.add("build");
            suggestions.add("construct");
        }
        
        if (lowerContext.contains("delete") || lowerContext.contains("remove")) {
            suggestions.add("delete");
            suggestions.add("remove");
            suggestions.add("destroy");
            suggestions.add("clear");
        }
        
        if (lowerContext.contains("check") || lowerContext.contains("validate")) {
            suggestions.add("validate");
            suggestions.add("check");
            suggestions.add("verify");
            suggestions.add("isValid");
        }
        
        if (lowerContext.contains("process") || lowerContext.contains("handle")) {
            suggestions.add("process");
            suggestions.add("handle");
            suggestions.add("execute");
            suggestions.add("run");
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("execute");
            suggestions.add("process");
            suggestions.add("handle");
        }
        
        logger.debug("✅ Suggested {} method names", suggestions.size());
        return suggestions;
    }
    
    /**
     * Suggest variable names
     */
    public List<String> suggestVariableNames(String context, String language) {
        logger.debug("🔮 Suggesting variable names");
        
        List<String> suggestions = new ArrayList<>();
        String lowerContext = context.toLowerCase();
        
        // Based on context type
        if (lowerContext.contains("list") || lowerContext.contains("array")) {
            suggestions.add("items");
            suggestions.add("elements");
            suggestions.add("values");
            suggestions.add("data");
        }
        
        if (lowerContext.contains("map") || lowerContext.contains("dictionary")) {
            suggestions.add("map");
            suggestions.add("dictionary");
            suggestions.add("cache");
            suggestions.add("lookup");
        }
        
        if (lowerContext.contains("string") || lowerContext.contains("text")) {
            suggestions.add("text");
            suggestions.add("message");
            suggestions.add("content");
            suggestions.add("value");
        }
        
        if (lowerContext.contains("number") || lowerContext.contains("count")) {
            suggestions.add("count");
            suggestions.add("total");
            suggestions.add("size");
            suggestions.add("index");
        }
        
        if (lowerContext.contains("boolean") || lowerContext.contains("flag")) {
            suggestions.add("isValid");
            suggestions.add("isActive");
            suggestions.add("enabled");
            suggestions.add("flag");
        }
        
        if (lowerContext.contains("object") || lowerContext.contains("entity")) {
            suggestions.add("object");
            suggestions.add("entity");
            suggestions.add("instance");
            suggestions.add("item");
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("value");
            suggestions.add("result");
            suggestions.add("data");
        }
        
        logger.debug("✅ Suggested {} variable names", suggestions.size());
        return suggestions;
    }
    
    /**
     * Get context-aware completions
     */
    public List<Completion> getCompletions(String prefix, String context, String language) {
        logger.debug("🔮 Getting completions for prefix: {}", prefix);
        
        List<Completion> completions = new ArrayList<>();
        
        // Method completions
        if (prefix.contains(".")) {
            completions.addAll(getMethodCompletions(prefix, language));
        }
        
        // Variable completions
        if (prefix.matches("^[a-zA-Z_].*")) {
            completions.addAll(getVariableCompletions(prefix, language));
        }
        
        // Keyword completions
        completions.addAll(getKeywordCompletions(prefix, language));
        
        // Sort by relevance
        completions.sort((a, b) -> Double.compare(b.relevance, a.relevance));
        
        logger.debug("✅ Generated {} completions", completions.size());
        return completions;
    }
    
    /**
     * Get method completions
     */
    private List<Completion> getMethodCompletions(String prefix, String language) {
        List<Completion> completions = new ArrayList<>();
        
        // Common Java methods
        if ("java".equals(language)) {
            String[] methods = {
                "toString()", "equals()", "hashCode()", "clone()",
                "getClass()", "notify()", "notifyAll()", "wait()",
                "stream()", "filter()", "map()", "collect()",
                "forEach()", "reduce()", "sorted()", "distinct()"
            };
            
            for (String method : methods) {
                if (method.startsWith(prefix.substring(prefix.lastIndexOf(".") + 1))) {
                    completions.add(new Completion(method, "method", 0.8));
                }
            }
        }
        
        return completions;
    }
    
    /**
     * Get variable completions
     */
    private List<Completion> getVariableCompletions(String prefix, String language) {
        List<Completion> completions = new ArrayList<>();
        
        // Common variable names
        String[] variables = {
            "value", "result", "data", "count", "index",
            "item", "element", "object", "instance", "entity",
            "message", "text", "content", "name", "id"
        };
        
        for (String var : variables) {
            if (var.startsWith(prefix)) {
                completions.add(new Completion(var, "variable", 0.7));
            }
        }
        
        return completions;
    }
    
    /**
     * Get keyword completions
     */
    private List<Completion> getKeywordCompletions(String prefix, String language) {
        List<Completion> completions = new ArrayList<>();
        
        if ("java".equals(language)) {
            String[] keywords = {
                "public", "private", "protected", "static", "final",
                "class", "interface", "enum", "abstract",
                "if", "else", "for", "while", "do", "switch", "case",
                "try", "catch", "finally", "throw", "throws",
                "new", "return", "break", "continue", "default"
            };
            
            for (String keyword : keywords) {
                if (keyword.startsWith(prefix)) {
                    completions.add(new Completion(keyword, "keyword", 0.9));
                }
            }
        }
        
        return completions;
    }
    
    /**
     * Get last line of code
     */
    private String getLastLine(String code) {
        String[] lines = code.split("\n");
        return lines.length > 0 ? lines[lines.length - 1] : "";
    }
    
    /**
     * Completion DTO
     */
    public static class Completion {
        public String text;
        public String type; // method, variable, keyword, class
        public double relevance; // 0-1
        
        public Completion(String text, String type, double relevance) {
            this.text = text;
            this.type = type;
            this.relevance = relevance;
        }
        
        @Override
        public String toString() {
            return String.format("%s (%s, %.1f%%)", text, type, relevance * 100);
        }
    }
}
