package com.vijay.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ResponseSummarizerAdvisorTest {

    private ResponseSummarizerAdvisor advisor;

    @BeforeEach
    void setUp() {
        advisor = new ResponseSummarizerAdvisor();
    }

    @Test
    @DisplayName("adviseCall should summarize long responses and still return original response object")
    void adviseCall_longResponse_runsSummarizationAndReturnsResponse() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);

        String longAnswer = "This is a long sentence about results and errors that should trigger summarization. ";
        longAnswer = longAnswer.repeat(20); // Ensure > 800 characters overall

        when(chain.nextCall(request)).thenReturn(response);
        when(response.chatResponse().getResult().getOutput().getText()).thenReturn(longAnswer);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should bypass summarization for short responses")
    void adviseCall_shortResponse_skipsSummarization() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);

        String shortAnswer = "Short answer";

        when(chain.nextCall(request)).thenReturn(response);
        when(response.chatResponse().getResult().getOutput().getText()).thenReturn(shortAnswer);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(chain, times(1)).nextCall(request);
    }
}
