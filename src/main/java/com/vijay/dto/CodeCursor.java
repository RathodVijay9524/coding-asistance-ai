package com.vijay.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a cursor position in code
 * Tracks file, line, scope, and surrounding context
 */
public class CodeCursor {
    private String filePath;
    private int lineNumber;
    private String scope; // "class.method" or "class"
    private String language; // "java", "python", etc.
    private String contextCode; // Code around cursor
    private List<String> dependencies = new ArrayList<>();
    private List<String> imports = new ArrayList<>();
    private String className;
    private String methodName;
    
    // ===== Getters and Setters =====
    
    public String getFilePath() {
        return filePath;
    }
    
    public CodeCursor setFile(String filePath) {
        this.filePath = filePath;
        return this;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public CodeCursor setLine(int lineNumber) {
        this.lineNumber = lineNumber;
        return this;
    }
    
    public String getScope() {
        return scope;
    }
    
    public CodeCursor setScope(String scope) {
        this.scope = scope;
        return this;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public CodeCursor setLanguage(String language) {
        this.language = language;
        return this;
    }
    
    public String getContextCode() {
        return contextCode;
    }
    
    public CodeCursor setContext(String contextCode) {
        this.contextCode = contextCode;
        return this;
    }
    
    public List<String> getDependencies() {
        return dependencies;
    }
    
    public CodeCursor setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
        return this;
    }
    
    public List<String> getImports() {
        return imports;
    }
    
    public CodeCursor setImports(List<String> imports) {
        this.imports = imports;
        return this;
    }
    
    public String getClassName() {
        return className;
    }
    
    public CodeCursor setClassName(String className) {
        this.className = className;
        return this;
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public CodeCursor setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }
    
    @Override
    public String toString() {
        return String.format("CodeCursor{file=%s, line=%d, scope=%s, language=%s}",
            filePath, lineNumber, scope, language);
    }
}
