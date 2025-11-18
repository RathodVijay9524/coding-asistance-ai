package com.vijay.service;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@Service
public class CodeChunkIndexer {

    private static final Logger logger = LoggerFactory.getLogger(CodeChunkIndexer.class);
    
    private final VectorStore chunkStore;
    private final EmbeddingCacheManager cacheManager;
    private final JavaParser javaParser = new JavaParser();
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    public CodeChunkIndexer(@Qualifier("chunkVectorStore") VectorStore chunkStore,
                           EmbeddingCacheManager cacheManager) {
        this.chunkStore = chunkStore;
        this.cacheManager = cacheManager;
    }

    @PostConstruct
    public void indexCodeChunks() {
        logger.info("🧩 Starting code chunk indexing...");
        
        try {
            Path srcPath = Paths.get("src/main/java");
            if (!Files.exists(srcPath)) {
                logger.warn("Source path not found: {}", srcPath);
                return;
            }

            // Check if cache already exists FIRST (before calculating hash)
            if (cacheManager.cacheFileExists()) {
                logger.info("✅ Cache file exists - SKIPPING chunk re-embedding (fast startup!)");
                return;
            }
            
            // Get file list ONCE and reuse it
            List<String> javaFilePaths;
            try (Stream<Path> paths = Files.walk(srcPath)) {
                javaFilePaths = paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !path.toString().contains("test"))
                    .map(Path::toString)
                    .toList();
            }
            
            logger.info("📁 Found {} Java files", javaFilePaths.size());
            
            // Calculate hash from the SAME file list
            String documentsHash = cacheManager.calculateDocumentsHash(javaFilePaths);
            
            logger.info("🔄 Cache invalid or missing - re-embedding chunks...");
            
            // Proceed with async embedding
            final String finalHash = documentsHash;
            CompletableFuture.runAsync(() -> {
                try {
                    int totalChunks = 0;
                    for (String filePath : javaFilePaths) {
                        try {
                            int chunks = chunkFile(Paths.get(filePath));
                            totalChunks += chunks;
                        } catch (Exception e) {
                            logger.error("Failed to chunk file: {}", filePath, e);
                        }
                    }

                    logger.info("✅ Code chunk indexing completed! {} chunks created", totalChunks);
                    
                    // Save cache after successful embedding
                    if (finalHash != null) {
                        cacheManager.saveToCache(chunkStore, finalHash);
                        logger.info("💾 Chunk cache saved for future startups");
                    }
                    
                } catch (Exception e) {
                    logger.error("❌ Failed to index code chunks", e);
                }
            }, executorService);
            
        } catch (Exception e) {
            logger.error("❌ Error in indexCodeChunks: {}", e.getMessage(), e);
        }
    }

    private int chunkFile(Path file) throws IOException {
        String content = Files.readString(file);
        String fileName = file.getFileName().toString();
        
        logger.debug("🧩 Chunking file: {}", fileName);

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
                    String classCode = classDecl.toString();
                    
                    // Create class overview chunk (without method bodies)
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
                    
                    // Skip very small methods (getters/setters)
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

    private String createClassOverview(ClassOrInterfaceDeclaration classDecl) {
        StringBuilder overview = new StringBuilder();
        
        // Class declaration
        overview.append(classDecl.toString().split("\\{")[0]).append("{\n\n");
        
        // Fields
        classDecl.getFields().forEach(field -> {
            overview.append("    ").append(field.toString()).append("\n");
        });
        
        // Method signatures only
        overview.append("\n    // Methods:\n");
        classDecl.getMethods().forEach(method -> {
            overview.append("    ").append(method.getDeclarationAsString()).append(";\n");
        });
        
        overview.append("\n}");
        
        return overview.toString();
    }

    private String findContainingClass(MethodDeclaration method) {
        return method.findAncestor(ClassOrInterfaceDeclaration.class)
            .map(ClassOrInterfaceDeclaration::getNameAsString)
            .orElse("unknown");
    }

    public void reindexFile(String filename) {
        logger.info("🔄 Re-indexing chunks for file: {}", filename);
        
        try {
            Path file = Paths.get("src/main/java").resolve(filename);
            if (Files.exists(file)) {
                chunkFile(file);
            }
        } catch (Exception e) {
            logger.error("Failed to re-index chunks for file: {}", filename, e);
        }
    }

    public long getIndexedChunkCount() {
        // This would require implementing a count method in VectorStore
        // For now, return estimated count
        try {
            return chunkStore.similaritySearch(
                SearchRequest.builder()
                    .query("java")
                    .topK(1000)
                    .build()
            ).size();
        } catch (Exception e) {
            logger.debug("Could not get indexed chunk count: {}", e.getMessage());
            return 0;
        }
    }
}
