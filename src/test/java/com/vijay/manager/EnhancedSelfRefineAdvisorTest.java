package com.vijay.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EnhancedSelfRefineAdvisorTest {

    private ChatClient judgeClient;
    private EnhancedSelfRefineAdvisor advisor;

    @BeforeEach
    void setUp() {
        judgeClient = Mockito.mock(ChatClient.class, Mockito.RETURNS_DEEP_STUBS);
        advisor = new EnhancedSelfRefineAdvisor(judgeClient);
    }

    @Test
    @DisplayName("adviseCall should return original response when quality rating is acceptable (>= 3.0)")
    void adviseCall_highRating_returnsOriginalResponse() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);

        when(chain.nextCall(request)).thenReturn(response);
        when(response.chatResponse().getResult().getOutput().getText()).thenReturn("Some good answer");

        // First evaluation call returns a high rating, so no refinement is needed
        String evaluation = "RATING: 4.0\nVERDICT: GOOD\nISSUES: none\nSTRENGTHS: clear";
        when(judgeClient.prompt(anyString()).call().content()).thenReturn(evaluation);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(chain, times(1)).nextCall(request);
        // Only one evaluation call should be enough when rating is acceptable
        verify(judgeClient, atLeastOnce()).prompt(anyString());
    }

    @Test
    @DisplayName("adviseCall should attempt refinement when rating is low (< 3.0) and still return original response")
    void adviseCall_lowRating_attemptsRefinementAndReturnsOriginal() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);

        when(chain.nextCall(request)).thenReturn(response);
        when(response.chatResponse().getResult().getOutput().getText()).thenReturn("Weak answer");

        // Sequence of judgeClient.prompt(...).call().content() calls:
        // 1) initial evaluation (low rating)
        // 2) refinement generation (returns improved text)
        // 3) re-evaluation (higher rating)
        String initialEval = "RATING: 2.0\nVERDICT: POOR\nISSUES: missing details\nSTRENGTHS: none";
        String refinedResponse = "Improved, more detailed and structured answer.";
        String refinedEval = "RATING: 4.0\nVERDICT: GOOD\nISSUES: minor\nSTRENGTHS: detailed";

        when(judgeClient.prompt(anyString()).call().content())
                .thenReturn(initialEval, refinedResponse, refinedEval);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(chain, times(1)).nextCall(request);
        // We expect multiple judge calls: evaluation + refinement + re-evaluation
        verify(judgeClient, atLeast(2)).prompt(anyString());
    }
}
