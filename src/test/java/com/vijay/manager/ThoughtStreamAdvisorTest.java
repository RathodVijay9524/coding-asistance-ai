package com.vijay.manager;

import com.vijay.dto.ThoughtStreamCursor;
import com.vijay.service.ThoughtStreamProcessor;
import com.vijay.service.WorkingMemoryManager;
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

class ThoughtStreamAdvisorTest {

    private ThoughtStreamProcessor thoughtStreamProcessor;
    private WorkingMemoryManager workingMemoryManager;
    private ThoughtStreamAdvisor advisor;

    @BeforeEach
    void setUp() {
        thoughtStreamProcessor = Mockito.mock(ThoughtStreamProcessor.class);
        workingMemoryManager = Mockito.mock(WorkingMemoryManager.class);
        advisor = new ThoughtStreamAdvisor(thoughtStreamProcessor, workingMemoryManager);
    }

    @Test
    @DisplayName("adviseCall should process non-empty query, update working memory and delegate")
    void adviseCall_nonEmptyQuery_updatesWorkingMemoryAndDelegates() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        String text = "user_id:12345 Please help with this bug";

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn(text);

        ThoughtStreamCursor cursor = new ThoughtStreamCursor("query_1");
        cursor.setFocusArea("CODE");
        cursor.setConfidence(0.85);

        when(thoughtStreamProcessor.processQuery(anyString(), eq(text))).thenReturn(cursor);
        when(chain.nextCall(any(ChatClientRequest.class))).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);

        // User message should be recorded with extracted user id
        verify(workingMemoryManager).recordUserMessage("12345", text);
        // Thought stream should be processed
        verify(thoughtStreamProcessor).processQuery(anyString(), eq(text));
        // Chain should be called once with (possibly augmented) request
        verify(chain, times(1)).nextCall(any(ChatClientRequest.class));
        // Response outcome should be recorded based on cursor
        verify(workingMemoryManager).recordBrainOutput(eq("12345"), eq("ThoughtStream"), contains("Focus: CODE"));
        verify(workingMemoryManager).recordIntent("12345", "CODE", 0.85);
    }

    @Test
    @DisplayName("adviseCall should bypass processing for empty query and still delegate")
    void adviseCall_emptyQuery_skipsProcessing() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);

        when(request.prompt()).thenReturn(null);
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        // No working memory or thought stream processing for empty queries
        verifyNoInteractions(thoughtStreamProcessor, workingMemoryManager);
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should recover gracefully when thought stream processing fails")
    void adviseCall_onError_callsChainWithOriginalRequest() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        String text = "user_id:12345 Please help with this bug";

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn(text);

        when(thoughtStreamProcessor.processQuery(anyString(), eq(text)))
                .thenThrow(new RuntimeException("Thought stream failure"));
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        // User message is still recorded before failure
        verify(workingMemoryManager).recordUserMessage("12345", text);
        // Chain should be called once with original request from the catch block
        verify(chain, times(1)).nextCall(request);
    }
}
