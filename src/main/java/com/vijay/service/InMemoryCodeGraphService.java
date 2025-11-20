package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Very small, in-memory implementation of CodeGraphService.
 *
 * This now builds a simple graph by scanning the local Java source tree
 * (src/main/java) for class files and naive references to other
 * *Service/*Advisor/*Controller types.
 */
@Service
public class InMemoryCodeGraphService implements CodeGraphService {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryCodeGraphService.class);

    private final Map<String, Set<String>> adjacency = new HashMap<>();

    public InMemoryCodeGraphService() {
        buildGraphFromSource();
    }

    private void addEdge(String from, String to) {
        adjacency.computeIfAbsent(from, k -> new LinkedHashSet<>()).add(to);
    }

    private void buildGraphFromSource() {
        String userDir = System.getProperty("user.dir", ".");
        Path root = Paths.get(userDir, "src", "main", "java");

        if (!Files.exists(root)) {
            logger.warn("CodeGraph: Source root not found at {} - adjacency will be empty", root);
            return;
        }

        logger.info("CodeGraph: Building adjacency from source under {}", root.toAbsolutePath());

        Pattern referencePattern = Pattern.compile("[A-Z][A-Za-z0-9_]*(Service|Advisor|Controller)");

        try {
            Files.walk(root)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(path -> processSourceFile(path, root, referencePattern));
        } catch (IOException e) {
            logger.warn("CodeGraph: Failed to walk source tree: {}", e.getMessage());
        }

        logger.info("CodeGraph: Built adjacency for {} nodes", adjacency.size());
    }

    private void processSourceFile(Path path, Path root, Pattern referencePattern) {
        String fileName = path.getFileName().toString();
        if (!fileName.endsWith(".java")) {
            return;
        }

        String className = fileName.substring(0, fileName.length() - 5);

        String content;
        try {
            content = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.debug("CodeGraph: Failed to read {}: {}", path, e.getMessage());
            return;
        }

        Matcher matcher = referencePattern.matcher(content);
        while (matcher.find()) {
            String referenced = matcher.group();
            if (!referenced.equals(className)) {
                addEdge(className, referenced);
            }
        }
    }

    @Override
    public List<String> getRelatedNodes(String nodeName) {
        return new ArrayList<>(adjacency.getOrDefault(nodeName, Collections.emptySet()));
    }

    @Override
    public List<String> getImpactRadius(String nodeName, int maxDepth, int maxNodes) {
        List<String> result = new ArrayList<>();
        if (nodeName == null || nodeName.isEmpty()) {
            return result;
        }

        Set<String> visited = new LinkedHashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        Deque<Integer> depthQueue = new ArrayDeque<>();

        queue.add(nodeName);
        depthQueue.add(0);
        visited.add(nodeName);

        while (!queue.isEmpty() && result.size() < maxNodes) {
            String current = queue.poll();
            int depth = depthQueue.poll();

            result.add(current);

            if (depth >= maxDepth) {
                continue;
            }

            for (String neighbor : adjacency.getOrDefault(current, Collections.emptySet())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                    depthQueue.add(depth + 1);
                }
            }
        }

        logger.debug("CodeGraph impact radius for {}: {}", nodeName, result);
        return result;
    }
}
