package com.vijay.service;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

@Service
public class DependencyGraphBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DependencyGraphBuilder.class);
    
    private final Map<String, Set<String>> fileToRelatedFiles = new HashMap<>();
    private final Map<String, Set<String>> methodToFiles = new HashMap<>();
    private final JavaParser javaParser = new JavaParser();

    @PostConstruct
    public void buildGraph() {
        logger.info("🔗 Building dependency graph for code understanding...");
        
        try {
            Path srcPath = Paths.get("src/main/java");
            if (!Files.exists(srcPath)) {
                logger.warn("Source path not found: {}", srcPath);
                return;
            }

            // First pass: collect all methods and their files
            collectMethodMappings(srcPath);
            
            // Second pass: analyze dependencies
            analyzeDependencies(srcPath);
            
            logger.info("✅ Dependency graph built successfully!");
            logger.info("📊 Graph stats: {} files, {} relationships", 
                fileToRelatedFiles.size(), 
                fileToRelatedFiles.values().stream().mapToInt(Set::size).sum());
                
        } catch (Exception e) {
            logger.error("❌ Failed to build dependency graph", e);
        }
    }

    private void collectMethodMappings(Path srcPath) throws IOException {
        try (Stream<Path> paths = Files.walk(srcPath)) {
            paths.filter(path -> path.toString().endsWith(".java"))
                 .forEach(this::collectMethodsFromFile);
        }
    }

    private void collectMethodsFromFile(Path file) {
        try {
            String content = Files.readString(file);
            CompilationUnit cu = javaParser.parse(content).getResult().orElse(null);
            
            if (cu == null) return;
            
            String fileName = file.getFileName().toString();
            
            // Collect all method names in this file
            cu.findAll(com.github.javaparser.ast.body.MethodDeclaration.class)
              .forEach(method -> {
                  String methodName = method.getNameAsString();
                  methodToFiles.computeIfAbsent(methodName, k -> new HashSet<>()).add(fileName);
              });
              
        } catch (Exception e) {
            logger.debug("Failed to parse file for methods: {}", file.getFileName());
        }
    }

    private void analyzeDependencies(Path srcPath) throws IOException {
        try (Stream<Path> paths = Files.walk(srcPath)) {
            paths.filter(path -> path.toString().endsWith(".java"))
                 .forEach(this::analyzeFile);
        }
    }

    private void analyzeFile(Path file) {
        try {
            String content = Files.readString(file);
            CompilationUnit cu = javaParser.parse(content).getResult().orElse(null);
            
            if (cu == null) return;
            
            String currentFile = file.getFileName().toString();
            Set<String> relatedFiles = new HashSet<>();

            // Extract imports from com.vijay package
            cu.getImports().forEach(imp -> {
                String importedClass = imp.getNameAsString();
                if (importedClass.startsWith("com.vijay")) {
                    String relatedFile = extractClassNameFromImport(importedClass) + ".java";
                    relatedFiles.add(relatedFile);
                }
            });

            // Extract method calls and map to files
            cu.findAll(MethodCallExpr.class).forEach(call -> {
                String methodName = call.getNameAsString();
                Set<String> filesWithMethod = methodToFiles.get(methodName);
                if (filesWithMethod != null) {
                    relatedFiles.addAll(filesWithMethod);
                }
            });

            // Remove self-reference
            relatedFiles.remove(currentFile);
            
            fileToRelatedFiles.put(currentFile, relatedFiles);
            
            if (!relatedFiles.isEmpty()) {
                logger.debug("📁 {} depends on: {}", currentFile, relatedFiles);
            }
            
        } catch (Exception e) {
            logger.debug("Failed to analyze file: {}", file.getFileName());
        }
    }

    private String extractClassNameFromImport(String importPath) {
        String[] parts = importPath.split("\\.");
        return parts[parts.length - 1];
    }

    public Set<String> getDependencies(String filename) {
        return fileToRelatedFiles.getOrDefault(filename, Collections.emptySet());
    }

    public Set<String> getReverseDependencies(String filename) {
        Set<String> reverseDeps = new HashSet<>();
        
        for (Map.Entry<String, Set<String>> entry : fileToRelatedFiles.entrySet()) {
            if (entry.getValue().contains(filename)) {
                reverseDeps.add(entry.getKey());
            }
        }
        
        return reverseDeps;
    }

    public Map<String, Set<String>> getAllDependencies() {
        return new HashMap<>(fileToRelatedFiles);
    }

    public void printGraph() {
        logger.info("🔗 Dependency Graph:");
        fileToRelatedFiles.forEach((file, deps) -> {
            if (!deps.isEmpty()) {
                logger.info("  {} → {}", file, deps);
            }
        });
    }
}
