package com.vijay.manager;

import com.vijay.service.EnhancedLearningSystem;
import com.vijay.service.UserPreferenceEvolution;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LearningGrowthAdvisorTest {

    private EnhancedLearningSystem enhancedLearningSystem;
    private UserPreferenceEvolution userPreferenceEvolution;
    private LearningGrowthAdvisor advisor;

    @BeforeEach
    void setUp() {
        enhancedLearningSystem = Mockito.mock(EnhancedLearningSystem.class);
        userPreferenceEvolution = Mockito.mock(UserPreferenceEvolution.class);
        advisor = new LearningGrowthAdvisor(enhancedLearningSystem, userPreferenceEvolution);
    }

    @Test
    @DisplayName("adviseCall should record learning metrics and evolve preferences for non-empty query")
    void adviseCall_nonEmptyQuery_recordsLearningAndEvolvesPreferences() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        String query = "user_id:12345 Please give a concise detailed explanation with code example and step by step guidance";

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn(query);
        when(chain.nextCall(request)).thenReturn(response);

        String responseText = "Step 1: Do this. Step 2: Do that. ```code``` This is a detailed explanation without errors.";
        when(response.chatResponse().getResult().getOutput().getText()).thenReturn(responseText);

        when(enhancedLearningSystem.getOverallStatistics()).thenReturn("Overall stats");
        when(userPreferenceEvolution.getEvolutionSummary("12345")).thenReturn("Evolution summary");
        when(userPreferenceEvolution.predictNextPreference("12345")).thenReturn("unknown");

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(enhancedLearningSystem).recordStrategyOutcome(anyString(), anyString(), anyBoolean(), anyDouble(), anyDouble(), anyDouble());
        verify(userPreferenceEvolution, atLeastOnce()).recordPreferenceFeedback(eq("12345"), anyString(), anyDouble());
        verify(enhancedLearningSystem, atLeastOnce()).getOverallStatistics();
        verify(userPreferenceEvolution).getEvolutionSummary("12345");
        verify(userPreferenceEvolution).predictNextPreference("12345");
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should skip learning when query is empty and still delegate")
    void adviseCall_emptyQuery_skipsLearning() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);

        when(request.prompt()).thenReturn(null);
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verifyNoInteractions(enhancedLearningSystem, userPreferenceEvolution);
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should recover gracefully when learning system fails")
    void adviseCall_onError_callsChainWithOriginalRequest() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse initialResponse = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);
        ChatClientResponse fallbackResponse = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        String query = "user_id:12345 Please give a concise explanation";

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn(query);

        when(chain.nextCall(request)).thenReturn(initialResponse, fallbackResponse);
        when(initialResponse.chatResponse().getResult().getOutput().getText()).thenReturn("Some response");

        doThrow(new RuntimeException("Learning failure"))
                .when(enhancedLearningSystem).recordStrategyOutcome(anyString(), anyString(), anyBoolean(), anyDouble(), anyDouble(), anyDouble());

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(fallbackResponse);
        verify(enhancedLearningSystem).recordStrategyOutcome(anyString(), anyString(), anyBoolean(), anyDouble(), anyDouble(), anyDouble());
        verify(chain, times(2)).nextCall(request);
    }
}
