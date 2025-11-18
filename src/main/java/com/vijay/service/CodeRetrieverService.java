package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CodeRetrieverService {

    private static final Logger logger = LoggerFactory.getLogger(CodeRetrieverService.class);
    
    private final VectorStore summaryStore;
    private final VectorStore chunkStore;
    private final DependencyGraphBuilder dependencyGraph;
    private final ContextManager contextManager;
    private final QueryPlanner queryPlanner;

    public CodeRetrieverService(@Qualifier("summaryVectorStore") VectorStore summaryStore,
                               @Qualifier("chunkVectorStore") VectorStore chunkStore,
                               DependencyGraphBuilder dependencyGraph,
                               ContextManager contextManager,
                               QueryPlanner queryPlanner) {
        this.summaryStore = summaryStore;
        this.chunkStore = chunkStore;
        this.dependencyGraph = dependencyGraph;
        this.contextManager = contextManager;
        this.queryPlanner = queryPlanner;
    }

    public CodeContext retrieveCodeContext(String query) {
        logger.info("🔍 Brain 1 (Code Retriever): Searching for code context - '{}'", query);
        
        // Use intelligent query planning
        return retrieveCodeContextWithPlan(query);
    }

    public CodeContext retrieveCodeContextWithPlan(String query) {
        logger.info("🎯 Brain 1 (Intelligent Code Retriever): Using search plan for - '{}'", query);
        
        // Step 0: Create intelligent search plan
        QueryPlanner.SearchPlan plan = queryPlanner.createSearchPlan(query);
        ContextManager.ContextBudget budget = contextManager.createBudget(query);
        
        // Override budget with plan's allocation
        budget.maxTokens = plan.tokenBudget;
        budget.remainingTokens = plan.tokenBudget - budget.usedTokens;
        
        CodeContext context = new CodeContext();
        
        try {
            // Step 1: Execute search strategy based on plan
            List<Document> fileSummaries = executeSearchStrategy(plan, budget);
            logger.info("📁 Found {} relevant files using {} strategy", 
                fileSummaries.size(), plan.searchStrategy);
            
            if (fileSummaries.isEmpty()) {
                logger.info("⚠️ No relevant files found for query: {}", query);
                return context;
            }
            
            // Step 2: Expand using plan parameters
            Set<String> allRelevantFiles = expandWithPlan(fileSummaries, plan, budget);
            logger.info("🔗 Expanded to {} files (maxHops: {}, reverseDeps: {})", 
                allRelevantFiles.size(), plan.maxHops, plan.includeReverseDeps);
            logger.info("💰 Token Budget after expansion: {}/{} tokens ({:.1f}%)", 
                budget.usedTokens, budget.maxTokens, budget.getUsagePercentage());
            
            // Step 3: Get detailed code chunks with plan-based selection
            List<Document> codeChunks = retrieveCodeChunksWithPlan(plan, allRelevantFiles, budget);
            logger.info("🧩 Retrieved {} code chunks", codeChunks.size());
            logger.info("💰 Final Token Budget: {}/{} tokens ({:.1f}%)", 
                budget.usedTokens, budget.maxTokens, budget.getUsagePercentage());
            
            if (budget.isNearLimit()) {
                logger.warn("⚠️ Context near token limit - consider reducing scope");
            }
            
            // Step 4: Build context with plan metadata
            context.setFileSummaries(fileSummaries);
            context.setCodeChunks(codeChunks);
            context.setRelevantFiles(allRelevantFiles);
            context.setQuery(query);
            context.setTokensUsed(budget.usedTokens);
            context.setSearchStrategy(plan.searchStrategy);
            context.setSearchConfidence(plan.confidence);
            
            logger.info("✅ Brain 1 (Intelligent Code Retriever): Context built successfully");
            
        } catch (Exception e) {
            logger.error("❌ Brain 1 (Intelligent Code Retriever): Failed to retrieve context", e);
        }
        
        return context;
    }

    private List<Document> executeSearchStrategy(QueryPlanner.SearchPlan plan, ContextManager.ContextBudget budget) {
        switch (plan.searchStrategy) {
            case "entity_centered":
                return executeEntityCenteredSearch(plan, budget);
            case "dependency_graph":
                return executeDependencyGraphSearch(plan, budget);
            case "method_focused":
                return executeMethodFocusedSearch(plan, budget);
            case "error_trace":
                return executeErrorTraceSearch(plan, budget);
            case "configuration_chain":
                return executeConfigurationChainSearch(plan, budget);
            default: // similarity_search
                return executeSimilaritySearch(plan, budget);
        }
    }

    private List<Document> executeEntityCenteredSearch(QueryPlanner.SearchPlan plan, ContextManager.ContextBudget budget) {
        List<Document> results = new ArrayList<>();
        
        // First try to find files by entity names
        for (String entity : plan.targetEntities) {
            List<Document> entityResults = summaryStore.similaritySearch(
                SearchRequest.builder()
                    .query(entity)
                    .topK(2)
                    .build()
            );
            results.addAll(entityResults);
        }
        
        // If no entity-specific results, fall back to query search
        if (results.isEmpty()) {
            results = summaryStore.similaritySearch(
                SearchRequest.builder()
                    .query(plan.originalQuery)
                    .topK(plan.topK)
                    .build()
            );
        }
        
        return results.stream().distinct().limit(plan.topK).collect(Collectors.toList());
    }

    private List<Document> executeDependencyGraphSearch(QueryPlanner.SearchPlan plan, ContextManager.ContextBudget budget) {
        // Start with broader search for architecture queries
        return summaryStore.similaritySearch(
            SearchRequest.builder()
                .query(plan.originalQuery)
                .topK(plan.topK)
                .build()
        );
    }

    private List<Document> executeMethodFocusedSearch(QueryPlanner.SearchPlan plan, ContextManager.ContextBudget budget) {
        // Focus on implementation details
        String enhancedQuery = plan.originalQuery + " implementation method function";
        return summaryStore.similaritySearch(
            SearchRequest.builder()
                .query(enhancedQuery)
                .topK(plan.topK)
                .build()
        );
    }

    private List<Document> executeErrorTraceSearch(QueryPlanner.SearchPlan plan, ContextManager.ContextBudget budget) {
        // Look for error handling and exception patterns
        String enhancedQuery = plan.originalQuery + " error exception handling try catch";
        return summaryStore.similaritySearch(
            SearchRequest.builder()
                .query(enhancedQuery)
                .topK(plan.topK)
                .build()
        );
    }

    private List<Document> executeConfigurationChainSearch(QueryPlanner.SearchPlan plan, ContextManager.ContextBudget budget) {
        // Focus on configuration files and setup
        String enhancedQuery = plan.originalQuery + " configuration config setup bean";
        return summaryStore.similaritySearch(
            SearchRequest.builder()
                .query(enhancedQuery)
                .topK(plan.topK)
                .build()
        );
    }

    private List<Document> executeSimilaritySearch(QueryPlanner.SearchPlan plan, ContextManager.ContextBudget budget) {
        // Standard similarity search
        return summaryStore.similaritySearch(
            SearchRequest.builder()
                .query(plan.originalQuery)
                .topK(plan.topK)
                .build()
        );
    }

    private Set<String> expandWithPlan(List<Document> fileSummaries, QueryPlanner.SearchPlan plan, ContextManager.ContextBudget budget) {
        Set<String> allFiles = new HashSet<>();
        Set<String> visited = new HashSet<>();
        Queue<String> toExplore = new LinkedList<>();
        
        // Start with initially found files
        List<String> initialFiles = new ArrayList<>();
        for (Document doc : fileSummaries) {
            String filename = (String) doc.getMetadata().get("filename");
            if (filename != null) {
                initialFiles.add(filename);
            }
        }
        
        // Add plan's starting files if specified
        if (!plan.startingFiles.isEmpty()) {
            initialFiles.addAll(plan.startingFiles);
        }
        
        // Prioritize files by relevance
        List<String> prioritizedFiles = contextManager.prioritizeFiles(initialFiles, plan.originalQuery, budget);
        
        for (String filename : prioritizedFiles) {
            allFiles.add(filename);
            toExplore.add(filename);
        }
        
        // Expand using plan parameters
        int currentDepth = 0;
        int maxDepth = budget.isNearLimit() ? Math.max(1, plan.maxHops - 1) : plan.maxHops;
        
        while (!toExplore.isEmpty() && currentDepth < maxDepth && !budget.isOverLimit()) {
            int levelSize = toExplore.size();
            
            for (int i = 0; i < levelSize && !budget.isOverLimit(); i++) {
                String currentFile = toExplore.poll();
                if (visited.contains(currentFile)) continue;
                
                visited.add(currentFile);
                
                // Get dependencies
                Set<String> dependencies = dependencyGraph.getDependencies(currentFile);
                List<String> relevantDeps = contextManager.prioritizeFiles(
                    new ArrayList<>(dependencies), plan.originalQuery, budget);
                
                int maxDepsToAdd = budget.isNearLimit() ? 2 : 4;
                int addedDeps = 0;
                
                for (String dep : relevantDeps) {
                    if (!visited.contains(dep) && addedDeps < maxDepsToAdd) {
                        allFiles.add(dep);
                        toExplore.add(dep);
                        addedDeps++;
                    }
                }
                
                // Get reverse dependencies if plan allows
                if (plan.includeReverseDeps) {
                    Set<String> reverseDeps = dependencyGraph.getReverseDependencies(currentFile);
                    List<String> relevantReverseDeps = contextManager.prioritizeFiles(
                        new ArrayList<>(reverseDeps), plan.originalQuery, budget);
                    
                    int maxReverseDepsToAdd = budget.isNearLimit() ? 1 : 2;
                    int addedReverseDeps = 0;
                    
                    for (String revDep : relevantReverseDeps) {
                        if (!visited.contains(revDep) && addedReverseDeps < maxReverseDepsToAdd) {
                            allFiles.add(revDep);
                            toExplore.add(revDep);
                            addedReverseDeps++;
                        }
                    }
                }
            }
            
            currentDepth++;
        }
        
        logger.debug("🔗 Plan-based expansion: {} → {} files (depth: {}, strategy: {})", 
            fileSummaries.size(), allFiles.size(), currentDepth, plan.searchStrategy);
        
        return allFiles;
    }

    private List<Document> retrieveCodeChunksWithPlan(QueryPlanner.SearchPlan plan, Set<String> relevantFiles, ContextManager.ContextBudget budget) {
        List<Document> allChunks = new ArrayList<>();
        
        // Adjust search parameters based on plan
        int topK = budget.isNearLimit() ? Math.max(3, plan.topK - 2) : plan.topK;
        
        // Create enhanced query based on plan
        String searchQuery = createEnhancedQuery(plan);
        
        List<Document> queryChunks = chunkStore.similaritySearch(
            SearchRequest.builder()
                .query(searchQuery)
                .topK(topK)
                .build()
        );
        
        // Filter chunks to only include those from relevant files
        List<Document> filteredChunks = queryChunks.stream()
            .filter(chunk -> {
                String chunkFilename = (String) chunk.getMetadata().get("filename");
                return relevantFiles.contains(chunkFilename);
            })
            .collect(Collectors.toList());
        
        // Convert to content strings for budget management
        List<String> chunkContents = filteredChunks.stream()
            .map(Document::getText)
            .collect(Collectors.toList());
        
        // Apply budget management and pruning
        List<String> prunedContents = contextManager.pruneContent(chunkContents, budget, plan.originalQuery);
        
        // Convert back to Documents
        for (int i = 0; i < Math.min(filteredChunks.size(), prunedContents.size()); i++) {
            if (prunedContents.contains(filteredChunks.get(i).getText())) {
                allChunks.add(filteredChunks.get(i));
            }
        }
        
        logger.info("📊 Plan-based chunk retrieval: {} chunks selected (strategy: {})", 
            allChunks.size(), plan.searchStrategy);
        
        return allChunks.stream().distinct().collect(Collectors.toList());
    }

    private String createEnhancedQuery(QueryPlanner.SearchPlan plan) {
        StringBuilder enhancedQuery = new StringBuilder(plan.originalQuery);
        
        // Add relevant keywords based on strategy
        switch (plan.searchStrategy) {
            case "method_focused":
                enhancedQuery.append(" method implementation function");
                break;
            case "error_trace":
                enhancedQuery.append(" error exception handling");
                break;
            case "configuration_chain":
                enhancedQuery.append(" configuration setup bean");
                break;
            case "dependency_graph":
                enhancedQuery.append(" architecture relationship dependency");
                break;
        }
        
        // Add target entities to boost relevance
        for (String entity : plan.targetEntities) {
            enhancedQuery.append(" ").append(entity);
        }
        
        return enhancedQuery.toString();
    }

    private Set<String> expandWithDependencies(List<Document> fileSummaries, int maxDepth) {
        Set<String> allFiles = new HashSet<>();
        Set<String> visited = new HashSet<>();
        Queue<String> toExplore = new LinkedList<>();
        
        // Start with initially found files
        for (Document doc : fileSummaries) {
            String filename = (String) doc.getMetadata().get("filename");
            if (filename != null) {
                allFiles.add(filename);
                toExplore.add(filename);
            }
        }
        
        // Expand using dependency graph
        int currentDepth = 0;
        while (!toExplore.isEmpty() && currentDepth < maxDepth) {
            int levelSize = toExplore.size();
            
            for (int i = 0; i < levelSize; i++) {
                String currentFile = toExplore.poll();
                if (visited.contains(currentFile)) continue;
                
                visited.add(currentFile);
                
                // Get dependencies (files this file depends on)
                Set<String> dependencies = dependencyGraph.getDependencies(currentFile);
                for (String dep : dependencies) {
                    if (!visited.contains(dep)) {
                        allFiles.add(dep);
                        toExplore.add(dep);
                    }
                }
                
                // Get reverse dependencies (files that depend on this file)
                Set<String> reverseDeps = dependencyGraph.getReverseDependencies(currentFile);
                for (String revDep : reverseDeps) {
                    if (!visited.contains(revDep)) {
                        allFiles.add(revDep);
                        toExplore.add(revDep);
                    }
                }
            }
            
            currentDepth++;
        }
        
        logger.debug("🔗 Dependency expansion: {} → {} files (depth: {})", 
            fileSummaries.size(), allFiles.size(), currentDepth);
        
        return allFiles;
    }

    private Set<String> expandWithDependenciesAndBudget(List<Document> fileSummaries, String query, ContextManager.ContextBudget budget) {
        Set<String> allFiles = new HashSet<>();
        Set<String> visited = new HashSet<>();
        Queue<String> toExplore = new LinkedList<>();
        
        // Start with initially found files
        List<String> initialFiles = new ArrayList<>();
        for (Document doc : fileSummaries) {
            String filename = (String) doc.getMetadata().get("filename");
            if (filename != null) {
                initialFiles.add(filename);
            }
        }
        
        // Prioritize files by relevance before expansion
        List<String> prioritizedFiles = contextManager.prioritizeFiles(initialFiles, query, budget);
        
        for (String filename : prioritizedFiles) {
            allFiles.add(filename);
            toExplore.add(filename);
        }
        
        // Expand using dependency graph with budget awareness
        int currentDepth = 0;
        int maxDepth = budget.isNearLimit() ? 1 : 2; // Reduce depth if budget is tight
        
        while (!toExplore.isEmpty() && currentDepth < maxDepth && !budget.isOverLimit()) {
            int levelSize = toExplore.size();
            
            for (int i = 0; i < levelSize && !budget.isOverLimit(); i++) {
                String currentFile = toExplore.poll();
                if (visited.contains(currentFile)) continue;
                
                visited.add(currentFile);
                
                // Get dependencies with relevance filtering
                Set<String> dependencies = dependencyGraph.getDependencies(currentFile);
                List<String> relevantDeps = contextManager.prioritizeFiles(
                    new ArrayList<>(dependencies), query, budget);
                
                // Add only top relevant dependencies to avoid budget explosion
                int maxDepsToAdd = budget.isNearLimit() ? 2 : 5;
                int addedDeps = 0;
                
                for (String dep : relevantDeps) {
                    if (!visited.contains(dep) && addedDeps < maxDepsToAdd) {
                        allFiles.add(dep);
                        toExplore.add(dep);
                        addedDeps++;
                    }
                }
                
                // Get reverse dependencies (more selective)
                Set<String> reverseDeps = dependencyGraph.getReverseDependencies(currentFile);
                List<String> relevantReverseDeps = contextManager.prioritizeFiles(
                    new ArrayList<>(reverseDeps), query, budget);
                
                // Add only top reverse dependencies
                int maxReverseDepsToAdd = budget.isNearLimit() ? 1 : 3;
                int addedReverseDeps = 0;
                
                for (String revDep : relevantReverseDeps) {
                    if (!visited.contains(revDep) && addedReverseDeps < maxReverseDepsToAdd) {
                        allFiles.add(revDep);
                        toExplore.add(revDep);
                        addedReverseDeps++;
                    }
                }
            }
            
            currentDepth++;
        }
        
        logger.debug("🔗 Budget-aware expansion: {} → {} files (depth: {}, budget: {:.1f}%)", 
            fileSummaries.size(), allFiles.size(), currentDepth, budget.getUsagePercentage());
        
        return allFiles;
    }

    private List<Document> retrieveCodeChunks(String query, Set<String> relevantFiles) {
        List<Document> allChunks = new ArrayList<>();
        
        // Search for chunks related to the query
        List<Document> queryChunks = chunkStore.similaritySearch(
            SearchRequest.builder()
                .query(query)
                .topK(10)
                .build()
        );
        
        // Filter chunks to only include those from relevant files
        List<Document> filteredChunks = queryChunks.stream()
            .filter(chunk -> {
                String filename = (String) chunk.getMetadata().get("filename");
                return filename != null && relevantFiles.contains(filename);
            })
            .collect(Collectors.toList());
        
        allChunks.addAll(filteredChunks);
        
        // Also get specific chunks for each relevant file
        for (String filename : relevantFiles) {
            List<Document> fileChunks = chunkStore.similaritySearch(
                SearchRequest.builder()
                    .query(filename + " " + query)
                    .topK(3)
                    .build()
            );
            
            // Add chunks that aren't already included
            for (Document chunk : fileChunks) {
                String chunkFilename = (String) chunk.getMetadata().get("filename");
                if (filename.equals(chunkFilename) && !allChunks.contains(chunk)) {
                    allChunks.add(chunk);
                }
            }
        }
        
        // Sort by relevance and limit
        return allChunks.stream()
            .distinct()
            .limit(15)
            .collect(Collectors.toList());
    }

    private List<Document> retrieveCodeChunksWithBudget(String query, Set<String> relevantFiles, ContextManager.ContextBudget budget) {
        List<Document> allChunks = new ArrayList<>();
        
        // Search for chunks related to the query
        int topK = budget.isNearLimit() ? 5 : 10; // Reduce if budget is tight
        List<Document> queryChunks = chunkStore.similaritySearch(
            SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build()
        );
        
        // Filter chunks to only include those from relevant files
        List<Document> filteredChunks = queryChunks.stream()
            .filter(chunk -> {
                String chunkFilename = (String) chunk.getMetadata().get("filename");
                return relevantFiles.contains(chunkFilename);
            })
            .collect(Collectors.toList());
        
        // Convert to content strings for budget management
        List<String> chunkContents = filteredChunks.stream()
            .map(Document::getText)
            .collect(Collectors.toList());
        
        // Apply budget management and pruning
        List<String> prunedContents = contextManager.pruneContent(chunkContents, budget, query);
        
        // Convert back to Documents
        for (int i = 0; i < Math.min(filteredChunks.size(), prunedContents.size()); i++) {
            if (prunedContents.contains(filteredChunks.get(i).getText())) {
                allChunks.add(filteredChunks.get(i));
            }
        }
        
        // If budget allows, get specific chunks for each relevant file (more selective)
        if (!budget.isNearLimit()) {
            int maxFilesToProcess = budget.isOverLimit() ? 3 : Math.min(5, relevantFiles.size());
            int processedFiles = 0;
            
            for (String filename : relevantFiles) {
                if (processedFiles >= maxFilesToProcess || budget.isOverLimit()) break;
                
                List<Document> fileChunks = chunkStore.similaritySearch(
                    SearchRequest.builder()
                        .query(filename + " " + query)
                        .topK(2) // Reduced from 3
                        .build()
                );
                
                // Add chunks that aren't already included and fit in budget
                for (Document chunk : fileChunks) {
                    String chunkFilename = (String) chunk.getMetadata().get("filename");
                    if (filename.equals(chunkFilename) && !allChunks.contains(chunk)) {
                        if (contextManager.canAddContent(chunk.getText(), budget)) {
                            allChunks.add(chunk);
                            contextManager.addContent(chunk.getText(), budget);
                        } else {
                            logger.debug("🚫 Skipping chunk from {} - would exceed budget", filename);
                            break; // Stop adding more chunks
                        }
                    }
                }
                processedFiles++;
            }
        }
        
        logger.info("📊 Chunk retrieval: {} chunks selected within budget", allChunks.size());
        
        return allChunks.stream()
            .distinct()
            .collect(Collectors.toList());
    }

    public CodeContext retrieveSpecificFile(String filename) {
        logger.info("📄 Retrieving specific file: {}", filename);
        
        CodeContext context = new CodeContext();
        
        try {
            // Get file summary
            List<Document> summaries = summaryStore.similaritySearch(
                SearchRequest.builder()
                    .query(filename)
                    .topK(5)
                    .build()
            );
            
            List<Document> fileSummary = summaries.stream()
                .filter(doc -> filename.equals(doc.getMetadata().get("filename")))
                .limit(1)
                .collect(Collectors.toList());
            
            // Get all chunks for this file
            List<Document> chunks = chunkStore.similaritySearch(
                SearchRequest.builder()
                    .query(filename)
                    .topK(20)
                    .build()
            );
            
            List<Document> fileChunks = chunks.stream()
                .filter(doc -> filename.equals(doc.getMetadata().get("filename")))
                .collect(Collectors.toList());
            
            context.setFileSummaries(fileSummary);
            context.setCodeChunks(fileChunks);
            context.setRelevantFiles(Set.of(filename));
            context.setQuery("file: " + filename);
            
            logger.info("✅ Retrieved {} chunks for file: {}", fileChunks.size(), filename);
            
        } catch (Exception e) {
            logger.error("❌ Failed to retrieve file: {}", filename, e);
        }
        
        return context;
    }

    public static class CodeContext {
        private List<Document> fileSummaries = new ArrayList<>();
        private List<Document> codeChunks = new ArrayList<>();
        private Set<String> relevantFiles = new HashSet<>();
        private String query;
        private int tokensUsed = 0;
        private String searchStrategy = "similarity_search";
        private double searchConfidence = 0.0;

        // Getters and setters
        public List<Document> getFileSummaries() { return fileSummaries; }
        public void setFileSummaries(List<Document> fileSummaries) { this.fileSummaries = fileSummaries; }
        
        public List<Document> getCodeChunks() { return codeChunks; }
        public void setCodeChunks(List<Document> codeChunks) { this.codeChunks = codeChunks; }
        
        public Set<String> getRelevantFiles() { return relevantFiles; }
        public void setRelevantFiles(Set<String> relevantFiles) { this.relevantFiles = relevantFiles; }
        
        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        
        public int getTokensUsed() { return tokensUsed; }
        public void setTokensUsed(int tokensUsed) { this.tokensUsed = tokensUsed; }
        
        public String getSearchStrategy() { return searchStrategy; }
        public void setSearchStrategy(String searchStrategy) { this.searchStrategy = searchStrategy; }
        
        public double getSearchConfidence() { return searchConfidence; }
        public void setSearchConfidence(double searchConfidence) { this.searchConfidence = searchConfidence; }
        
        public boolean isEmpty() {
            return fileSummaries.isEmpty() && codeChunks.isEmpty();
        }
        
        public String getFormattedContext() {
            StringBuilder context = new StringBuilder();
            
            if (!fileSummaries.isEmpty()) {
                context.append("📁 **File Summaries:**\n");
                for (Document summary : fileSummaries) {
                    String filename = (String) summary.getMetadata().get("filename");
                    context.append(String.format("- **%s**: %s\n", filename, summary.getText()));
                }
                context.append("\n");
            }
            
            if (!codeChunks.isEmpty()) {
                context.append("🧩 **Code Chunks:**\n");
                for (Document chunk : codeChunks) {
                    String filename = (String) chunk.getMetadata().get("filename");
                    String type = (String) chunk.getMetadata().get("chunk_type");
                    context.append(String.format("- **%s** (%s):\n```java\n%s\n```\n\n", 
                        filename, type, chunk.getText()));
                }
            }
            
            return context.toString();
        }
    }
}
