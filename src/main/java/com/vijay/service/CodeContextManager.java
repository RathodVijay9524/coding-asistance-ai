package com.vijay.service;

import com.vijay.dto.CodeCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 📍 Code Context Manager
 * 
 * Manages code cursor positions and retrieves surrounding context
 * - Creates code cursors at file positions
 * - Retrieves surrounding code (±10 lines)
 * - Detects scope (class.method)
 * - Finds dependencies and imports
 */
@Component
public class CodeContextManager {
    
    private static final Logger logger = LoggerFactory.getLogger(CodeContextManager.class);
    private static final int DEFAULT_CONTEXT_RADIUS = 10;
    
    /**
     * Create code cursor at file position
     */
    public CodeCursor createCursor(String filePath, int lineNumber) {
        return createCursor(filePath, lineNumber, DEFAULT_CONTEXT_RADIUS);
    }
    
    /**
     * Create code cursor with custom context radius
     */
    public CodeCursor createCursor(String filePath, int lineNumber, int contextRadius) {
        if (filePath == null || filePath.isEmpty()) {
            logger.warn("⚠️ Empty file path provided");
            return createDefaultCursor();
        }
        
        logger.debug("📍 Creating code cursor: {}:{}", filePath, lineNumber);
        
        CodeCursor cursor = new CodeCursor();
        cursor.setFile(filePath);
        cursor.setLine(lineNumber);
        cursor.setLanguage(detectLanguage(filePath));
        
        try {
            // Get context code
            String context = getContext(filePath, lineNumber, contextRadius);
            cursor.setContext(context);
            
            // Detect scope
            String scope = detectScope(filePath, lineNumber);
            cursor.setScope(scope);
            
            // Extract class and method names
            extractClassAndMethod(cursor, scope);
            
            // Get dependencies
            List<String> dependencies = detectDependencies(filePath);
            cursor.setDependencies(dependencies);
            
            // Get imports
            List<String> imports = extractImports(filePath);
            cursor.setImports(imports);
            
            logger.debug("✅ Code cursor created: {}", cursor);
            
        } catch (IOException e) {
            logger.error("❌ Error creating code cursor: {}", e.getMessage());
            return createDefaultCursor();
        }
        
        return cursor;
    }
    
    /**
     * Get code context around line
     */
    private String getContext(String filePath, int lineNumber, int radius) throws IOException {
        List<String> allLines = Files.readAllLines(Paths.get(filePath));
        
        int startLine = Math.max(0, lineNumber - radius);
        int endLine = Math.min(allLines.size(), lineNumber + radius);
        
        StringBuilder context = new StringBuilder();
        for (int i = startLine; i < endLine; i++) {
            String marker = (i == lineNumber - 1) ? ">>> " : "    ";
            context.append(String.format("%s%d: %s\n", marker, i + 1, allLines.get(i)));
        }
        
        return context.toString();
    }
    
    /**
     * Detect programming language from file extension
     */
    private String detectLanguage(String filePath) {
        if (filePath.endsWith(".java")) return "java";
        if (filePath.endsWith(".py")) return "python";
        if (filePath.endsWith(".js")) return "javascript";
        if (filePath.endsWith(".ts")) return "typescript";
        if (filePath.endsWith(".go")) return "go";
        if (filePath.endsWith(".rs")) return "rust";
        if (filePath.endsWith(".cpp") || filePath.endsWith(".cc")) return "cpp";
        if (filePath.endsWith(".cs")) return "csharp";
        return "unknown";
    }
    
    /**
     * Detect scope (class.method) by searching backwards
     */
    private String detectScope(String filePath, int lineNumber) throws IOException {
        List<String> allLines = Files.readAllLines(Paths.get(filePath));
        
        String className = null;
        String methodName = null;
        
        // Search backwards for class definition
        for (int i = lineNumber - 1; i >= 0; i--) {
            String line = allLines.get(i);
            if (line.matches(".*\\b(public|private|protected)?\\s+class\\s+\\w+.*")) {
                Pattern p = Pattern.compile("class\\s+(\\w+)");
                Matcher m = p.matcher(line);
                if (m.find()) {
                    className = m.group(1);
                    break;
                }
            }
        }
        
        // Search backwards for method definition
        for (int i = lineNumber - 1; i >= 0; i--) {
            String line = allLines.get(i);
            if (line.matches(".*\\b(public|private|protected)?\\s+\\w+\\s+\\w+\\s*\\(.*")) {
                Pattern p = Pattern.compile("(\\w+)\\s*\\(");
                Matcher m = p.matcher(line);
                if (m.find()) {
                    methodName = m.group(1);
                    break;
                }
            }
        }
        
        if (className != null && methodName != null) {
            return className + "." + methodName;
        } else if (className != null) {
            return className;
        } else {
            return "unknown";
        }
    }
    
    /**
     * Extract class and method names from scope
     */
    private void extractClassAndMethod(CodeCursor cursor, String scope) {
        if (scope == null || scope.isEmpty()) return;
        
        String[] parts = scope.split("\\.");
        if (parts.length > 0) {
            cursor.setClassName(parts[0]);
        }
        if (parts.length > 1) {
            cursor.setMethodName(parts[1]);
        }
    }
    
    /**
     * Detect dependencies from imports
     */
    private List<String> detectDependencies(String filePath) throws IOException {
        List<String> allLines = Files.readAllLines(Paths.get(filePath));
        Set<String> dependencies = new HashSet<>();
        
        Pattern importPattern = Pattern.compile("^import\\s+([\\w.]+);?$");
        
        for (String line : allLines) {
            Matcher m = importPattern.matcher(line.trim());
            if (m.find()) {
                String importPath = m.group(1);
                // Extract package name
                String[] parts = importPath.split("\\.");
                if (parts.length > 0) {
                    dependencies.add(parts[0]);
                }
            }
        }
        
        return new ArrayList<>(dependencies);
    }
    
    /**
     * Extract all imports from file
     */
    private List<String> extractImports(String filePath) throws IOException {
        List<String> allLines = Files.readAllLines(Paths.get(filePath));
        
        return allLines.stream()
            .filter(line -> line.trim().startsWith("import "))
            .map(line -> line.trim().replaceAll("^import\\s+", "").replaceAll(";$", ""))
            .collect(Collectors.toList());
    }
    
    /**
     * Create default cursor for error cases
     */
    private CodeCursor createDefaultCursor() {
        return new CodeCursor()
            .setFile("unknown")
            .setLine(0)
            .setLanguage("unknown")
            .setScope("unknown");
    }
}
