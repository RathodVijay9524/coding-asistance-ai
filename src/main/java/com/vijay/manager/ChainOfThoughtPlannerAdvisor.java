package com.vijay.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

@Component
public class ChainOfThoughtPlannerAdvisor implements CallAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(ChainOfThoughtPlannerAdvisor.class);
    
    private final ChatClient thinkerClient;

    public ChainOfThoughtPlannerAdvisor(OpenAiChatModel chatModel) {
        this.thinkerClient = ChatClient.builder(chatModel).build();
    }

    @Override
    public String getName() {
        return "ChainOfThoughtPlannerAdvisor";
    }

    @Override
    public int getOrder() {
        return 0; // Run FIRST - this is the "thinking front door"
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("🧠 Brain 0 (Chain-of-Thought Planner): Starting deep analysis...");
        
        try {
            String userQuery = extractUserMessage(request);
            if (userQuery.isEmpty()) {
                logger.info("⚠️ Brain 0: Empty query, proceeding with default flow");
                return chain.nextCall(request);
            }

            // Step-by-step thinking process
            ThoughtProcess thoughts = analyzeWithChainOfThought(userQuery);
            
            logger.info("🤔 Brain 0: Internal Monologue:");
            logger.info("   💭 Query Analysis: {}", thoughts.queryAnalysis);
            logger.info("   🎯 Intent Reasoning: {}", thoughts.intentReasoning);
            logger.info("   📋 Final Intent: {} (Confidence: {:.2f})", thoughts.finalIntent, thoughts.confidence);
            logger.info("   ⚠️ Complexity: {} | Ambiguity: {}", thoughts.complexity, thoughts.ambiguity);
            
            if (thoughts.needsSpecialHandling) {
                logger.info("🔄 Brain 0: Complex query detected - activating enhanced processing");
            }

            return chain.nextCall(request);
            
        } catch (Exception e) {
            logger.error("❌ Brain 0: Error in chain-of-thought planning - {}", e.getMessage());
            return chain.nextCall(request);
        }
    }

    private ThoughtProcess analyzeWithChainOfThought(String query) {
        String thinkingPrompt = String.format("""
            You are an intelligent query analyzer. Think step-by-step about this user query.
            
            Query: "%s"
            
            Please analyze this query using chain-of-thought reasoning:
            
            Step 1 - Query Decomposition:
            What are the main components of this query? What is the user really asking for?
            
            Step 2 - Context Analysis:
            Is this query:
            - Simple and direct?
            - Complex with multiple parts?
            - Ambiguous or unclear?
            - A follow-up to previous conversation?
            
            Step 3 - Intent Classification:
            Based on the analysis, classify the PRIMARY intent:
            - CODE: Questions about software architecture, implementation, classes, methods, configuration
            - TOOLS: Requests for external actions (weather, calendar, search, calculations, current info)
            - GENERAL: Casual conversation, greetings, help requests
            - CORRECTION: User pointing out errors or requesting corrections
            - COMPLEX: Multi-part queries requiring multiple capabilities
            
            Step 4 - Confidence Assessment:
            How confident are you in this classification? (0.0 to 1.0)
            
            Step 5 - Special Handling Needs:
            Does this query need special handling due to:
            - Ambiguity
            - Multiple intents
            - Correction/feedback nature
            - Complexity
            
            Respond in this exact format:
            ANALYSIS: [your step 1-2 analysis]
            REASONING: [your step 3-4 reasoning]
            INTENT: [CODE/TOOLS/GENERAL/CORRECTION/COMPLEX]
            CONFIDENCE: [0.0-1.0]
            COMPLEXITY: [LOW/MEDIUM/HIGH]
            AMBIGUITY: [LOW/MEDIUM/HIGH]
            SPECIAL_HANDLING: [YES/NO]
            """, query);

        try {
            String response = thinkerClient.prompt(thinkingPrompt).call().chatResponse()
                .getResult().getOutput().getText();
            
            return parseThoughtProcess(response, query);
            
        } catch (Exception e) {
            logger.warn("🤔 Brain 0: Chain-of-thought analysis failed, using fallback - {}", e.getMessage());
            return createFallbackThoughts(query);
        }
    }

    private ThoughtProcess parseThoughtProcess(String response, String originalQuery) {
        ThoughtProcess thoughts = new ThoughtProcess();
        thoughts.originalQuery = originalQuery;
        
        try {
            String[] lines = response.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("ANALYSIS:")) {
                    thoughts.queryAnalysis = line.substring(9).trim();
                } else if (line.startsWith("REASONING:")) {
                    thoughts.intentReasoning = line.substring(10).trim();
                } else if (line.startsWith("INTENT:")) {
                    thoughts.finalIntent = line.substring(7).trim();
                } else if (line.startsWith("CONFIDENCE:")) {
                    thoughts.confidence = Double.parseDouble(line.substring(11).trim());
                } else if (line.startsWith("COMPLEXITY:")) {
                    thoughts.complexity = line.substring(11).trim();
                } else if (line.startsWith("AMBIGUITY:")) {
                    thoughts.ambiguity = line.substring(10).trim();
                } else if (line.startsWith("SPECIAL_HANDLING:")) {
                    thoughts.needsSpecialHandling = line.substring(17).trim().equalsIgnoreCase("YES");
                }
            }
            
            // Validate and set defaults
            if (thoughts.finalIntent == null || thoughts.finalIntent.isEmpty()) {
                thoughts.finalIntent = "GENERAL";
            }
            if (thoughts.confidence == 0.0) {
                thoughts.confidence = 0.5;
            }
            
        } catch (Exception e) {
            logger.warn("🤔 Brain 0: Failed to parse thought process - {}", e.getMessage());
            return createFallbackThoughts(originalQuery);
        }
        
        return thoughts;
    }

    private ThoughtProcess createFallbackThoughts(String query) {
        ThoughtProcess thoughts = new ThoughtProcess();
        thoughts.originalQuery = query;
        thoughts.queryAnalysis = "Simple keyword-based analysis";
        thoughts.intentReasoning = "Fallback classification using pattern matching";
        thoughts.finalIntent = classifyWithKeywords(query);
        thoughts.confidence = 0.6;
        thoughts.complexity = "MEDIUM";
        thoughts.ambiguity = "MEDIUM";
        thoughts.needsSpecialHandling = false;
        return thoughts;
    }

    private String classifyWithKeywords(String query) {
        String lowerQuery = query.toLowerCase();
        
        // Correction patterns
        if (lowerQuery.contains("wrong") || lowerQuery.contains("incorrect") || 
            lowerQuery.contains("you said") || lowerQuery.contains("but i") ||
            lowerQuery.contains("actually") || lowerQuery.contains("correct")) {
            return "CORRECTION";
        }
        
        // Code-related patterns
        if (lowerQuery.contains("chatservice") || lowerQuery.contains("aiproviderconfig") ||
            lowerQuery.contains("advisor") || lowerQuery.contains("how does") ||
            lowerQuery.contains("show me") || lowerQuery.contains("explain") ||
            lowerQuery.contains("architecture") || lowerQuery.contains("code") ||
            lowerQuery.contains("class") || lowerQuery.contains("method") ||
            lowerQuery.contains("service") || lowerQuery.contains("config")) {
            return "CODE";
        }
        
        // Tool-related patterns
        if (lowerQuery.contains("weather") || lowerQuery.contains("calendar") ||
            lowerQuery.contains("meeting") || lowerQuery.contains("schedule") ||
            lowerQuery.contains("search") || lowerQuery.contains("email") ||
            lowerQuery.contains("time") || lowerQuery.contains("date") ||
            lowerQuery.contains("forecast") || lowerQuery.contains("temperature") ||
            lowerQuery.contains("version") || lowerQuery.contains("latest")) {
            return "TOOLS";
        }
        
        // Multi-part complexity check
        if (query.split("\\s+").length > 10 || lowerQuery.contains(" and ") || 
            lowerQuery.contains(" also ") || lowerQuery.contains(" plus ")) {
            return "COMPLEX";
        }
        
        return "GENERAL";
    }

    private String extractUserMessage(ChatClientRequest request) {
        try {
            if (request.prompt() != null && request.prompt().getInstructions() != null) {
                StringBuilder messageText = new StringBuilder();
                for (var message : request.prompt().getInstructions()) {
                    if (message instanceof UserMessage) {
                        UserMessage userMsg = (UserMessage) message;
                        messageText.append(userMsg.getText()).append(" ");
                    }
                }
                return messageText.toString().trim();
            }
            return "";
        } catch (Exception e) {
            logger.debug("Failed to extract user message: {}", e.getMessage());
            return "";
        }
    }

    private static class ThoughtProcess {
        String originalQuery;
        String queryAnalysis;
        String intentReasoning;
        String finalIntent;
        double confidence;
        String complexity;
        String ambiguity;
        boolean needsSpecialHandling;
    }
}
