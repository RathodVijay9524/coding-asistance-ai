package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 🌳 AST Analysis Service
 * 
 * Performs Abstract Syntax Tree analysis on Java code including:
 * - Method extraction and analysis
 * - Class hierarchy detection
 * - Code complexity calculation
 * - Cyclomatic complexity analysis
 * - Method call graph generation
 * - Variable usage tracking
 * 
 * ✅ PHASE 3.2: Advanced Features
 * Uses regex-based parsing for lightweight AST analysis
 */
@Service
public class ASTAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(ASTAnalysisService.class);
    
    // Regex patterns for code analysis
    private static final Pattern CLASS_PATTERN = Pattern.compile("(?:public|private|protected)?\\s+(?:abstract\\s+)?class\\s+(\\w+)(?:\\s+extends\\s+(\\w+))?(?:\\s+implements\\s+([\\w,\\s]+))?");
    private static final Pattern METHOD_PATTERN = Pattern.compile("(?:public|private|protected)?\\s+(?:static\\s+)?(?:synchronized\\s+)?(?:\\w+\\s+)*?(\\w+)\\s*\\(([^)]*)\\)\\s*(?:throws\\s+[\\w,\\s]+)?\\s*\\{");
    private static final Pattern FIELD_PATTERN = Pattern.compile("(?:public|private|protected)?\\s+(?:static\\s+)?(?:final\\s+)?(\\w+)\\s+(\\w+)(?:\\s*=\\s*[^;]+)?;");
    private static final Pattern METHOD_CALL_PATTERN = Pattern.compile("(\\w+)\\s*\\(");
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("(?:new\\s+)?(\\w+)\\s+(\\w+)(?:\\s*=)?");
    
    /**
     * Extract all methods from source code
     */
    public List<MethodInfo> extractMethods(String sourceCode) {
        List<MethodInfo> methods = new ArrayList<>();
        
        try {
            Matcher matcher = METHOD_PATTERN.matcher(sourceCode);
            int methodCount = 0;
            
            while (matcher.find() && methodCount < 50) { // Limit to 50 methods
                String methodName = matcher.group(1);
                String parameters = matcher.group(2);
                
                MethodInfo method = new MethodInfo();
                method.setName(methodName);
                method.setParameters(parseParameters(parameters));
                method.setComplexity(calculateMethodComplexity(sourceCode, methodName));
                method.setLineCount(estimateMethodLines(sourceCode, methodName));
                
                methods.add(method);
                methodCount++;
            }
            
            logger.info("✅ Extracted {} methods from source code", methods.size());
            
        } catch (Exception e) {
            logger.error("❌ Method extraction failed: {}", e.getMessage());
        }
        
        return methods;
    }
    
    /**
     * Extract class information
     */
    public ClassInfo extractClassInfo(String sourceCode) {
        ClassInfo classInfo = new ClassInfo();
        
        try {
            Matcher matcher = CLASS_PATTERN.matcher(sourceCode);
            
            if (matcher.find()) {
                classInfo.setName(matcher.group(1));
                classInfo.setParentClass(matcher.group(2));
                
                String interfaces = matcher.group(3);
                if (interfaces != null) {
                    classInfo.setInterfaces(Arrays.asList(interfaces.split(",")));
                }
            }
            
            // Extract fields
            classInfo.setFields(extractFields(sourceCode));
            
            // Extract methods
            classInfo.setMethods(extractMethods(sourceCode));
            
            // Calculate metrics
            classInfo.setComplexity(calculateClassComplexity(sourceCode));
            classInfo.setLinesOfCode(countLines(sourceCode));
            
            logger.info("✅ Extracted class info: {}", classInfo.getName());
            
        } catch (Exception e) {
            logger.error("❌ Class extraction failed: {}", e.getMessage());
        }
        
        return classInfo;
    }
    
    /**
     * Generate method call graph
     */
    public MethodCallGraph generateCallGraph(String sourceCode) {
        MethodCallGraph graph = new MethodCallGraph();
        
        try {
            // Extract all methods
            List<MethodInfo> methods = extractMethods(sourceCode);
            
            // For each method, find what it calls
            for (MethodInfo method : methods) {
                List<String> calls = findMethodCalls(sourceCode, method.getName());
                graph.addNode(method.getName(), calls);
            }
            
            logger.info("✅ Generated call graph with {} nodes", graph.getNodeCount());
            
        } catch (Exception e) {
            logger.error("❌ Call graph generation failed: {}", e.getMessage());
        }
        
        return graph;
    }
    
    /**
     * Calculate cyclomatic complexity
     */
    public int calculateCyclomaticComplexity(String sourceCode) {
        int complexity = 1; // Base complexity
        
        try {
            // Count decision points
            complexity += countOccurrences(sourceCode, "if\\s*\\(");
            complexity += countOccurrences(sourceCode, "for\\s*\\(");
            complexity += countOccurrences(sourceCode, "while\\s*\\(");
            complexity += countOccurrences(sourceCode, "catch\\s*\\(");
            complexity += countOccurrences(sourceCode, "case\\s+");
            complexity += countOccurrences(sourceCode, "\\?\\s*"); // Ternary operator
            
        } catch (Exception e) {
            logger.debug("Could not calculate cyclomatic complexity: {}", e.getMessage());
        }
        
        return complexity;
    }
    
    /**
     * Detect code smells
     */
    public List<CodeSmell> detectCodeSmells(String sourceCode) {
        List<CodeSmell> smells = new ArrayList<>();
        
        try {
            // Long method detection
            List<MethodInfo> methods = extractMethods(sourceCode);
            for (MethodInfo method : methods) {
                if (method.getLineCount() > 50) {
                    smells.add(new CodeSmell("Long Method", method.getName(), 
                        "Method has " + method.getLineCount() + " lines (threshold: 50)"));
                }
                if (method.getComplexity() > 10) {
                    smells.add(new CodeSmell("High Complexity", method.getName(),
                        "Cyclomatic complexity: " + method.getComplexity()));
                }
            }
            
            // Large class detection
            ClassInfo classInfo = extractClassInfo(sourceCode);
            if (classInfo.getMethods().size() > 20) {
                smells.add(new CodeSmell("Large Class", classInfo.getName(),
                    "Class has " + classInfo.getMethods().size() + " methods"));
            }
            
            // Duplicate code detection
            if (countOccurrences(sourceCode, "TODO|FIXME|HACK") > 3) {
                smells.add(new CodeSmell("Technical Debt", "Multiple", 
                    "Found " + countOccurrences(sourceCode, "TODO|FIXME|HACK") + " TODO/FIXME comments"));
            }
            
            logger.info("✅ Detected {} code smells", smells.size());
            
        } catch (Exception e) {
            logger.error("❌ Code smell detection failed: {}", e.getMessage());
        }
        
        return smells;
    }
    
    /**
     * Analyze code dependencies
     */
    public CodeDependencies analyzeDependencies(String sourceCode) {
        CodeDependencies deps = new CodeDependencies();
        
        try {
            // Extract imports
            Pattern importPattern = Pattern.compile("import\\s+([\\w.]+);");
            Matcher matcher = importPattern.matcher(sourceCode);
            
            while (matcher.find()) {
                deps.addImport(matcher.group(1));
            }
            
            // Extract external class usages
            Pattern classUsagePattern = Pattern.compile("new\\s+(\\w+)\\s*\\(");
            matcher = classUsagePattern.matcher(sourceCode);
            
            while (matcher.find()) {
                deps.addExternalClass(matcher.group(1));
            }
            
            logger.info("✅ Analyzed dependencies: {} imports, {} external classes", 
                deps.getImportCount(), deps.getExternalClassCount());
            
        } catch (Exception e) {
            logger.error("❌ Dependency analysis failed: {}", e.getMessage());
        }
        
        return deps;
    }
    
    // Helper methods
    
    private List<FieldInfo> extractFields(String sourceCode) {
        List<FieldInfo> fields = new ArrayList<>();
        
        try {
            Matcher matcher = FIELD_PATTERN.matcher(sourceCode);
            
            while (matcher.find()) {
                FieldInfo field = new FieldInfo();
                field.setType(matcher.group(1));
                field.setName(matcher.group(2));
                fields.add(field);
            }
            
        } catch (Exception e) {
            logger.debug("Could not extract fields: {}", e.getMessage());
        }
        
        return fields;
    }
    
    private List<String> parseParameters(String paramString) {
        List<String> params = new ArrayList<>();
        
        if (paramString == null || paramString.trim().isEmpty()) {
            return params;
        }
        
        String[] parts = paramString.split(",");
        for (String part : parts) {
            String[] tokens = part.trim().split("\\s+");
            if (tokens.length >= 2) {
                params.add(tokens[tokens.length - 1]); // Parameter name
            }
        }
        
        return params;
    }
    
    private int calculateMethodComplexity(String sourceCode, String methodName) {
        // Avoid calling extractMethods here to prevent recursive analysis
        // Heuristic: distribute overall cyclomatic complexity across occurrences of this method name
        int totalComplexity = calculateCyclomaticComplexity(sourceCode);
        int occurrences = Math.max(1, countOccurrences(sourceCode, methodName + "\\s*\\("));
        return Math.max(1, totalComplexity / occurrences);
    }
    
    private int estimateMethodLines(String sourceCode, String methodName) {
        // Simple estimation: count lines between method start and next method
        String[] lines = sourceCode.split("\n");
        int methodStart = -1;
        int braceCount = 0;
        
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(methodName + "(")) {
                methodStart = i;
            }
            if (methodStart != -1) {
                braceCount += countOccurrences(lines[i], "\\{");
                braceCount -= countOccurrences(lines[i], "\\}");
                if (braceCount == 0 && methodStart != -1) {
                    return i - methodStart;
                }
            }
        }
        
        return 10; // Default estimate
    }
    
    private int calculateClassComplexity(String sourceCode) {
        return calculateCyclomaticComplexity(sourceCode);
    }
    
    private int countLines(String sourceCode) {
        return sourceCode.split("\n").length;
    }
    
    private List<String> findMethodCalls(String sourceCode, String methodName) {
        List<String> calls = new ArrayList<>();
        
        try {
            Matcher matcher = METHOD_CALL_PATTERN.matcher(sourceCode);
            
            while (matcher.find()) {
                String call = matcher.group(1);
                if (!call.equals(methodName) && !calls.contains(call)) {
                    calls.add(call);
                }
            }
            
        } catch (Exception e) {
            logger.debug("Could not find method calls: {}", e.getMessage());
        }
        
        return calls;
    }
    
    private int countOccurrences(String text, String pattern) {
        try {
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(text);
            int count = 0;
            while (m.find()) {
                count++;
            }
            return count;
        } catch (Exception e) {
            return 0;
        }
    }
    
    // Inner classes for AST information
    
    public static class MethodInfo {
        private String name;
        private List<String> parameters = new ArrayList<>();
        private int complexity;
        private int lineCount;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<String> getParameters() { return parameters; }
        public void setParameters(List<String> parameters) { this.parameters = parameters; }
        public int getComplexity() { return complexity; }
        public void setComplexity(int complexity) { this.complexity = complexity; }
        public int getLineCount() { return lineCount; }
        public void setLineCount(int lineCount) { this.lineCount = lineCount; }
    }
    
    public static class ClassInfo {
        private String name;
        private String parentClass;
        private List<String> interfaces = new ArrayList<>();
        private List<FieldInfo> fields = new ArrayList<>();
        private List<MethodInfo> methods = new ArrayList<>();
        private int complexity;
        private int linesOfCode;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getParentClass() { return parentClass; }
        public void setParentClass(String parentClass) { this.parentClass = parentClass; }
        public List<String> getInterfaces() { return interfaces; }
        public void setInterfaces(List<String> interfaces) { this.interfaces = interfaces; }
        public List<FieldInfo> getFields() { return fields; }
        public void setFields(List<FieldInfo> fields) { this.fields = fields; }
        public List<MethodInfo> getMethods() { return methods; }
        public void setMethods(List<MethodInfo> methods) { this.methods = methods; }
        public int getComplexity() { return complexity; }
        public void setComplexity(int complexity) { this.complexity = complexity; }
        public int getLinesOfCode() { return linesOfCode; }
        public void setLinesOfCode(int linesOfCode) { this.linesOfCode = linesOfCode; }
    }
    
    public static class FieldInfo {
        private String type;
        private String name;
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
    
    public static class MethodCallGraph {
        private Map<String, List<String>> nodes = new HashMap<>();
        
        public void addNode(String method, List<String> calls) {
            nodes.put(method, calls);
        }
        
        public int getNodeCount() { return nodes.size(); }
        public Map<String, List<String>> getNodes() { return nodes; }
    }
    
    public static class CodeSmell {
        private String type;
        private String location;
        private String description;
        
        public CodeSmell(String type, String location, String description) {
            this.type = type;
            this.location = location;
            this.description = description;
        }
        
        public String getType() { return type; }
        public String getLocation() { return location; }
        public String getDescription() { return description; }
    }
    
    public static class CodeDependencies {
        private Set<String> imports = new HashSet<>();
        private Set<String> externalClasses = new HashSet<>();
        
        public void addImport(String imp) { imports.add(imp); }
        public void addExternalClass(String cls) { externalClasses.add(cls); }
        public int getImportCount() { return imports.size(); }
        public int getExternalClassCount() { return externalClasses.size(); }
        public Set<String> getImports() { return imports; }
        public Set<String> getExternalClasses() { return externalClasses; }
    }
}
