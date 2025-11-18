package com.vijay.service;

import com.vijay.manager.IAgentBrain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 🧠 PHASE 8: Brain RAG - Brain Indexer Service
 * 
 * Indexes all advisor brains at startup, just like ToolIndexingService.
 * 
 * At startup:
 * 1. Finds all beans implementing IAgentBrain
 * 2. Extracts brain name, description, and order
 * 3. Embeds descriptions using OllamaEmbeddingModel
 * 4. Stores vectors in brainVectorStore
 * 
 * Result: All 13 brains indexed and ready for semantic search
 */
@Service
public class BrainIndexerService implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(BrainIndexerService.class);

    private final VectorStore brainVectorStore;
    private final List<IAgentBrain> allBrains;

    public BrainIndexerService(@Qualifier("brainVectorStore") VectorStore brainVectorStore,
                              List<IAgentBrain> allBrains) {
        this.brainVectorStore = brainVectorStore;
        this.allBrains = allBrains;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("🧠 --- Indexing all {} brains for semantic search... ---", allBrains.size());

        List<Document> brainDocuments = new ArrayList<>();

        for (IAgentBrain brain : allBrains) {
            String beanName = brain.getBrainName();  // This is the ACTUAL Spring bean name
            String description = brain.getBrainDescription();
            int order = brain.getOrder();

            // Create a document for this brain
            // IMPORTANT: Store the bean name (not class name) so we can retrieve it later
            Document brainDoc = new Document(
                    description,  // Content to embed
                    Map.of(
                            "brainName", beanName,  // ← This is what ApplicationContext.getBean() needs
                            "order", String.valueOf(order)
                    )
            );

            brainDocuments.add(brainDoc);
            logger.info("   📌 Indexing brain: {} (order: {}, desc: '{}')", 
                    beanName, 
                    order,
                    description.substring(0, Math.min(60, description.length())));
        }

        if (!brainDocuments.isEmpty()) {
            brainVectorStore.add(brainDocuments);
            logger.info("✅ --- Indexed {} brains to brainVectorStore. Ready for semantic search! ---", 
                    brainDocuments.size());
        } else {
            logger.warn("⚠️ --- No brains implementing IAgentBrain found! ---");
        }
    }
}
