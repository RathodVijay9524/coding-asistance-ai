package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DependencyGraphBuilderTest {

    private DependencyGraphBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new DependencyGraphBuilder();
    }

    @Test
    @DisplayName("getDependencies and getReverseDependencies should reflect import relationships")
    void dependencies_and_reverseDependencies() throws Exception {
        Path dir = Files.createTempDirectory("dep-graph-test");
        Path fileA = dir.resolve("A.java");
        Path fileB = dir.resolve("B.java");

        String codeA = "package com.vijay.test;\n" +
                "import com.vijay.test.B;\n" +
                "public class A { void call() { new B(); } }";
        String codeB = "package com.vijay.test;\n" +
                "public class B { void call() {} }";

        Files.writeString(fileA, codeA);
        Files.writeString(fileB, codeB);

        java.lang.reflect.Field fileToRelatedField = DependencyGraphBuilder.class.getDeclaredField("fileToRelatedFiles");
        java.lang.reflect.Field methodToFilesField = DependencyGraphBuilder.class.getDeclaredField("methodToFiles");
        java.lang.reflect.Field javaParserField = DependencyGraphBuilder.class.getDeclaredField("javaParser");
        fileToRelatedField.setAccessible(true);
        methodToFilesField.setAccessible(true);
        javaParserField.setAccessible(true);

        Files.walk(dir)
                .filter(p -> p.toString().endsWith(".java"))
                .forEach(path -> {
                    try {
                        java.lang.reflect.Method collectMethodsFromFile = DependencyGraphBuilder.class
                                .getDeclaredMethod("collectMethodsFromFile", java.nio.file.Path.class);
                        collectMethodsFromFile.setAccessible(true);
                        collectMethodsFromFile.invoke(builder, path);

                        java.lang.reflect.Method analyzeFile = DependencyGraphBuilder.class
                                .getDeclaredMethod("analyzeFile", java.nio.file.Path.class);
                        analyzeFile.setAccessible(true);
                        analyzeFile.invoke(builder, path);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        Set<String> depsA = builder.getDependencies("A.java");
        Set<String> depsB = builder.getDependencies("B.java");

        assertThat(depsA).contains("B.java");
        assertThat(depsB).isEmpty();

        Set<String> revDepsB = builder.getReverseDependencies("B.java");
        assertThat(revDepsB).contains("A.java");
    }
}
