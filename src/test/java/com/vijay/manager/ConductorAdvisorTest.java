package com.vijay.manager;

import com.vijay.context.GlobalBrainContext;
import com.vijay.dto.AgentPlan;
import com.vijay.dto.ReasoningState;
import com.vijay.util.AgentPlanHolder;
import org.junit.jupiter.api.AfterEach;
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

class ConductorAdvisorTest {

    private ConductorAdvisor advisor;

    @BeforeEach
    void setUp() {
        advisor = new ConductorAdvisor();
    }

    @AfterEach
    void tearDown() {
        AgentPlanHolder.clear();
        GlobalBrainContext.setReasoningState(null);
    }

    @Test
    @DisplayName("adviseCall should create a master plan and store it in AgentPlanHolder")
    void adviseCall_createsPlan() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn("Please refactor this code and improve performance");
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        AgentPlan plan = AgentPlanHolder.getPlan();
        assertThat(plan).isNotNull();
        assertThat(plan.getIntent()).isIn("REFACTOR", "PERFORMANCE", "GENERAL");
        assertThat(plan.getStrategy()).isNotBlank();
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should use fast path for simple arithmetic/time queries")
    void adviseCall_fastPathSimpleQuery() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn("1 + 2");
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        AgentPlan plan = AgentPlanHolder.getPlan();
        assertThat(plan).isNotNull();
        assertThat(plan.getIntent()).isEqualTo("SIMPLE");
        assertThat(plan.getSelectedBrains()).contains("conductorAdvisor", "toolCallAdvisor", "personalityAdvisor");
    }

    @Test
    @DisplayName("adviseCall should use default plan when user query is empty")
    void adviseCall_emptyQuery_usesDefaultPlan() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn("");
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        AgentPlan plan = AgentPlanHolder.getPlan();
        assertThat(plan).isNotNull();
        assertThat(plan.getUserQuery()).isEqualTo("(default plan)");
    }

    @Test
    @DisplayName("adviseCall should approve suggested tools for analysis-style queries")
    void adviseCall_analysisQuery_usesSuggestedToolsFromReasoningState() {
        ReasoningState state = new ReasoningState();
        state.setSuggestedTools(List.of("analyzeProjectComprehensive", "detectBugs"));
        GlobalBrainContext.setReasoningState(state);

        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn("please analyze this project path = C:/repo");
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        AgentPlan plan = AgentPlanHolder.getPlan();
        assertThat(plan).isNotNull();
        assertThat(plan.getRequiredTools())
                .containsExactlyInAnyOrder("analyzeProjectComprehensive", "detectBugs");
    }

    @Test
    @DisplayName("adviseCall should fall back to default plan when an error occurs")
    void adviseCall_exception_usesFallbackPlan() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);

        when(request.prompt()).thenThrow(new RuntimeException("boom"));
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        AgentPlan plan = AgentPlanHolder.getPlan();
        assertThat(plan).isNotNull();
        assertThat(plan.getUserQuery()).isEqualTo("(default plan)");
        assertThat(plan.getIntent()).isEqualTo("GENERAL");
    }
}
