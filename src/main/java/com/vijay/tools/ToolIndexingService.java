package com.vijay.tools;

import com.vijay.manager.AiToolProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ToolIndexingService implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(ToolIndexingService.class);

    private final VectorStore vectorStore;
    private final List<AiToolProvider> allToolProviders;

    public ToolIndexingService(VectorStore vectorStore, List<AiToolProvider> allToolProviders) {
        this.vectorStore = vectorStore;
        this.allToolProviders = allToolProviders;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("--- Indexing all @Tool methods from {} providers... ---", allToolProviders.size());

        List<Document> toolDocuments = new ArrayList<>();
        for (AiToolProvider provider : allToolProviders) {

            // --- FIX 1: This is now an Array (ToolCallback[]) ---
            // As shown in your screenshot
            ToolCallback[] tools = ToolCallbacks.from(provider);

            // Loop through the array
            for (ToolCallback tool : tools) {

                // --- FIX 2 & 3 ---
                // In v1.0.3, you must use .getToolDefinition()
                String toolName = tool.getToolDefinition().name();
                String description = tool.getToolDefinition().description();

                Document toolDoc = new Document(
                        description,
                        Map.of("toolName", toolName)
                );
                toolDocuments.add(toolDoc);
                logger.info("Indexing tool: {}", toolName);
            }
        }

        if (!toolDocuments.isEmpty()) {
            vectorStore.add(toolDocuments);
            logger.info("--- Indexed {} tools to the Vector Store. ---", toolDocuments.size());
        } else {
            logger.warn("--- No @Tool methods found! ---");
        }
    }
}