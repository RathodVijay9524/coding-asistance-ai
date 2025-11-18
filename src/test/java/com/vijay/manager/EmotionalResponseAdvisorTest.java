package com.vijay.manager;

import com.vijay.service.EmotionalMemoryStore;
import com.vijay.service.EmotionalResponseAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class EmotionalResponseAdvisorTest {

    private EmotionalResponseAdapter responseAdapter;
    private EmotionalMemoryStore memoryStore;
    private EmotionalResponseAdvisor advisor;

    @BeforeEach
    void setUp() {
        responseAdapter = Mockito.mock(EmotionalResponseAdapter.class);
        memoryStore = Mockito.mock(EmotionalMemoryStore.class);
        advisor = new EmotionalResponseAdvisor(responseAdapter, memoryStore);
    }

    @Test
    @DisplayName("adviseCall should delegate to chain and handle missing emotional context")
    void adviseCall_neutralOrMissingContext() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        ChatClientResponse response = mock(ChatClientResponse.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);

        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        // Current implementation always returns null from getEmotionalContextFromRequest,
        // so no interactions with adapter or memory store are expected.
        verifyNoInteractions(responseAdapter, memoryStore);
        verify(chain, times(1)).nextCall(request);
    }
}
