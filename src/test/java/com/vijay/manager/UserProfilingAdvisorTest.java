package com.vijay.manager;

import com.vijay.context.GlobalBrainContext;
import com.vijay.service.UserProfilingService;
import org.junit.jupiter.api.AfterEach;
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

class UserProfilingAdvisorTest {

    private UserProfilingService userProfilingService;
    private UserProfilingAdvisor advisor;

    @BeforeEach
    void setUp() {
        userProfilingService = Mockito.mock(UserProfilingService.class);
        advisor = new UserProfilingAdvisor(userProfilingService);
        GlobalBrainContext.clear();
    }

    @AfterEach
    void tearDown() {
        GlobalBrainContext.clear();
    }

    @Test
    @DisplayName("adviseCall should adapt request based on user profile and record interaction")
    void adviseCall_nonEmptyQuery_adaptsAndRecordsInteraction() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        String query = "Hi, my name is Vijay and I want backend API help";

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn(query);

        UserProfilingService.UserProfileSummary summary = new UserProfilingService.UserProfileSummary();
        summary.userId = "default_user";
        summary.expertiseLevel = 3;
        summary.preferredResponseFormat = "balanced";
        summary.specializations = List.of("backend");
        summary.interactionCount = 10;
        summary.averageQuality = 4.2;

        when(userProfilingService.getProfileSummary("default_user")).thenReturn(summary);
        when(chain.nextCall(any(ChatClientRequest.class))).thenReturn(response);
        when(response.chatResponse().getResult().getOutput().getText()).thenReturn("This backend API uses Spring Boot and database access.");

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        assertThat(GlobalBrainContext.get("userName")).isEqualTo("Vijay");
        verify(userProfilingService).getProfileSummary("default_user");
        verify(chain, times(1)).nextCall(any(ChatClientRequest.class));
    }

    @Test
    @DisplayName("adviseCall should recover gracefully when profiling service fails")
    void adviseCall_onError_callsChainWithOriginalRequest() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        String query = "Please help with Spring Boot configuration";

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn(query);

        when(userProfilingService.getProfileSummary("default_user"))
                .thenThrow(new RuntimeException("Profiling failure"));
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(userProfilingService).getProfileSummary("default_user");
        verify(chain, times(1)).nextCall(request);
    }
}
