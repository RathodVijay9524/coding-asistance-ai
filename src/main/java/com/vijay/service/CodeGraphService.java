package com.vijay.service;

import java.util.List;

/**
 * Simple stub interface for a code structure graph (GraphRAG entry point).
 *
 * Nodes are typically classes, interfaces, or modules. Edges represent
 * relationships like CALLS, EXTENDS, IMPLEMENTS, DEPENDS_ON, etc.
 */
public interface CodeGraphService {

    /**
     * Return a list of node identifiers that are directly related to the
     * given node (e.g. callers, callees, dependencies).
     */
    List<String> getRelatedNodes(String nodeName);

    /**
     * Return a small "impact radius" around a given node – the node itself
     * plus a limited number of related nodes for context.
     */
    List<String> getImpactRadius(String nodeName, int maxDepth, int maxNodes);
}
