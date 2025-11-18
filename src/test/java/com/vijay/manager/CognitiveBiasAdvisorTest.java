package com.vijay.manager;

import com.vijay.dto.CognitiveBias;
import com.vijay.service.CognitiveBiasEngine;
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

class CognitiveBiasAdvisorTest {

    private CognitiveBiasEngine biasEngine;
    private CognitiveBiasAdvisor advisor;

    @BeforeEach
    void setUp() {
        biasEngine = Mockito.mock(CognitiveBiasEngine.class);
        advisor = new CognitiveBiasAdvisor(biasEngine);
    }

    @Test
    @DisplayName("adviseCall should record interaction, apply biases and delegate for non-empty query")
    void adviseCall_nonEmptyQuery_appliesBiasesAndDelegates() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        String text = "user_id:12345 Please explain recency bias";

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn(text);
        when(chain.nextCall(request)).thenReturn(response);
        when(response.chatResponse().getResult().getOutput().getText()).thenReturn("Base response");

        when(biasEngine.applyBiases("12345", text, "Base response")).thenReturn("Biased response");
        when(biasEngine.getActiveBias("12345")).thenReturn(CognitiveBias.RECENCY_BIAS);
        when(biasEngine.getBiasStrength("12345")).thenReturn(0.8f);
        when(biasEngine.getBiasSummary("12345")).thenReturn("Summary");

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(biasEngine).recordInteraction("12345", text);
        verify(biasEngine).applyBiases("12345", text, "Base response");
        verify(biasEngine).getActiveBias("12345");
        verify(biasEngine).getBiasStrength("12345");
        verify(biasEngine).getBiasSummary("12345");
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should skip bias processing for empty query and still delegate")
    void adviseCall_emptyQuery_skipsBiasProcessing() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);

        when(request.prompt()).thenReturn(null);
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verifyNoInteractions(biasEngine);
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should recover gracefully when bias engine fails")
    void adviseCall_onError_callsChainWithOriginalRequest() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        String text = "user_id:12345 Please explain recency bias";

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn(text);

        doThrow(new RuntimeException("Bias failure"))
                .when(biasEngine).recordInteraction("12345", text);
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(biasEngine).recordInteraction("12345", text);
        verify(chain, times(1)).nextCall(request);
    }
}
