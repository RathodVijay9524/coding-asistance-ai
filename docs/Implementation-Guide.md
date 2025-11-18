# Multi-Brain Architecture Implementation Guide

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Spring Boot 3.5.7
- Spring AI 1.1.0-M4
- Maven 3.6+

### Environment Setup
```bash
# Required API Keys
export OPENAI_API_KEY="your-openai-key"
export ANTHROPIC_API_KEY="your-anthropic-key" 
export GOOGLE_API_KEY="your-google-key"
export HUGGINGFACE_API_KEY="your-hf-key"
export OPENWEATHERMAP_API_KEY="your-weather-key"

# Optional: Ollama (for local models)
curl -fsSL https://ollama.ai/install.sh | sh
ollama serve
ollama pull llama2
```

## 🏗️ Core Implementation

### 1. Brain 0: Knowledge Graph Advisor

```java
@Component
public class KnowledgeGraphAdvisor implements CallAdvisor {
    
    private final Map<String, Set<String>> knowledgeGraph;
    
    public KnowledgeGraphAdvisor() {
        this.knowledgeGraph = initializeKnowledgeGraph();
    }
    
    private Map<String, Set<String>> initializeKnowledgeGraph() {
        Map<String, Set<String>> graph = new HashMap<>();
        
        // Define concept relationships
        graph.put("weather", Set.of("temperature", "humidity", "wind", "forecast"));
        graph.put("ai", Set.of("model", "llm", "openai", "claude", "gemini"));
        graph.put("calendar", Set.of("event", "meeting", "schedule", "appointment"));
        
        return graph;
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        String userMessage = extractUserMessage(request);
        Set<String> detectedConcepts = extractConcepts(userMessage);
        
        if (!detectedConcepts.isEmpty()) {
            Set<String> relatedConcepts = findRelatedConcepts(detectedConcepts);
            System.out.println("🔍 Detected concepts: " + detectedConcepts);
            System.out.println("🔗 Related concepts: " + relatedConcepts);
        }
        
        return chain.nextCall(request);
    }
    
    @Override
    public int getOrder() { return 100; } // Execute first
}
```

### 2. Brain 1: Smart Tool Retriever

```java
@Service
public class ToolFinderService {
    
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;
    
    public List<String> findRelevantTools(String prompt) {
        // Convert prompt to embedding
        List<Double> queryEmbedding = embeddingModel.embed(prompt);
        
        // Search for similar tools
        List<Document> similarTools = vectorStore.similaritySearch(
            SearchRequest.query(prompt).withTopK(5)
        );
        
        return similarTools.stream()
            .map(doc -> doc.getMetadata().get("toolName").toString())
            .collect(Collectors.toList());
    }
}

@Service  
public class AIAgentToolService {
    
    @Tool("Get current weather for a location")
    public String getCurrentWeather(String location) {
        // OpenWeatherMap API integration
        return weatherApiClient.getCurrentWeather(location);
    }
    
    @Tool("Get current date and time")
    public String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    
    @Tool("Search the web for information")
    public String googleSearch(String query) {
        // Google Search API integration
        return searchApiClient.search(query);
    }
}
```

### 3. Brain 2: Response Summarizer

```java
@Component
public class ResponseSummarizerAdvisor implements CallAdvisor {
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        ChatClientResponse response = chain.nextCall(request);
        String originalAnswer = response.chatResponse().getResult().getOutput().getText();
        
        if (originalAnswer.length() > 800) {
            System.out.println("📝 Brain 2: Creating summary for long response...");
            String summary = createSimpleSummary(originalAnswer);
            System.out.println("📝 Summary: " + summary);
        }
        
        return response;
    }
    
    private String createSimpleSummary(String text) {
        String[] sentences = text.split("\\. ");
        StringBuilder summary = new StringBuilder();
        
        for (int i = 0; i < Math.min(sentences.length, 3); i++) {
            String sentence = sentences[i].trim();
            if (i == 0 || containsKeyInfo(sentence)) {
                summary.append(sentence).append(". ");
            }
        }
        
        return summary.toString().trim();
    }
    
    private boolean containsKeyInfo(String sentence) {
        String lower = sentence.toLowerCase();
        return lower.contains("temperature") || lower.contains("weather") || 
               lower.contains("event") || lower.contains("result");
    }
    
    @Override
    public int getOrder() { return 500; }
}
```

### 4. Brain 3: Self-Refine Evaluator

