package com.vijay.codebase;

import org.springframework.stereotype.Component;

@Component
public class DependencyMapper {
    
    public DependencyGraph mapDependencies(CodebaseIndex fileIndex) {
        DependencyGraph graph = new DependencyGraph();
        graph.setEdgeCount(0);
        return graph;
    }
}
