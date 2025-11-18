package com.vijay.manager;

import com.vijay.service.QueryPlanner;
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

class IntelligentQueryPlannerAdvisorTest {

    private QueryPlanner queryPlanner;
    private IntelligentQueryPlannerAdvisor advisor;

    @BeforeEach
    void setUp() {
        queryPlanner = Mockito.mock(QueryPlanner.class);
        advisor = new IntelligentQueryPlannerAdvisor(queryPlanner);
    }

    @Test
    @DisplayName("adviseCall should create search plan for non-empty query and delegate")
    void adviseCall_nonEmptyQuery_createsPlanAndDelegates() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        String text = "Analyze ChatService architecture and dependencies";

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn(text);

        QueryPlanner.SearchPlan plan = new QueryPlanner.SearchPlan();
        plan.intent = "ARCHITECTURE";
        plan.confidence = 0.9;
        plan.complexity = "HIGH";
        plan.searchStrategy = "dependency_graph";
        when(queryPlanner.createSearchPlan(text)).thenReturn(plan);
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(queryPlanner).createSearchPlan(text);
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should skip planning for empty query and still delegate")
    void adviseCall_emptyQuery_skipsPlanning() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);

        when(request.prompt()).thenReturn(null);
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verifyNoInteractions(queryPlanner);
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should recover gracefully when query planner fails")
    void adviseCall_onError_callsChainWithOriginalRequest() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        String text = "Analyze ChatService architecture";

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn(text);

        when(queryPlanner.createSearchPlan(text)).thenThrow(new RuntimeException("Planner failure"));
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(queryPlanner).createSearchPlan(text);
        verify(chain, times(1)).nextCall(request);
    }
}
