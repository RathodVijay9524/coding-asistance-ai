package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class IncrementalGraphCalculatorTest {

    private IncrementalGraphCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new IncrementalGraphCalculator();
    }

    @Test
    @DisplayName("calculateChangedEdges should create edges for similar nodes and cache unchanged ones")
    void calculateChangedEdges_basic() {
        IncrementalGraphCalculator.GraphNode n1 = new IncrementalGraphCalculator.GraphNode("A", "public class A {}", "code");
        IncrementalGraphCalculator.GraphNode n2 = new IncrementalGraphCalculator.GraphNode("B", "public class B extends A {}", "code");
        IncrementalGraphCalculator.GraphNode n3 = new IncrementalGraphCalculator.GraphNode("C", "unrelated text", "doc");

        IncrementalGraphCalculator.GraphCalculationResult result1 = calculator.calculateChangedEdges(List.of(n1, n2, n3));

        assertThat(result1.totalNodes).isEqualTo(3);
        assertThat(result1.nodesProcessed).isEqualTo(3);
        assertThat(result1.edgesCalculated).isGreaterThanOrEqualTo(1);

        Set<IncrementalGraphCalculator.GraphEdge> edgesB = calculator.getNodeEdges("B");
        Set<IncrementalGraphCalculator.GraphEdge> allEdges = calculator.getAllEdges();

        assertThat(edgesB).isNotEmpty();
        assertThat(allEdges).isNotEmpty();

        IncrementalGraphCalculator.GraphCalculationResult result2 = calculator.calculateChangedEdges(List.of(n1, n2, n3));
        assertThat(result2.cachedNodes).isEqualTo(3);

        IncrementalGraphCalculator.GraphStatistics stats = calculator.getStatistics();
        assertThat(stats.totalNodes).isEqualTo(3);
        assertThat(stats.totalEdges).isGreaterThanOrEqualTo(allEdges.size());
    }
}
