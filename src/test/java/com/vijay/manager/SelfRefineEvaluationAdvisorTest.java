package com.vijay.manager;

import com.vijay.dto.JudgeAdvisor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SelfRefineEvaluationAdvisorTest {

    private ChatClient judgeClient;
    private SelfRefineEvaluationAdvisor advisor;

    @BeforeEach
    void setUp() {
        judgeClient = Mockito.mock(ChatClient.class, Mockito.RETURNS_DEEP_STUBS);
        advisor = new SelfRefineEvaluationAdvisor(judgeClient);
    }

    @Test
    @DisplayName("adviseCall should return original response when rating is high (>= 4)")
    void adviseCall_highRating_returnsOriginalResponse() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);

        when(chain.nextCall(request)).thenReturn(response);
        when(response.chatResponse().getResult().getOutput().getText()).thenReturn("Some answer");

        // Stub judge client via deep stubbing: final text returned by judge is "5"
        when(judgeClient.prompt(anyString())
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText()).thenReturn("5");

        try (MockedStatic<JudgeAdvisor> judgeMock = Mockito.mockStatic(JudgeAdvisor.class)) {
            judgeMock.when(() -> JudgeAdvisor.evaluate("Some answer")).thenReturn(5);

            ChatClientResponse result = advisor.adviseCall(request, chain);

            assertThat(result).isSameAs(response);
            verify(chain, times(1)).nextCall(request);
            verify(chain, never()).copy(any());
        }
    }
}
