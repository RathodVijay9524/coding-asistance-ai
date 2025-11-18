package com.vijay.tools;

import com.vijay.context.GlobalBrainContext;
import com.vijay.context.TraceContext;
import com.vijay.dto.ReasoningState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ToolFinderService {

    private static final Logger logger = LoggerFactory.getLogger(ToolFinderService.class);

    // We can now use the VectorStore INTERFACE
    private final VectorStore vectorStore;

    public ToolFinderService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<String> findToolsFor(String prompt) {
        String traceId = TraceContext.getTraceId();
        
        SearchRequest request = SearchRequest.builder()
                .query(prompt)
                .topK(3)
                //.similarityThreshold(0.7)
                .build();

        List<Document> similarDocuments = vectorStore.similaritySearch(request);

        List<String> toolNames = similarDocuments.stream()
                .map(doc -> (String) doc.getMetadata().get("toolName"))
                .collect(Collectors.toList());

        logger.info("[{}] 🔧 ToolFinder: Found {} tools for prompt", traceId, toolNames.size());
        logger.info("[{}]    Tools: {}", traceId, toolNames);
        
        // PHASE 1 INTEGRATION: Create ReasoningState and store in GlobalBrainContext
        ReasoningState state = new ReasoningState(prompt);
        state.setSuggestedTools(toolNames);
        GlobalBrainContext.setReasoningState(state);
        
        logger.info("[{}]    ✅ ReasoningState created and stored in GlobalBrainContext", traceId);
        
        return toolNames;
    }
}
