package com.vijay.manager;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

@Component
public class KnowledgeGraphAdvisor implements CallAdvisor, IAgentBrain {

    // Static knowledge graph - relationships between concepts
    private final Map<String, Set<String>> knowledgeGraph;
    private final Map<String, String> conceptDefinitions;

    public KnowledgeGraphAdvisor() {
        this.knowledgeGraph = initializeKnowledgeGraph();
        this.conceptDefinitions = initializeConceptDefinitions();
    }

    private Map<String, Set<String>> initializeKnowledgeGraph() {
        Map<String, Set<String>> graph = new HashMap<>();
        
        // Weather-related connections
        graph.put("weather", Set.of("temperature", "humidity", "wind", "forecast", "climate"));
        graph.put("temperature", Set.of("celsius", "fahrenheit", "feels_like", "heat_index"));
        graph.put("forecast", Set.of("5day", "weekly", "prediction", "weather"));
        
        // Calendar-related connections
        graph.put("calendar", Set.of("event", "meeting", "schedule", "appointment", "time"));
        graph.put("meeting", Set.of("participants", "duration", "zoom", "conference"));
        graph.put("schedule", Set.of("time_slot", "availability", "booking", "calendar"));
        
        // Search-related connections
        graph.put("search", Set.of("google", "query", "results", "information", "web"));
        graph.put("information", Set.of("data", "facts", "knowledge", "search"));
        
        // Math-related connections
        graph.put("math", Set.of("calculation", "add", "multiply", "numbers", "arithmetic"));
        graph.put("calculation", Set.of("result", "compute", "math", "formula"));
        
        // AI-related connections
        graph.put("ai", Set.of("model", "llm", "openai", "claude", "gemini", "ollama"));
        graph.put("model", Set.of("provider", "response", "generation", "ai"));
        
        return graph;
    }

    private Map<String, String> initializeConceptDefinitions() {
        Map<String, String> definitions = new HashMap<>();
        
        definitions.put("weather", "Current atmospheric conditions including temperature, humidity, and precipitation");
        definitions.put("calendar", "Time management system for scheduling events and appointments");
        definitions.put("search", "Information retrieval process using web search engines");
        definitions.put("math", "Mathematical operations and calculations");
        definitions.put("ai", "Artificial Intelligence models and language processing");
        
        return definitions;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        
        String userMessage = extractUserMessage(request);
        System.out.println("🕸️ Knowledge Graph: Analyzing query for concept relationships...");
        
        // Extract concepts from user message
        Set<String> detectedConcepts = extractConcepts(userMessage);
        
        if (!detectedConcepts.isEmpty()) {
            System.out.println("🔍 Detected concepts: " + detectedConcepts);
            
            // Find related concepts using graph edges
            Set<String> relatedConcepts = findRelatedConcepts(detectedConcepts);
            System.out.println("🔗 Related concepts: " + relatedConcepts);
            
            // Log the knowledge graph enhancement (for now just log, don't modify prompt)
            System.out.println("✅ Knowledge Graph: Context available - " + 
                String.join(", ", detectedConcepts) + " → " + String.join(", ", relatedConcepts));
        } else {
            System.out.println("⏭️ Knowledge Graph: No relevant concepts detected");
        }
        
        // For now, just pass through without modifying the request to avoid issues
        return chain.nextCall(request);
    }

    private String extractUserMessage(ChatClientRequest request) {
        try {
            // Try to get the user message from the prompt
            if (request.prompt() != null && request.prompt().getInstructions() != null) {
                // getInstructions() returns List<Message>, so we need to extract text from messages
                StringBuilder messageText = new StringBuilder();
                for (var message : request.prompt().getInstructions()) {
                    // Try different ways to get message content
                    if (message instanceof UserMessage) {
                        UserMessage userMsg = (UserMessage) message;
                        messageText.append(userMsg.getText()).append(" ");
                    } else if (message.toString() != null) {
                        // Fallback to toString if specific type doesn't work
                        messageText.append(message.toString()).append(" ");
                    }
                }
                return messageText.toString().trim();
            }
            return ""; // Return empty string if no message found
        } catch (Exception e) {
            System.err.println("❌ Knowledge Graph: Error extracting user message - " + e.getMessage());
            return "";
        }
    }

    private Set<String> extractConcepts(String text) {
        Set<String> concepts = new HashSet<>();
        String lowerText = text.toLowerCase();
        
        // Check for each concept in the knowledge graph
        for (String concept : knowledgeGraph.keySet()) {
            if (lowerText.contains(concept)) {
                concepts.add(concept);
            }
        }
        
        // Also check concept definitions
        for (String concept : conceptDefinitions.keySet()) {
            if (lowerText.contains(concept)) {
                concepts.add(concept);
            }
        }
        
        return concepts;
    }

    private Set<String> findRelatedConcepts(Set<String> detectedConcepts) {
        Set<String> related = new HashSet<>();
        
        for (String concept : detectedConcepts) {
            Set<String> connections = knowledgeGraph.get(concept);
            if (connections != null) {
                related.addAll(connections);
            }
        }
        
        // Remove already detected concepts
        related.removeAll(detectedConcepts);
        return related;
    }

    private String enhancePromptWithContext(String originalPrompt, Set<String> detectedConcepts, Set<String> relatedConcepts) {
        StringBuilder context = new StringBuilder();
        context.append("\n\n**Context from Knowledge Graph:**\n");
        
        // Add definitions for detected concepts
        for (String concept : detectedConcepts) {
            String definition = conceptDefinitions.get(concept);
            if (definition != null) {
                context.append("- ").append(concept).append(": ").append(definition).append("\n");
            }
        }
        
        // Add related concepts that might be relevant
        if (!relatedConcepts.isEmpty()) {
            context.append("\n**Related concepts to consider:** ");
            context.append(String.join(", ", relatedConcepts));
            context.append("\n");
        }
        
        return originalPrompt + context.toString();
    }

    @Override
    public String getName() {
        return "knowledgeGraphAdvisor";
    }

    @Override
    public int getOrder() {
        return 100; // Execute early to enhance the prompt
    }
    
    // ===== IAgentBrain Implementation =====
    @Override
    public String getBrainName() {
        return "knowledgeGraphAdvisor";  // ← Spring bean name (lowercase first letter)
    }
    
    @Override
    public String getBrainDescription() {
        return "Builds and queries knowledge graph, connects concepts and relationships, enables semantic reasoning and context enrichment";
    }
}
