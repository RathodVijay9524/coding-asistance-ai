package com.vijay.codebase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 📁 Codebase Indexer - Scans and indexes all project files
 * 
 * Provides:
 * - Complete file listing
 * - File metadata (size, complexity, type)
 * - Directory structure
 * - File relationships
 */
@Component
public class CodebaseIndexer {
    
    private static final Logger logger = LoggerFactory.getLogger(CodebaseIndexer.class);
    
    private static final Set<String> SOURCE_EXTENSIONS = Set.of(
            ".java", ".js", ".ts", ".py", ".go", ".rs", ".cpp", ".c", ".h"
    );
    
    private static final Set<String> IGNORE_DIRS = Set.of(
            ".git", ".mvn", "node_modules", "target", "build", ".gradle", ".idea", "dist"
    );
    
    /**
     * Index entire project
     */
    public CodebaseIndex indexProject(Path projectRoot) {
        logger.info("📁 Indexing project: {}", projectRoot);
        
        CodebaseIndex index = new CodebaseIndex();
        index.setProjectRoot(projectRoot.toString());
        
        try (Stream<Path> paths = Files.walk(projectRoot)) {
            List<CodeFile> files = paths
                    .filter(Files::isRegularFile)
                    .filter(this::isSourceFile)
                    .filter(this::isNotIgnored)
                    .map(this::indexFile)
                    .collect(Collectors.toList());
            
            index.setFiles(files);
            index.setTotalFiles(files.size());
            
            // Calculate statistics
            index.setTotalLines(files.stream().mapToLong(CodeFile::getLineCount).sum());
            index.setAverageComplexity(files.stream()
                    .mapToDouble(CodeFile::getComplexity)
                    .average()
                    .orElse(0.0));
            
            logger.info("✅ Indexed {} files, {} total lines", files.size(), index.getTotalLines());
            
        } catch (Exception e) {
            logger.error("❌ Error indexing project: {}", e.getMessage(), e);
        }
        
        return index;
    }
    
    /**
     * Index a single file
     */
    private CodeFile indexFile(Path filePath) {
        CodeFile file = new CodeFile();
        file.setPath(filePath.toString());
        file.setName(filePath.getFileName().toString());
        file.setExtension(getExtension(filePath));
        
        try {
            // Get file size
            file.setSize(Files.size(filePath));
            
            // Count lines
            long lineCount = Files.lines(filePath).count();
            file.setLineCount(lineCount);
            
            // Estimate complexity (simple heuristic)
            file.setComplexity(estimateComplexity(filePath, lineCount));
            
            // Check if has tests
            file.setHasTests(hasCorrespondingTest(filePath));
            
        } catch (Exception e) {
            logger.debug("Error indexing file {}: {}", filePath, e.getMessage());
        }
        
        return file;
    }
    
    /**
     * Estimate code complexity (1-10 scale)
     */
    private double estimateComplexity(Path filePath, long lineCount) {
        // Simple heuristic: complexity increases with file size
        if (lineCount < 100) return 1.0;
        if (lineCount < 300) return 3.0;
        if (lineCount < 600) return 5.0;
        if (lineCount < 1000) return 7.0;
        return 9.0;
    }
    
    /**
     * Check if file has corresponding test
     */
    private boolean hasCorrespondingTest(Path filePath) {
        String fileName = filePath.getFileName().toString();
        String testFileName = fileName.replace(".java", "Test.java")
                .replace(".js", ".test.js")
                .replace(".ts", ".test.ts");
        
        Path testPath = filePath.getParent().resolve(testFileName);
        return Files.exists(testPath);
    }
    
    /**
     * Check if file is source code
     */
    private boolean isSourceFile(Path path) {
        String extension = getExtension(path);
        return SOURCE_EXTENSIONS.contains(extension);
    }
    
    /**
     * Check if path should be ignored
     */
    private boolean isNotIgnored(Path path) {
        return path.getNameCount() == 0 || 
               !IGNORE_DIRS.contains(path.getName(0).toString());
    }
    
    /**
     * Get file extension
     */
    private String getExtension(Path path) {
        String fileName = path.getFileName().toString();
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : "";
    }
}