```java
@Component
public class SelfRefineEvaluationAdvisor implements CallAdvisor {
    
    private final ChatClient judgeClient;
    
    public SelfRefineEvaluationAdvisor(OpenAiChatModel judgeModel) {
        this.judgeClient = ChatClient.builder(judgeModel).build();
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        ChatClientResponse response = chain.nextCall(request);
        String answer = response.chatResponse().getResult().getOutput().getText();
        
        int rating = evaluateResponse(answer);
        System.out.println("🧾 Judge rating: " + rating);
        
        if (rating < 4) {
            System.out.println("🔁 Re-evaluating... (rating too low)");
            
            String improvePrompt = String.format("""
                The previous answer was rated %d/5.
                Please improve your response to make it clearer and more accurate.
                Original answer: %s
                """, rating, answer);
            
            ChatClientRequest improvedRequest = request.mutate()
                .prompt(request.prompt().augmentUserMessage(improvePrompt))
                .build();
            
            return chain.copy(this).nextCall(improvedRequest);
        }
        
        return response;
    }
    
    private int evaluateResponse(String answer) {
        // Mock evaluation - replace with actual LLM judge
        return JudgeAdvisor.evaluate(answer);
    }
    
    @Override
    public int getOrder() { return 1000; } // Execute last
}
```

## 🔧 Configuration Setup

### AIProviderConfig.java
```java
@Configuration
public class AIProviderConfig {
    
    @Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
    }
    
    @Bean(name = "openAiChatClient")
    ChatClient openAiChatClient(
            OpenAiChatModel openAiChatModel,
            ChatMemory chatMemory,
            KnowledgeGraphAdvisor knowledgeGraphAdvisor,
            ResponseSummarizerAdvisor summarizerAdvisor,
            SelfRefineEvaluationAdvisor refineAdvisor,
            AIAgentToolService aiAgentToolService) {
        
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(
                    MessageChatMemoryAdvisor.builder(chatMemory).build(),
                    knowledgeGraphAdvisor,     // Order: 100
                    summarizerAdvisor,         // Order: 500
                    refineAdvisor              // Order: 1000
                )
                .defaultTools(aiAgentToolService)
                .build();
    }
    
    // Similar configuration for other providers...
}
```

### ChatService.java
```java
@Service
public class ChatService {
    
    private final Map<String, ChatClient> chatClients;
    private final ToolFinderService toolFinderService;
    
    public ChatResponse processChat(String provider, ChatRequest request) {
        ChatClient client = chatClients.get(provider + "ChatClient");
        
        // Find relevant tools
        List<String> tools = toolFinderService.findRelevantTools(request.getMessage());
        logger.info("Tools activated for provider {}: {}", provider, tools);
        
        // Execute chat with multi-brain processing
        ChatClientResponse response = client
                .prompt(request.getMessage())
                .call();
        
        String responseText = response.chatResponse().getResult().getOutput().getText();
        
        return new ChatResponse(responseText, provider, tools.toArray(new String[0]));
    }
    
    public List<String> getSupportedProviders() {
        return List.of("openai", "anthropic", "google", "ollama", "huggingface");
    }
}
```

## 🌐 Web Interface Implementation

### ChatBotController.java
```java
@Controller
public class ChatBotController {
    
    private final ChatService chatService;
    
    @GetMapping("/")
    public String indexPage() {
        return "index";
    }
    
    @GetMapping("/chatbot")
    public String chatbotPage(Model model) {
        model.addAttribute("providers", chatService.getSupportedProviders());
        return "chatbot";
    }
    
    @PostMapping("/send")
    @ResponseBody
    public ResponseEntity<ChatResponse> sendMessage(
            @RequestParam String message,
            @RequestParam String provider,
            @RequestParam(defaultValue = "true") boolean useTools) {
        
        try {
            ChatRequest request = new ChatRequest(message, useTools);
            ChatResponse response = chatService.processChat(provider, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ChatResponse errorResponse = new ChatResponse(
                "Sorry, I encountered an error: " + e.getMessage(), 
                provider
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
```

### Enhanced ChatResponse DTO
```java
public class ChatResponse {
    
    private String response;
    private String provider;
    private String[] toolsUsed;
    private String timestamp;
    private String formattedResponse;
    
    public ChatResponse(String response, String provider) {
        this.response = response;
        this.provider = provider;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.formattedResponse = formatResponseForUI(response);
    }
    
    private String formatResponseForUI(String rawResponse) {
        return rawResponse
                .replace("\n", "<br/>")
                .replace("- ", "• ")
                .trim();
    }
    
    // Getters and setters...
}
```

