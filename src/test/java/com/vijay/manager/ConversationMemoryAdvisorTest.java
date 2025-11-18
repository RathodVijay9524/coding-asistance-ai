package com.vijay.manager;

import com.vijay.service.ConversationMemoryManager;
import com.vijay.service.ConversationMemoryManager.ConversationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ConversationMemoryAdvisorTest {

    private ConversationMemoryManager memoryManager;
    private ConversationMemoryAdvisor advisor;

    @BeforeEach
    void setUp() {
        memoryManager = Mockito.mock(ConversationMemoryManager.class);
        advisor = new ConversationMemoryAdvisor(memoryManager);
    }

    @Test
    @DisplayName("adviseCall should retrieve context for non-empty query and delegate to chain")
    void adviseCall_nonEmptyQuery_retrievesContextAndDelegates() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        String userQuery = "Explain our previous conversation about ChatService";

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn(userQuery);

        ConversationContext context = mock(ConversationContext.class);
        when(context.hasRelevantContext()).thenReturn(true);
        when(context.getRecentExchanges()).thenReturn(Collections.emptyList());
        when(context.getRelatedExchanges()).thenReturn(Collections.emptyList());
        when(context.getPreferredSearchStrategies()).thenReturn(Collections.emptyList());
        when(context.getTypicalQueryPatterns()).thenReturn(Collections.emptyList());
        when(context.getLongTermMemories()).thenReturn(Collections.emptyList());

        when(memoryManager.getRelevantContext(anyString(), anyString(), eq(userQuery))).thenReturn(context);

        String responseText = "Here is a detailed explanation of the previous architecture decision. ```code```";
        when(response.chatResponse().getResult().getOutput().getText()).thenReturn(responseText);
        when(chain.nextCall(any(ChatClientRequest.class))).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);

        verify(memoryManager, times(1)).getRelevantContext(anyString(), eq("default_user"), eq(userQuery));
        verify(chain, times(1)).nextCall(any(ChatClientRequest.class));
    }

    @Test
    @DisplayName("adviseCall should skip memory when query is empty and still delegate to chain")
    void adviseCall_emptyQuery_skipsMemoryAndDelegates() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);

        when(request.prompt()).thenReturn(null);
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verifyNoInteractions(memoryManager);
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should recover gracefully when memory manager fails")
    void adviseCall_onError_callsChainWithOriginalRequest() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        String userQuery = "Explain our previous conversation about ChatService";

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn(userQuery);

        when(memoryManager.getRelevantContext(anyString(), anyString(), eq(userQuery)))
                .thenThrow(new RuntimeException("Memory failure"));
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(memoryManager, times(1)).getRelevantContext(anyString(), eq("default_user"), eq(userQuery));
        verify(chain, times(1)).nextCall(request);
    }
}
