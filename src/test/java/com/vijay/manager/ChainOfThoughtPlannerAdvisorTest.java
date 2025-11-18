package com.vijay.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ChainOfThoughtPlannerAdvisorTest {

    private OpenAiChatModel chatModel;
    private ChainOfThoughtPlannerAdvisor advisor;

    @BeforeEach
    void setUp() {
        chatModel = Mockito.mock(OpenAiChatModel.class);
        advisor = new ChainOfThoughtPlannerAdvisor(chatModel);
    }

    @Test
    @DisplayName("adviseCall should run chain-of-thought analysis for non-empty query and delegate")
    void adviseCall_nonEmptyQuery_runsCotAndDelegates() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        String query = "Explain how ChatService and AIProviderConfig work together";

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn(query);
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should skip chain-of-thought planning for empty query and still delegate")
    void adviseCall_emptyQuery_skipsCot() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);

        when(request.prompt()).thenReturn(null);
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should recover gracefully when chain fails")
    void adviseCall_onError_callsChainTwice() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse fallbackResponse = mock(ChatClientResponse.class);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        String query = "Complex multi-part query about architecture and tools";

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn(query);

        when(chain.nextCall(request)).thenThrow(new RuntimeException("Chain failure"))
                                     .thenReturn(fallbackResponse);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(fallbackResponse);
        verify(chain, times(2)).nextCall(request);
    }
}
