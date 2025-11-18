package com.vijay.editing;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 🎯 SMART COMPLETION ENGINE
 * 
 * Generates intelligent code completions based on context.
 * Provides method, class, import, and variable completions.
 * 
 * ✅ PHASE 2: Intelligent Editing - Week 2
 */
@Service
@RequiredArgsConstructor
public class SmartCompletionEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(SmartCompletionEngine.class);
    private final ObjectMapper objectMapper;
    
    // Common Java methods and classes
    private static final List<String> COMMON_METHODS = List.of(
        "toString()", "equals()", "hashCode()", "clone()", "compareTo()",
        "getValue()", "setValue()", "get()", "set()", "add()", "remove()",
        "contains()", "size()", "isEmpty()", "clear()", "iterator()"
    );
    
    private static final List<String> COMMON_CLASSES = List.of(
        "String", "Integer", "Long", "Double", "Boolean", "List", "Map",
        "Set", "ArrayList", "HashMap", "HashSet", "LinkedList", "TreeMap",
        "Optional", "Stream", "LocalDate", "LocalDateTime", "Exception"
    );
    
    private static final List<String> COMMON_IMPORTS = List.of(
        "java.util.*", "java.io.*", "java.time.*", "java.lang.*",
        "java.util.stream.*", "java.util.concurrent.*", "java.nio.*"
    );
    
    /**
     * Get completions at cursor position
     */
    @Tool(description = "Get intelligent code completions")
    public String getCompletions(
            @ToolParam(description = "Full code") String fullCode,
            @ToolParam(description = "Cursor line") int cursorLine,
            @ToolParam(description = "Cursor column") int cursorColumn,
            @ToolParam(description = "Partial input") String partialInput) {
        
        logger.info("🎯 Getting completions for: {}", partialInput);
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Determine completion type
            String completionType = determineCompletionType(partialInput);
            
            // Generate completions
            List<CodeCompletion> completions = new ArrayList<>();
            
            switch (completionType) {
                case "method":
                    completions.addAll(completeMethod(partialInput, fullCode));
                    break;
                case "class":
                    completions.addAll(completeClass(partialInput, fullCode));
                    break;
                case "import":
                    completions.addAll(completeImport(partialInput, fullCode));
                    break;
                case "variable":
                    completions.addAll(completeVariable(partialInput, fullCode));
                    break;
                default:
                    completions.addAll(completeGeneral(partialInput, fullCode));
            }
            
            // Sort by relevance
            completions.sort((a, b) -> Double.compare(b.getRelevance(), a.getRelevance()));
            
            // Limit to top 10
            completions = completions.stream().limit(10).collect(Collectors.toList());
            
            result.put("status", "success");
            result.put("completionType", completionType);
            result.put("partialInput", partialInput);
            result.put("completions", completions);
            result.put("count", completions.size());
            
            logger.info("✅ Generated {} completions", completions.size());
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Completion generation failed: {}", e.getMessage());
            return errorResponse("Completion generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Complete method names
     */
    private List<CodeCompletion> completeMethod(String partial, String context) {
        logger.info("🎯 Completing method: {}", partial);
        
        List<CodeCompletion> completions = new ArrayList<>();
        
        for (String method : COMMON_METHODS) {
            if (method.startsWith(partial)) {
                CodeCompletion completion = new CodeCompletion();
                completion.setType("Method");
                completion.setText(method);
                completion.setLabel(method);
                completion.setDescription("Method: " + method);
                completion.setRelevance(calculateRelevance(partial, method));
                completion.setIcon("ƒ");
                completion.setKind("Method");
                
                completions.add(completion);
            }
        }
        
        // Extract methods from context
        Pattern methodPattern = Pattern.compile("public\\s+\\w+\\s+(\\w+)\\s*\\(");
        Matcher matcher = methodPattern.matcher(context);
        
        while (matcher.find()) {
            String methodName = matcher.group(1);
            if (methodName.startsWith(partial)) {
                CodeCompletion completion = new CodeCompletion();
                completion.setType("Method");
                completion.setText(methodName + "()");
                completion.setLabel(methodName);
                completion.setDescription("Local method: " + methodName);
                completion.setRelevance(0.95);
                completion.setIcon("ƒ");
                completion.setKind("Method");
                
                completions.add(completion);
            }
        }
        
        return completions;
    }
    
    /**
     * Complete class names
     */
    private List<CodeCompletion> completeClass(String partial, String context) {
        logger.info("🎯 Completing class: {}", partial);
        
        List<CodeCompletion> completions = new ArrayList<>();
        
        for (String className : COMMON_CLASSES) {
            if (className.startsWith(partial)) {
                CodeCompletion completion = new CodeCompletion();
                completion.setType("Class");
                completion.setText(className);
                completion.setLabel(className);
                completion.setDescription("Class: " + className);
                completion.setRelevance(calculateRelevance(partial, className));
                completion.setIcon("C");
                completion.setKind("Class");
                
                completions.add(completion);
            }
        }
        
        // Extract classes from context
        Pattern classPattern = Pattern.compile("class\\s+(\\w+)");
        Matcher matcher = classPattern.matcher(context);
        
        while (matcher.find()) {
            String className = matcher.group(1);
            if (className.startsWith(partial)) {
                CodeCompletion completion = new CodeCompletion();
                completion.setType("Class");
                completion.setText(className);
                completion.setLabel(className);
                completion.setDescription("Local class: " + className);
                completion.setRelevance(0.95);
                completion.setIcon("C");
                completion.setKind("Class");
                
                completions.add(completion);
            }
        }
        
        return completions;
    }
    
    /**
     * Complete import statements
     */
    private List<CodeCompletion> completeImport(String partial, String context) {
        logger.info("🎯 Completing import: {}", partial);
        
        List<CodeCompletion> completions = new ArrayList<>();
        
        for (String importPath : COMMON_IMPORTS) {
            if (importPath.startsWith(partial)) {
                CodeCompletion completion = new CodeCompletion();
                completion.setType("Import");
                completion.setText("import " + importPath + ";");
                completion.setLabel(importPath);
                completion.setDescription("Import: " + importPath);
                completion.setRelevance(calculateRelevance(partial, importPath));
                completion.setIcon("📦");
                completion.setKind("Module");
                
                completions.add(completion);
            }
        }
        
        return completions;
    }
    
    /**
     * Complete variable names
     */
    private List<CodeCompletion> completeVariable(String partial, String context) {
        logger.info("🎯 Completing variable: {}", partial);
        
        List<CodeCompletion> completions = new ArrayList<>();
        
        // Extract variables from context
        Pattern varPattern = Pattern.compile("\\b(\\w+)\\s*=");
        Matcher matcher = varPattern.matcher(context);
        
        while (matcher.find()) {
            String varName = matcher.group(1);
            if (varName.startsWith(partial) && !isPrimitiveType(varName)) {
                CodeCompletion completion = new CodeCompletion();
                completion.setType("Variable");
                completion.setText(varName);
                completion.setLabel(varName);
                completion.setDescription("Variable: " + varName);
                completion.setRelevance(calculateRelevance(partial, varName));
                completion.setIcon("v");
                completion.setKind("Variable");
                
                completions.add(completion);
            }
        }
        
        return completions;
    }
    
    /**
     * General completions
     */
    private List<CodeCompletion> completeGeneral(String partial, String context) {
        logger.info("🎯 Generating general completions");
        
        List<CodeCompletion> completions = new ArrayList<>();
        
        // Combine all completions
        completions.addAll(completeMethod(partial, context));
        completions.addAll(completeClass(partial, context));
        completions.addAll(completeVariable(partial, context));
        
        return completions;
    }
    
    /**
     * Get completions for specific context
     */
    @Tool(description = "Get context-aware completions")
    public String getContextAwareCompletions(
            @ToolParam(description = "Code context") String context,
            @ToolParam(description = "Partial input") String partial) {
        
        logger.info("🎯 Getting context-aware completions");
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            List<CodeCompletion> completions = new ArrayList<>();
            
            // Analyze context
            if (context.contains("new ")) {
                completions.addAll(completeClass(partial, context));
            } else if (context.contains(".")) {
                completions.addAll(completeMethod(partial, context));
            } else if (context.contains("import")) {
                completions.addAll(completeImport(partial, context));
            } else {
                completions.addAll(completeGeneral(partial, context));
            }
            
            // Sort and limit
            completions.sort((a, b) -> Double.compare(b.getRelevance(), a.getRelevance()));
            completions = completions.stream().limit(10).collect(Collectors.toList());
            
            result.put("status", "success");
            result.put("completions", completions);
            result.put("count", completions.size());
            
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Context-aware completion failed: {}", e.getMessage());
            return errorResponse("Context-aware completion failed: " + e.getMessage());
        }
    }
    
    // Helper methods
    
    private String determineCompletionType(String partial) {
        if (partial.contains("(")) return "method";
        if (partial.contains("import")) return "import";
        if (Character.isUpperCase(partial.charAt(0))) return "class";
        return "variable";
    }
    
    private double calculateRelevance(String partial, String completion) {
        // Exact match
        if (completion.equals(partial)) return 1.0;
        
        // Starts with partial
        if (completion.startsWith(partial)) {
            double ratio = (double) partial.length() / completion.length();
            return 0.8 + (ratio * 0.2);
        }
        
        // Contains partial
        if (completion.contains(partial)) {
            return 0.5;
        }
        
        return 0.3;
    }
    
    private boolean isPrimitiveType(String name) {
        String[] primitives = {"int", "long", "double", "float", "boolean", "byte", "short", "char"};
        for (String prim : primitives) {
            if (prim.equals(name)) return true;
        }
        return false;
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
        return "{\"status\": \"error\", \"message\": \"" + message.replace("\"", "\\\"") + "\"}";
    }
    
    // Inner class
    
    public static class CodeCompletion {
        private String type;
        private String text;
        private String label;
        private String description;
        private double relevance;
        private String icon;
        private String kind;
        
        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public double getRelevance() { return relevance; }
        public void setRelevance(double relevance) { this.relevance = relevance; }
        
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        
        public String getKind() { return kind; }
        public void setKind(String kind) { this.kind = kind; }
    }
}
