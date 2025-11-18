package com.vijay.manager;

import com.vijay.dto.PersonalityTraits;
import com.vijay.service.PersonalityEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PersonalityAdvisorTest {

    private PersonalityEngine personalityEngine;
    private PersonalityAdvisor advisor;

    @BeforeEach
    void setUp() {
        personalityEngine = Mockito.mock(PersonalityEngine.class, Mockito.RETURNS_DEEP_STUBS);
        advisor = new PersonalityAdvisor(personalityEngine);
    }

    @Test
    @DisplayName("adviseCall should apply personality to response and delegate to chain")
    void adviseCall_appliesPersonalityAndDelegates() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);
        PersonalityTraits traits = mock(PersonalityTraits.class);

        when(chain.nextCall(request)).thenReturn(response);
        when(response.chatResponse().getResult().getOutput().getText()).thenReturn("Original response text");
        when(personalityEngine.getTraits()).thenReturn(traits);
        when(traits.getArchetype()).thenReturn("MENTOR");
        when(personalityEngine.applyPersonality("Original response text")).thenReturn("Personalized response text");

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(chain, times(1)).nextCall(request);
        verify(personalityEngine, times(1)).applyPersonality("Original response text");
    }

    @Test
    @DisplayName("adviseCall should log personality details when empathy and helpfulness flags are enabled")
    void adviseCall_logsPersonalityFlags() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);
        PersonalityTraits traits = mock(PersonalityTraits.class);

        when(chain.nextCall(request)).thenReturn(response);
        when(response.chatResponse().getResult().getOutput().getText()).thenReturn("Original response text");
        when(personalityEngine.getTraits()).thenReturn(traits);
        when(traits.getArchetype()).thenReturn("MENTOR");
        when(personalityEngine.applyPersonality("Original response text")).thenReturn("Personalized response text");
        when(personalityEngine.isEmpathetic()).thenReturn(true);
        when(personalityEngine.isPatient()).thenReturn(true);
        when(personalityEngine.isHelpful()).thenReturn(true);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(chain, times(1)).nextCall(request);
        verify(personalityEngine, times(1)).applyPersonality("Original response text");
        verify(personalityEngine, times(1)).isEmpathetic();
        verify(personalityEngine, times(1)).isPatient();
        verify(personalityEngine, times(1)).isHelpful();
    }

    @Test
    @DisplayName("adviseCall should recover gracefully when personality application fails")
    void adviseCall_onErrorCallsChainAgain() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse initialResponse = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);
        ChatClientResponse fallbackResponse = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);
        PersonalityTraits traits = mock(PersonalityTraits.class);

        when(chain.nextCall(request)).thenReturn(initialResponse, fallbackResponse);
        when(initialResponse.chatResponse().getResult().getOutput().getText()).thenReturn("Original response text");
        when(personalityEngine.getTraits()).thenReturn(traits);
        when(traits.getArchetype()).thenReturn("MENTOR");
        when(personalityEngine.applyPersonality("Original response text"))
                .thenThrow(new RuntimeException("Personality failure"));

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(fallbackResponse);
        verify(chain, times(2)).nextCall(request);
        verify(personalityEngine, times(1)).applyPersonality("Original response text");
    }
}
