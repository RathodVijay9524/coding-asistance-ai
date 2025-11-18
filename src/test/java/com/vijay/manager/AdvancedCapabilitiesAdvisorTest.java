package com.vijay.manager;

import com.vijay.dto.ResponseScenario;
import com.vijay.service.MentalSimulator;
import com.vijay.service.PersonalityEvolutionEngine;
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

class AdvancedCapabilitiesAdvisorTest {

    private MentalSimulator mentalSimulator;
    private PersonalityEvolutionEngine personalityEvolutionEngine;
    private AdvancedCapabilitiesAdvisor advisor;

    @BeforeEach
    void setUp() {
        mentalSimulator = Mockito.mock(MentalSimulator.class);
        personalityEvolutionEngine = Mockito.mock(PersonalityEvolutionEngine.class);
        advisor = new AdvancedCapabilitiesAdvisor(mentalSimulator, personalityEvolutionEngine);
    }

    @Test
    @DisplayName("adviseCall should simulate scenarios for non-empty query and delegate")
    void adviseCall_nonEmptyQuery_simulatesScenariosAndDelegates() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        String text = "user_id:12345 Please explain mental simulation";

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn(text);

        when(chain.nextCall(request)).thenReturn(response);
        when(response.chatResponse().getResult().getOutput().getText()).thenReturn("Base advanced response");

        ResponseScenario scenario1 = new ResponseScenario("Resp1");
        scenario1.setTone("concise");
        ResponseScenario scenario2 = new ResponseScenario("Resp2");
        scenario2.setTone("detailed");

        List<ResponseScenario> scenarios = List.of(scenario1, scenario2);
        when(mentalSimulator.simulateScenarios(text, "Base advanced response")).thenReturn(scenarios);
        when(mentalSimulator.evaluateAndSelectBest(scenarios)).thenReturn(scenario2);
        when(mentalSimulator.predictUserReaction(scenario2)).thenReturn("User will be very satisfied");
        when(personalityEvolutionEngine.getEvolutionSummary("12345")).thenReturn("Evolution summary");

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(mentalSimulator).simulateScenarios(text, "Base advanced response");
        verify(mentalSimulator).evaluateAndSelectBest(scenarios);
        verify(mentalSimulator).predictUserReaction(scenario2);
        verify(personalityEvolutionEngine).getEvolutionSummary("12345");
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should skip advanced capabilities for empty query and still delegate")
    void adviseCall_emptyQuery_skipsAdvancedCapabilities() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);

        when(request.prompt()).thenReturn(null);
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verifyNoInteractions(mentalSimulator, personalityEvolutionEngine);
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should recover gracefully when advanced capabilities fail")
    void adviseCall_onError_callsChainWithOriginalRequest() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse initialResponse = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);
        ChatClientResponse fallbackResponse = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        String text = "user_id:12345 Please explain mental simulation";

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn(text);

        when(chain.nextCall(request)).thenReturn(initialResponse, fallbackResponse);
        when(initialResponse.chatResponse().getResult().getOutput().getText()).thenReturn("Base advanced response");

        when(mentalSimulator.simulateScenarios(text, "Base advanced response"))
                .thenThrow(new RuntimeException("Simulation failure"));

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(fallbackResponse);
        verify(mentalSimulator).simulateScenarios(text, "Base advanced response");
        verify(chain, times(2)).nextCall(request);
    }
}
