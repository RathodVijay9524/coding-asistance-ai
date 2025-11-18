package com.vijay.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ConversationHistoryAdvisorTest {

    private ConversationHistoryAdvisor advisor;

    @BeforeEach
    void setUp() {
        advisor = new ConversationHistoryAdvisor();
    }

    @Test
    @DisplayName("adviseCall should log information and still delegate to chain")
    void adviseCall_logsAndDelegates() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        ChatClientResponse response = mock(ChatClientResponse.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn("Hello, show me my history");
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should handle missing prompt safely")
    void adviseCall_missingPrompt() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        ChatClientResponse response = mock(ChatClientResponse.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);

        when(request.prompt()).thenReturn(null);
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(chain, times(1)).nextCall(request);
    }
}
