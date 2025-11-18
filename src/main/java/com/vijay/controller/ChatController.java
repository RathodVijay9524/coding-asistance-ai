package com.vijay.controller;

import com.vijay.dto.ChatRequest;
import com.vijay.dto.ChatResponse;
import com.vijay.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/{provider}")
    public ResponseEntity<ChatResponse> chat(@PathVariable String provider, 
                                           @RequestBody ChatRequest request) {
        logger.info("Chat request received for provider: {} with message: {}", provider, request.getMessage());

        try {
            ChatResponse chatResponse = chatService.processChat(provider, request);
            return ResponseEntity.ok(chatResponse);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid provider: {}", provider);
            ChatResponse errorResponse = new ChatResponse("Invalid provider: " + provider + 
                ". Supported providers: " + String.join(", ", chatService.getSupportedProviders()), provider);
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            logger.error("Error processing chat request for provider {}: {}", provider, e.getMessage());
            ChatResponse errorResponse = new ChatResponse("Error processing request: " + e.getMessage(), provider);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/providers")
    public ResponseEntity<String[]> getSupportedProviders() {
        return ResponseEntity.ok(chatService.getSupportedProviders());
    }

    @GetMapping("/health/{provider}")
    public ResponseEntity<String> healthCheck(@PathVariable String provider) {
        try {
            ChatRequest testRequest = new ChatRequest("Hello, this is a health check", false);
            ChatResponse response = chatService.processChat(provider, testRequest);
            return ResponseEntity.ok("Provider " + provider + " is working: " + response.getResponse().substring(0, Math.min(50, response.getResponse().length())) + "...");
        } catch (Exception e) {
            logger.error("Health check failed for provider {}: {}", provider, e.getMessage());
            return ResponseEntity.status(500).body("Provider " + provider + " failed: " + e.getMessage());
        }
    }
}
