package com.vijay.service;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 🧠 INCREMENTAL INDEXER - Phase 8
 * 
 * Purpose: Only re-indexes changed files, uses file hash for detection,
 * performs incremental chunk updates, efficient re-indexing.
 * 
 * Responsibilities:
 * - Detect changed files using hash
 * - Re-index only changed files
 * - Update chunks incrementally
 * - Track indexing state
 * - Provide efficiency metrics
 */
@Service
public class IncrementalIndexer {
    
    private static final Logger logger = LoggerFactory.getLogger(IncrementalIndexer.class);
    
    private final VectorStore chunkStore;
    private final FileHashTracker fileHashTracker;
    private final JavaParser javaParser = new JavaParser();
    
    // Tracking
    private final Map<String, IndexState> indexStates = new HashMap<>();
    
    public IncrementalIndexer(@Qualifier("chunkVectorStore") VectorStore chunkStore,
                             FileHashTracker fileHashTracker) {
        this.chunkStore = chunkStore;
        this.fileHashTracker = fileHashTracker;
    }
    
    /**
     * Incrementally index changed files
     */
    public IncrementalIndexResult indexChangedFiles(List<String> filePaths) {
        IncrementalIndexResult result = new IncrementalIndexResult();
        
        logger.info("🧠 Incremental Indexer: Checking {} files for changes", filePaths.size());
        
        // Get changed files
        List<String> changedFiles = fileHashTracker.getChangedFiles(filePaths);
        List<String> newFiles = fileHashTracker.getNewFiles(filePaths);
        
        result.totalFiles = filePaths.size();
        result.changedFiles = changedFiles.size();
        result.newFiles = newFiles.size();
        
        // Index changed files
        for (String filePath : changedFiles) {
            try {
                int chunksAdded = indexFile(filePath);
                result.chunksIndexed += chunksAdded;
                result.filesProcessed++;
                
                // Update index state
                IndexState state = new IndexState(filePath, chunksAdded);
                indexStates.put(filePath, state);
            } catch (Exception e) {
                logger.error("🧠 Incremental Indexer: Error indexing {}: {}", filePath, e.getMessage());
                result.errors++;
            }
        }
        
        // Index new files
        for (String filePath : newFiles) {
            try {
                int chunksAdded = indexFile(filePath);
                result.chunksIndexed += chunksAdded;
                result.filesProcessed++;
                
                // Update index state
                IndexState state = new IndexState(filePath, chunksAdded);
                indexStates.put(filePath, state);
            } catch (Exception e) {
                logger.error("🧠 Incremental Indexer: Error indexing {}: {}", filePath, e.getMessage());
                result.errors++;
            }
        }
        
        result.duration = System.currentTimeMillis();
        
        logger.info("🧠 Incremental Indexer: Indexed {} files, {} chunks, {} errors in {}ms", 
            result.filesProcessed, result.chunksIndexed, result.errors, result.duration);
        
        return result;
    }
    
    /**
     * Index single file
     */
    private int indexFile(String filePath) throws IOException {
        String content = Files.readString(Paths.get(filePath));
        String fileName = Paths.get(filePath).getFileName().toString();
        
        logger.debug("🧠 Incremental Indexer: Indexing file: {}", fileName);
        
        try {
            CompilationUnit cu = javaParser.parse(content).getResult().orElse(null);
            if (cu == null) {
                logger.debug("Could not parse file: {}", fileName);
                return 0;
            }
            
            int chunkCount = 0;
            String packageName = cu.getPackageDeclaration()
                .map(pd -> pd.getNameAsString())
                .orElse("unknown");
            
            // Index class-level chunks
            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
                try {
                    String className = classDecl.getNameAsString();
                    String classOverview = createClassOverview(classDecl);
                    
                    Document classDoc = new Document(classOverview, Map.of(
                        "type", "class-chunk",
                        "filename", fileName,
                        "package", packageName,
                        "class", className,
                        "chunk_type", "class_overview"
                    ));
                    
                    chunkStore.add(List.of(classDoc));
                } catch (Exception e) {
                    logger.debug("Failed to index class chunk: {}", e.getMessage());
                }
            });
            
