package com.vijay.manager;

import com.vijay.dto.UserMentalModel;
import com.vijay.service.MentalStateInferencer;
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

class TheoryOfMindAdvisorTest {

    private MentalStateInferencer mentalStateInferencer;
    private TheoryOfMindAdvisor advisor;

    @BeforeEach
    void setUp() {
        mentalStateInferencer = Mockito.mock(MentalStateInferencer.class);
        advisor = new TheoryOfMindAdvisor(mentalStateInferencer);
    }

    @Test
    @DisplayName("adviseCall should infer mental model for non-empty query and delegate with augmented request")
    void adviseCall_nonEmptyQuery_infersMentalModelAndDelegates() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        String text = "user_id:12345 I am very confused about Spring Boot configuration";

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn(text);

        UserMentalModel mentalModel = new UserMentalModel("12345");
        mentalModel.setKnowledgeLevel(3);
        mentalModel.setConfusionLevel(80);
        mentalModel.setFrustrationLevel(70);
        mentalModel.setLearningStyle("code-heavy");
        mentalModel.addExpertiseArea("backend");
        mentalModel.addKnowledgeGap("spring");

        when(mentalStateInferencer.getMentalModel("12345")).thenReturn(mentalModel);
        when(chain.nextCall(any(ChatClientRequest.class))).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(mentalStateInferencer).inferFromQuery("12345", text);
        verify(mentalStateInferencer).getMentalModel("12345");
        verify(chain, times(1)).nextCall(any(ChatClientRequest.class));
    }

    @Test
    @DisplayName("adviseCall should skip mental model inference for empty query and still delegate")
    void adviseCall_emptyQuery_skipsInference() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);

        when(request.prompt()).thenReturn(null);
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verifyNoInteractions(mentalStateInferencer);
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should recover gracefully when mental state inference fails")
    void adviseCall_onError_callsChainWithOriginalRequest() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        String text = "user_id:12345 I am very confused about Spring Boot configuration";

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn(text);

        doThrow(new RuntimeException("Inference failure"))
                .when(mentalStateInferencer).inferFromQuery("12345", text);
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(mentalStateInferencer).inferFromQuery("12345", text);
        verify(chain, times(1)).nextCall(request);
    }
}
