package com.vijay.manager;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.stereotype.Component;

@Component
public class ResponseSummarizerAdvisor implements CallAdvisor, IAgentBrain {

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        
        // Get the original response
        ChatClientResponse response = chain.nextCall(request);
        String originalAnswer = response.chatResponse().getResult().getOutput().getText();
        
        // Check if response is too long (>800 chars) and needs summarization
        if (originalAnswer.length() > 800) {
            System.out.println("📝 Brain 2 (Summarizer): Response too long (" + originalAnswer.length() + " chars), adding summary...");
            
            try {
                // Create a simple summary by taking key points
                String summary = createSimpleSummary(originalAnswer);
                
                // Create enhanced response with both summary and original
                String enhancedResponse = String.format("""
                    📝 **Quick Summary:** %s
                    
                    📋 **Full Response:** %s
                    """, summary, originalAnswer);

                System.out.println("✅ Brain 2 (Summarizer): Summary created successfully");
                System.out.println("📝 Summary: " + summary);
                
                // For now, just log the summary and return the original response
                // This avoids complex response mutation issues
                // The summary is available in the logs for debugging
                        
            } catch (Exception e) {
                System.err.println("❌ Brain 2 (Summarizer): Failed to create summary - " + e.getMessage());
                // Return original response if summarization fails
                return response;
            }
        } else {
            System.out.println("⏭️ Brain 2 (Summarizer): Response length OK (" + originalAnswer.length() + " chars), no summarization needed");
        }
        
        return response;
    }

    private String createSimpleSummary(String text) {
        // Simple rule-based summarization
        String[] sentences = text.split("\\. ");
        StringBuilder summary = new StringBuilder();
        
        // Take first sentence and any sentence with key indicators
        for (int i = 0; i < Math.min(sentences.length, 3); i++) {
            String sentence = sentences[i].trim();
            if (i == 0 || containsKeyInfo(sentence)) {
                if (summary.length() > 0) summary.append(" ");
                summary.append(sentence);
                if (!sentence.endsWith(".")) summary.append(".");
            }
        }
        
        return summary.toString();
    }

    private boolean containsKeyInfo(String sentence) {
        String lower = sentence.toLowerCase();
        return lower.contains("temperature") || lower.contains("weather") || 
               lower.contains("event") || lower.contains("meeting") ||
               lower.contains("result") || lower.contains("found") ||
               lower.contains("error") || lower.contains("success");
    }

    @Override
    public String getName() {
        return "responseSummarizerAdvisor";
    }

    @Override
    public int getOrder() {
        return 500; // Execute before SelfRefineEvaluationAdvisor (1000)
    }
    
    // ===== IAgentBrain Implementation =====
    @Override
    public String getBrainName() {
        return "responseSummarizerAdvisor";  // ← Spring bean name (lowercase first letter)
    }
    
    @Override
    public String getBrainDescription() {
        return "Summarizes and condenses responses, extracts key information, formats output for clarity and readability";
    }
}
