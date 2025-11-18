package com.vijay.manager;

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

class ErrorPredictionAdvisorTest {

    private ErrorPredictionAdvisor advisor;

    @BeforeEach
    void setUp() {
        advisor = new ErrorPredictionAdvisor();
    }

    @Test
    @DisplayName("adviseCall should analyze query and response for non-empty query and delegate")
    void adviseCall_nonEmptyQuery_analyzesAndDelegates() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        String query = "Please check for SQL injection and performance issues";

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn(query);
        when(chain.nextCall(request)).thenReturn(response);
        when(response.chatResponse().getResult().getOutput().getText())
                .thenReturn("This code uses Thread.stop and might have an N+1 query and SQL injection risk.");

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should recover gracefully when chain throws and still delegate")
    void adviseCall_onError_callsChainTwice() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse fallbackResponse = mock(ChatClientResponse.class);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        String query = "Short ambiguous query";

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
