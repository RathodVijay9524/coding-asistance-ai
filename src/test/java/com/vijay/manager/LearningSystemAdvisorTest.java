package com.vijay.manager;

import com.vijay.service.LearningMetricsService;
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

class LearningSystemAdvisorTest {

    private LearningMetricsService learningMetricsService;
    private LearningSystemAdvisor advisor;

    @BeforeEach
    void setUp() {
        learningMetricsService = Mockito.mock(LearningMetricsService.class);
        advisor = new LearningSystemAdvisor(learningMetricsService);
    }

    @Test
    @DisplayName("adviseCall should record interaction metrics and analyze patterns")
    void adviseCall_recordsMetricsAndPatterns() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn("Explain the architecture and show some code examples.");
        when(chain.nextCall(request)).thenReturn(response);
        when(response.chatResponse().getResult().getOutput().getText())
                .thenReturn("Here is an explanation with code and structure...\n1. ...");

        LearningMetricsService.LearningInsights insights = new LearningMetricsService.LearningInsights();
        insights.totalQueries = 1;
        insights.overallSuccessRate = 0.9;
        insights.averageQuality = 4.5;
        when(learningMetricsService.getInsights()).thenReturn(insights);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(chain, times(1)).nextCall(request);
        verify(learningMetricsService, atLeastOnce()).recordQuerySuccess(anyString(), anyString(), anyDouble(), anyLong());
        verify(learningMetricsService, atLeastOnce()).getInsights();
    }
}
