package com.vijay.manager;

import com.vijay.dto.EmotionalContext;
import com.vijay.dto.EmotionalState;
import com.vijay.service.EmotionalAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class EmotionalContextAdvisorTest {

    private EmotionalAnalyzer emotionalAnalyzer;
    private EmotionalContextAdvisor advisor;

    @BeforeEach
    void setUp() {
        emotionalAnalyzer = Mockito.mock(EmotionalAnalyzer.class);
        advisor = new EmotionalContextAdvisor(emotionalAnalyzer);
    }

    @Test
    @DisplayName("adviseCall should analyze emotion when user message is present and continue chain")
    void adviseCall_analyzesEmotionAndContinues() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        ChatClientResponse response = mock(ChatClientResponse.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn("I am very frustrated with these errors");

        EmotionalContext context = new EmotionalContext();
        context.setCurrentState(EmotionalState.FRUSTRATED);
        when(emotionalAnalyzer.analyzeEmotion(anyString())).thenReturn(context);
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(emotionalAnalyzer, times(1)).analyzeEmotion(contains("frustrated"));
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should skip analysis on empty message and still continue chain")
    void adviseCall_emptyMessage() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);

        when(request.prompt()).thenReturn(null);
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(emotionalAnalyzer, never()).analyzeEmotion(anyString());
    }
}