            // Index method-level chunks
            cu.findAll(MethodDeclaration.class).forEach(method -> {
                try {
                    String methodName = method.getNameAsString();
                    String methodCode = method.toString();
                    
                    if (methodCode.length() < 50) {
                        return;
                    }
                    
                    String className = findContainingClass(method);
                    
                    Document methodDoc = new Document(methodCode, Map.of(
                        "type", "method-chunk",
                        "filename", fileName,
                        "package", packageName,
                        "class", className,
                        "method", methodName,
                        "chunk_type", "method_implementation",
                        "method_signature", method.getDeclarationAsString()
                    ));
                    
                    chunkStore.add(List.of(methodDoc));
                } catch (Exception e) {
                    logger.debug("Failed to index method chunk: {}", e.getMessage());
                }
            });
            
            chunkCount = cu.findAll(ClassOrInterfaceDeclaration.class).size() + 
                        cu.findAll(MethodDeclaration.class).size();
            
            logger.debug("✅ Created {} chunks for: {}", chunkCount, fileName);
            return chunkCount;
            
        } catch (Exception e) {
            logger.debug("Failed to parse file {}: {}", fileName, e.getMessage());
            return 0;
        }
    }
    
    /**
     * Create class overview
     */
    private String createClassOverview(ClassOrInterfaceDeclaration classDecl) {
        StringBuilder overview = new StringBuilder();
        
        overview.append(classDecl.toString().split("\\{")[0]).append("{\n\n");
        
        classDecl.getFields().forEach(field -> {
            overview.append("    ").append(field.toString()).append("\n");
        });
        
        overview.append("\n    // Methods:\n");
        classDecl.getMethods().forEach(method -> {
            overview.append("    ").append(method.getDeclarationAsString()).append(";\n");
        });
        
        overview.append("\n}");
        
        return overview.toString();
    }
    
    /**
     * Find containing class
     */
    private String findContainingClass(MethodDeclaration method) {
        return method.findAncestor(ClassOrInterfaceDeclaration.class)
            .map(ClassOrInterfaceDeclaration::getNameAsString)
            .orElse("unknown");
    }
    
    /**
     * Get indexing statistics
     */
    public IndexingStatistics getStatistics() {
        int totalChunks = indexStates.values().stream()
            .mapToInt(s -> s.chunksIndexed)
            .sum();
        
        return new IndexingStatistics(
            indexStates.size(),
            totalChunks,
            indexStates.values().stream()
                .mapToLong(s -> s.timestamp)
                .max()
                .orElse(0)
        );
    }
    
    // ============ Inner Classes ============
    
    /**
     * Index state for a file
     */
    public static class IndexState {
        public final String filePath;
        public final int chunksIndexed;
        public final long timestamp;
        
        public IndexState(String filePath, int chunksIndexed) {
            this.filePath = filePath;
            this.chunksIndexed = chunksIndexed;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Incremental index result
     */
    public static class IncrementalIndexResult {
        public int totalFiles = 0;
        public int changedFiles = 0;
        public int newFiles = 0;
        public int filesProcessed = 0;
        public int chunksIndexed = 0;
        public int errors = 0;
        public long duration = 0;
        
        public double getEfficiency() {
            return totalFiles > 0 ? (double) filesProcessed / totalFiles * 100 : 0;
        }
    }
    
    /**
     * Indexing statistics
     */
    public static class IndexingStatistics {
        public final int filesIndexed;
        public final int totalChunks;
        public final long lastIndexTime;
        
        public IndexingStatistics(int filesIndexed, int totalChunks, long lastIndexTime) {
            this.filesIndexed = filesIndexed;
            this.totalChunks = totalChunks;
            this.lastIndexTime = lastIndexTime;
        }
    }
}