## 📊 Testing & Validation

### Unit Tests
```java
@SpringBootTest
class MultiBrainArchitectureTest {
    
    @Autowired
    private ChatService chatService;
    
    @Test
    void testKnowledgeGraphDetection() {
        ChatRequest request = new ChatRequest("What's the weather like?", true);
        ChatResponse response = chatService.processChat("openai", request);
        
        // Verify knowledge graph detected weather concepts
        assertThat(response.getResponse()).isNotEmpty();
    }
    
    @Test
    void testToolActivation() {
        ChatRequest request = new ChatRequest("Current weather in London", true);
        ChatResponse response = chatService.processChat("openai", request);
        
        // Verify weather tool was activated
        assertThat(response.getToolsUsed()).contains("getCurrentWeather");
    }
    
    @Test
    void testSelfRefinement() {
        // Test that low-quality responses get refined
        ChatRequest request = new ChatRequest("Tell me about AI", true);
        ChatResponse response = chatService.processChat("openai", request);
        
        assertThat(response.getResponse()).isNotEmpty();
    }
}
```

### Integration Tests
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatBotControllerTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testChatEndpoint() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("message", "What's the weather in Mumbai?");
        params.add("provider", "openai");
        params.add("useTools", "true");
        
        ResponseEntity<ChatResponse> response = restTemplate.postForEntity(
            "/send", params, ChatResponse.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getResponse()).contains("weather");
    }
}
```

## 🚀 Deployment Guide

### Docker Configuration
```dockerfile
FROM openjdk:21-jdk-slim

COPY target/coding-assistance-0.0.1-SNAPSHOT.jar app.jar

ENV OPENAI_API_KEY=""
ENV ANTHROPIC_API_KEY=""
ENV GOOGLE_API_KEY=""

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Docker Compose
```yaml
version: '3.8'
services:
  multi-brain-ai:
    build: .
    ports:
      - "8080:8080"
    environment:
      - OPENAI_API_KEY=${OPENAI_API_KEY}
      - ANTHROPIC_API_KEY=${ANTHROPIC_API_KEY}
      - GOOGLE_API_KEY=${GOOGLE_API_KEY}
      - OPENWEATHERMAP_API_KEY=${OPENWEATHERMAP_API_KEY}
    depends_on:
      - ollama
      
  ollama:
    image: ollama/ollama
    ports:
      - "11434:11434"
    volumes:
      - ollama_data:/root/.ollama
      
volumes:
  ollama_data:
```

### Production Configuration
```properties
# Production application.properties
server.port=8080
logging.level.com.vijay=INFO

# AI Provider Configuration
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.anthropic.api-key=${ANTHROPIC_API_KEY}
spring.ai.google.genai.api-key=${GOOGLE_API_KEY}

# Performance Tuning
spring.ai.openai.chat.options.temperature=0.7
spring.ai.openai.chat.options.max-tokens=2000

# Monitoring
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

## 📈 Monitoring & Observability

### Metrics Collection
```java
@Component
public class MultiBrainMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter conceptDetectionCounter;
    private final Timer responseTimeTimer;
    
    public MultiBrainMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.conceptDetectionCounter = Counter.builder("concepts.detected")
            .description("Number of concepts detected by knowledge graph")
            .register(meterRegistry);
        this.responseTimeTimer = Timer.builder("response.time")
            .description("Response generation time")
            .register(meterRegistry);
    }
    
    public void recordConceptDetection(int count) {
        conceptDetectionCounter.increment(count);
    }
    
    public void recordResponseTime(Duration duration) {
        responseTimeTimer.record(duration);
    }
}
```

### Health Checks
```java
@Component
public class AIProviderHealthIndicator implements HealthIndicator {
    
    private final Map<String, ChatClient> chatClients;
    
    @Override
    public Health health() {
        Health.Builder builder = Health.up();
        
        for (String provider : chatClients.keySet()) {
            try {
                // Test provider connectivity
                testProviderHealth(provider);
                builder.withDetail(provider, "UP");
            } catch (Exception e) {
                builder.withDetail(provider, "DOWN: " + e.getMessage());
            }
        }
        
        return builder.build();
    }
}
```

This implementation guide provides everything needed to build and deploy the Multi-Brain Graph-RAG Architecture with comprehensive multi-model provider support.
