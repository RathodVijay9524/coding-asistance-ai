package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🧠 INCREMENTAL GRAPH CALCULATOR - Phase 8
 * 
 * Purpose: Only re-calculates changed edges, incremental graph updates,
 * efficient edge calculation.
 * 
 * Responsibilities:
 * - Calculate graph edges incrementally
 * - Detect changed nodes
 * - Update only affected edges
 * - Track graph state
 * - Provide graph statistics
 */
@Service
public class IncrementalGraphCalculator {
    
    private static final Logger logger = LoggerFactory.getLogger(IncrementalGraphCalculator.class);
    
    // Graph tracking
    private final Map<String, GraphNode> nodes = new ConcurrentHashMap<>();
    private final Map<String, Set<GraphEdge>> edges = new ConcurrentHashMap<>();
    private final Map<String, String> nodeHashes = new ConcurrentHashMap<>();
    
    /**
     * Calculate graph edges for changed nodes
     */
    public GraphCalculationResult calculateChangedEdges(List<GraphNode> changedNodes) {
        GraphCalculationResult result = new GraphCalculationResult();
        
        logger.info("🧠 Incremental Graph Calculator: Processing {} changed nodes", changedNodes.size());
        
        for (GraphNode node : changedNodes) {
            String nodeHash = calculateNodeHash(node);
            String cachedHash = nodeHashes.get(node.id);
            
            // Check if node changed
            if (cachedHash == null || !cachedHash.equals(nodeHash)) {
                try {
                    // Update node
                    nodes.put(node.id, node);
                    nodeHashes.put(node.id, nodeHash);
                    
                    // Calculate edges for this node
                    Set<GraphEdge> nodeEdges = calculateNodeEdges(node);
                    edges.put(node.id, nodeEdges);
                    
                    result.nodesProcessed++;
                    result.edgesCalculated += nodeEdges.size();
                } catch (Exception e) {
                    logger.error("🧠 Incremental Graph Calculator: Error processing node {}: {}", 
                        node.id, e.getMessage());
                    result.errors++;
                }
            } else {
                result.cachedNodes++;
            }
        }
        
        result.totalNodes = changedNodes.size();
        result.duration = System.currentTimeMillis();
        
        logger.info("🧠 Incremental Graph Calculator: Processed {} nodes, {} edges, {} cached in {}ms", 
            result.nodesProcessed, result.edgesCalculated, result.cachedNodes, result.duration);
        
        return result;
    }
    
    /**
     * Calculate edges for a node
     */
    private Set<GraphEdge> calculateNodeEdges(GraphNode node) {
        Set<GraphEdge> nodeEdges = new HashSet<>();
        
        // Find related nodes
        for (GraphNode otherNode : nodes.values()) {
            if (!node.id.equals(otherNode.id)) {
                double similarity = calculateSimilarity(node, otherNode);
                
                if (similarity > 0.5) { // Threshold for edge creation
                    GraphEdge edge = new GraphEdge(node.id, otherNode.id, similarity);
                    nodeEdges.add(edge);
                }
            }
        }
        
        return nodeEdges;
    }
    
    /**
     * Calculate similarity between nodes
     */
    private double calculateSimilarity(GraphNode node1, GraphNode node2) {
        if (node1.content == null || node2.content == null) {
            return 0.0;
        }
        
        String[] words1 = node1.content.toLowerCase().split("\\s+");
        String[] words2 = node2.content.toLowerCase().split("\\s+");
        
        Set<String> set1 = new HashSet<>(Arrays.asList(words1));
        Set<String> set2 = new HashSet<>(Arrays.asList(words2));
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    /**
     * Calculate node hash
     */
    private String calculateNodeHash(GraphNode node) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            String content = node.id + node.content + node.type;
            byte[] hashBytes = md.digest(content.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            logger.debug("Error calculating node hash: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * Get edges for node
     */
    public Set<GraphEdge> getNodeEdges(String nodeId) {
        Set<GraphEdge> nodeEdges = edges.get(nodeId);
        return nodeEdges != null ? new HashSet<>(nodeEdges) : new HashSet<>();
    }
    
    /**
     * Get all edges
     */
    public Set<GraphEdge> getAllEdges() {
        Set<GraphEdge> allEdges = new HashSet<>();
        for (Set<GraphEdge> nodeEdges : edges.values()) {
            allEdges.addAll(nodeEdges);
        }
        return allEdges;
    }
    
    /**
     * Get node
     */
    public GraphNode getNode(String nodeId) {
        return nodes.get(nodeId);
    }
    
    /**
     * Get all nodes
     */
    public Collection<GraphNode> getAllNodes() {
        return new ArrayList<>(nodes.values());
    }
    
    /**
     * Clear graph cache
     */
    public void clearCache() {
        nodes.clear();
        edges.clear();
        nodeHashes.clear();
        logger.info("🧠 Incremental Graph Calculator: Cleared graph cache");
    }
    
    /**
     * Get graph statistics
     */
    public GraphStatistics getStatistics() {
        return new GraphStatistics(
            nodes.size(),
            edges.values().stream().mapToInt(Set::size).sum()
        );
    }
    
    // ============ Inner Classes ============
    
    /**
     * Graph node
     */
    public static class GraphNode {
        public final String id;
        public final String content;
        public final String type;
        public final long timestamp;
        
        public GraphNode(String id, String content, String type) {
            this.id = id;
            this.content = content;
            this.type = type;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Graph edge
     */
    public static class GraphEdge {
        public final String sourceId;
        public final String targetId;
        public final double weight;
        
        public GraphEdge(String sourceId, String targetId, double weight) {
            this.sourceId = sourceId;
            this.targetId = targetId;
            this.weight = weight;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GraphEdge)) return false;
            GraphEdge edge = (GraphEdge) o;
            return sourceId.equals(edge.sourceId) && targetId.equals(edge.targetId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(sourceId, targetId);
        }
    }
    
    /**
     * Graph calculation result
     */
    public static class GraphCalculationResult {
        public int totalNodes = 0;
        public int nodesProcessed = 0;
        public int cachedNodes = 0;
        public int edgesCalculated = 0;
        public int errors = 0;
        public long duration = 0;
        
        public double getEfficiency() {
            return totalNodes > 0 ? (double) cachedNodes / totalNodes * 100 : 0;
        }
    }
    
    /**
     * Graph statistics
     */
    public static class GraphStatistics {
        public final int totalNodes;
        public final int totalEdges;
        
        public GraphStatistics(int totalNodes, int totalEdges) {
            this.totalNodes = totalNodes;
            this.totalEdges = totalEdges;
        }
    }
}
