package com.vijay.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EnhancedContextBuilderAdvisorTest {

    private OpenAiChatModel chatModel;
    private EnhancedContextBuilderAdvisor advisor;

    @BeforeEach
    void setUp() {
        chatModel = Mockito.mock(OpenAiChatModel.class);
        advisor = new EnhancedContextBuilderAdvisor(chatModel);
    }

    @Test
    @DisplayName("adviseCall should analyze response and still return original response")
    void adviseCall_analyzesButReturnsOriginal() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn("Explain how DynamicContextAdvisor works in the architecture");

        when(chain.nextCall(request)).thenReturn(response);
        when(response.chatResponse().getResult().getOutput().getText())
                .thenReturn("DynamicContextAdvisor selects specialist brains and tools based on AgentPlan.");

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(chain, times(1)).nextCall(request);
        // We don't assert on calls to ChatClient since enhanceResponse may decide no improvement/summarization is needed
    }
}
