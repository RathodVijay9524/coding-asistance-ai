package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
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
public class CodeSummaryIndexer {

    private static final Logger logger = LoggerFactory.getLogger(CodeSummaryIndexer.class);
    
    private final VectorStore summaryStore;
    private final ChatClient chatClient;
    private final EmbeddingCacheManager cacheManager;
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    public CodeSummaryIndexer(@Qualifier("summaryVectorStore") VectorStore summaryStore,
                             OpenAiChatModel chatModel,
                             EmbeddingCacheManager cacheManager) {
        this.summaryStore = summaryStore;
        this.chatClient = ChatClient.builder(chatModel).build();
        this.cacheManager = cacheManager;
    }

    @PostConstruct
    public void indexCodeSummaries() {
        logger.info("📚 Starting code summary indexing...");
        
        try {
            Path srcPath = Paths.get("src/main/java");
            if (!Files.exists(srcPath)) {
                logger.warn("Source path not found: {}", srcPath);
                return;
            }

            // Check if cache already exists FIRST (before calculating hash)
            if (cacheManager.cacheFileExists()) {
                logger.info("✅ Cache file exists - SKIPPING summary re-embedding (fast startup!)");
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
            
            logger.info("🔄 Cache invalid or missing - re-embedding summaries...");
            
            // Proceed with async embedding
            final String finalHash = documentsHash;
            CompletableFuture.runAsync(() -> {
                try {
                    for (String filePath : javaFilePaths) {
                        try {
                            indexFile(Paths.get(filePath));
                            Thread.sleep(100); // Rate limiting
                        } catch (Exception e) {
                            logger.error("Failed to index file: {}", filePath, e);
                        }
                    }

                    logger.info("✅ Code summary indexing completed!");
                    
                    // Save cache after successful embedding
                    if (finalHash != null) {
                        cacheManager.saveToCache(summaryStore, finalHash);
                        logger.info("💾 Summary cache saved for future startups");
                    }
                    
                } catch (Exception e) {
                    logger.error("❌ Failed to index code summaries", e);
                }
            }, executorService);
            
        } catch (Exception e) {
            logger.error("❌ Error in indexCodeSummaries: {}", e.getMessage(), e);
        }
    }

    private void indexFile(Path file) throws IOException {
        String content = Files.readString(file);
        
        // Skip empty files or files that are too small
        if (content.trim().length() < 100) {
            return;
        }

        String fileName = file.getFileName().toString();
        String packageName = extractPackageName(content);
        
        logger.debug("📄 Indexing summary for: {}", fileName);

        String summaryPrompt = String.format("""
            Analyze this Java file and provide a structured summary:
            
            File: %s
            Package: %s
            
            Content:
            %s
            
            Provide a concise summary covering:
            1. Main purpose (1-2 sentences)
            2. Key classes/interfaces defined
            3. Important public methods
            4. Dependencies on other com.vijay classes
            5. What functionality this provides to other files
            
            Keep it under 300 words and focus on what a developer would need to know.
            """, fileName, packageName, truncateContent(content, 4000));

        try {
            String summary = chatClient.prompt(summaryPrompt).call().content();
            
            Document doc = new Document(summary, Map.of(
                "type", "file-summary",
                "filename", fileName,
                "path", file.toString(),
                "package", packageName,
                "size", String.valueOf(content.length()),
                "indexed_at", String.valueOf(System.currentTimeMillis())
            ));

            summaryStore.add(List.of(doc));
            
            logger.debug("✅ Indexed summary for: {}", fileName);
            
        } catch (Exception e) {
            logger.error("Failed to generate summary for {}: {}", fileName, e.getMessage());
        }
    }

    private String extractPackageName(String content) {
        return content.lines()
            .filter(line -> line.trim().startsWith("package "))
            .findFirst()
            .map(line -> line.replace("package ", "").replace(";", "").trim())
            .orElse("unknown");
    }

    private String truncateContent(String content, int maxLength) {
        if (content.length() <= maxLength) {
            return content;
        }
        
        // Try to truncate at a reasonable point (end of method or class)
        String truncated = content.substring(0, maxLength);
        int lastBrace = truncated.lastIndexOf('}');
        
        if (lastBrace > maxLength * 0.7) {
            return truncated.substring(0, lastBrace + 1) + "\n\n// ... (truncated)";
        }
        
        return truncated + "\n\n// ... (truncated)";
    }

    public void reindexFile(String filename) {
        logger.info("🔄 Re-indexing file: {}", filename);
        
        try {
            Path file = Paths.get("src/main/java").resolve(filename);
            if (Files.exists(file)) {
                indexFile(file);
            }
        } catch (Exception e) {
            logger.error("Failed to re-index file: {}", filename, e);
        }
    }

    public long getIndexedFileCount() {
        // This would require implementing a count method in VectorStore
        // For now, return estimated count
        try {
            return summaryStore.similaritySearch(
                SearchRequest.builder()
                    .query("java")
                    .topK(1000)
                    .build()
            ).size();
        } catch (Exception e) {
            logger.debug("Could not get indexed file count: {}", e.getMessage());
            return 0;
        }
    }
}
