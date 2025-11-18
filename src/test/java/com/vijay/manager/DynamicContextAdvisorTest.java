package com.vijay.manager;

import com.vijay.dto.AgentPlan;
import com.vijay.service.BrainFinderService;
import com.vijay.tools.ToolFinderService;
import com.vijay.util.AgentPlanHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DynamicContextAdvisorTest {

    private BrainFinderService brainFinderService;
    private ToolFinderService toolFinderService;
    private DynamicContextAdvisor advisor;

    @BeforeEach
    void setUp() {
        brainFinderService = Mockito.mock(BrainFinderService.class);
        toolFinderService = Mockito.mock(ToolFinderService.class);
        advisor = new DynamicContextAdvisor(brainFinderService, toolFinderService);
    }

    @AfterEach
    void tearDown() {
        AgentPlanHolder.clear();
    }

    @Test
    @DisplayName("adviseCall should use selectedBrains from AgentPlan and continue chain")
    void adviseCall_withPlan_usesSelectedBrains() {
        AgentPlan plan = new AgentPlan()
                .setIntent("DEBUG")
                .setComplexity(4)
                .setRequiredTools(List.of("toolA"))
                .setSelectedBrains(List.of("specialBrain1", "specialBrain2"));
        AgentPlanHolder.setPlan(plan);

        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        Message msg = mock(UserMessage.class);

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(msg));
        when(((UserMessage) msg).getText()).thenReturn("Please debug this issue");
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        // When selectedBrains is non-empty, BrainFinderService should NOT be called in fallback
        verifyNoInteractions(brainFinderService, toolFinderService);
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should use fallback mode when no plan is present")
    void adviseCall_noPlan_fallbackUsesBrainAndToolFinder() {
        AgentPlanHolder.clear();

        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn("Analyze this project architecture");
        when(chain.nextCall(request)).thenReturn(response);

        when(brainFinderService.findBrainsFor(anyString())).thenReturn(List.of("brainA"));
        when(toolFinderService.findToolsFor(anyString())).thenReturn(List.of("toolX"));

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(brainFinderService, times(1)).findBrainsFor(contains("Analyze"));
        verify(toolFinderService, times(1)).findToolsFor(contains("Analyze"));
        verify(chain, times(1)).nextCall(request);
    }
}
