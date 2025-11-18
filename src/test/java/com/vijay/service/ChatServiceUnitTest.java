package com.vijay.service;

import com.vijay.context.GlobalBrainContext;
import com.vijay.context.TraceContext;
import com.vijay.dto.AgentPlan;
import com.vijay.dto.ChatRequest;
import com.vijay.dto.ChatResponse;
import com.vijay.tools.ToolFinderService;
import com.vijay.util.AgentPlanHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.ApplicationContext;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ChatServiceUnitTest {

    private ApplicationContext applicationContext;
    private ChatClient chatClient;
    private ToolFinderService toolFinderService;
    private SupervisorBrain supervisorBrain;
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        applicationContext = mock(ApplicationContext.class);
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        toolFinderService = mock(ToolFinderService.class);
        supervisorBrain = mock(SupervisorBrain.class);

        when(applicationContext.getBean(org.mockito.ArgumentMatchers.anyString(), eq(ChatClient.class))).thenReturn(chatClient);
        when(toolFinderService.findToolsFor(org.mockito.ArgumentMatchers.anyString())).thenReturn(List.of("toolA", "toolB"));

        // Stub ChatClient to always return a non-null response string
        when(chatClient.prompt().call().content()).thenReturn("answer");

        TraceContext.initialize();
        chatService = new ChatService(applicationContext, List.of(), toolFinderService, supervisorBrain);
    }

    @AfterEach
    void tearDown() {
        GlobalBrainContext.clear();
        TraceContext.clear();
        AgentPlanHolder.clear();
    }

    @Test
    @DisplayName("processChat should call ChatClient, use ToolFinder, and return ChatResponse with used tools")
    void processChat_basicFlow() {
        AgentPlan plan = new AgentPlan();
        plan.setRequiredTools(List.of("toolA"));
        AgentPlanHolder.setPlan(plan);

        ChatRequest req = new ChatRequest();
        req.setMessage("Hello world");
        req.setConversationId("conv-1");

        ChatResponse resp = chatService.processChat("ollama", req);

        assertThat(resp.getProvider()).isEqualTo("ollama");
        assertThat(resp.getToolsUsed()).containsExactly("toolA");

        verify(toolFinderService, times(1)).findToolsFor("Hello world");
        verify(supervisorBrain, times(1)).initializeConversation(eq("default_user"), anyString());
    }

    @Test
    @DisplayName("getSupportedProviders should list known providers")
    void getSupportedProviders_basic() {
        String[] providers = chatService.getSupportedProviders();
        assertThat(providers).contains("openai", "claude", "anthropic", "google", "gemini", "ollama");
    }

    @Test
    @DisplayName("processChat should return empty tools when no plan is available")
    void processChat_noPlan_returnsEmptyTools() {
        ChatRequest req = new ChatRequest();
        req.setMessage("Hello without plan");
        req.setConversationId("conv-2");

        ChatResponse resp = chatService.processChat("ollama", req);

        assertThat(resp.getToolsUsed()).isEmpty();
    }

    @Test
    @DisplayName("processChat should generate default conversation ID when none is provided")
    void processChat_generatesDefaultConversationIdWhenMissing() {
        AgentPlan plan = new AgentPlan();
        plan.setRequiredTools(List.of("toolA"));
        AgentPlanHolder.setPlan(plan);

        ChatRequest req = new ChatRequest();
        req.setMessage("Hello without conversation id");

        ChatResponse resp = chatService.processChat("ollama", req);
        assertThat(resp).isNotNull();

        ArgumentCaptor<String> conversationIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(supervisorBrain).initializeConversation(eq("default_user"), conversationIdCaptor.capture());
        assertThat(conversationIdCaptor.getValue()).startsWith("session_default_");
    }

    @Test
    @DisplayName("processChat should use OpenAI chat client when provider is openai")
    void processChat_usesOpenAiChatClient() {
        AgentPlan plan = new AgentPlan();
        plan.setRequiredTools(List.of("toolA"));
        AgentPlanHolder.setPlan(plan);

        ChatRequest req = new ChatRequest();
        req.setMessage("Hello openai");
        req.setConversationId("conv-openai");

        ChatResponse resp = chatService.processChat("openai", req);

        assertThat(resp.getProvider()).isEqualTo("openai");

        verify(applicationContext, times(1)).getBean("openAiChatClient", ChatClient.class);
    }

    @Test
    @DisplayName("processChat should default to Ollama chat client for unknown provider")
    void processChat_unknownProviderFallsBackToOllama() {
        AgentPlan plan = new AgentPlan();
        plan.setRequiredTools(List.of("toolA"));
        AgentPlanHolder.setPlan(plan);

        ChatRequest req = new ChatRequest();
        req.setMessage("Hello unknown");
        req.setConversationId("conv-unknown");

        ChatResponse resp = chatService.processChat("some-unknown-provider", req);

        assertThat(resp.getProvider()).isEqualTo("some-unknown-provider");

        verify(applicationContext, times(1)).getBean("ollamaChatClient", ChatClient.class);
    }

    @Test
    @DisplayName("processChat should propagate IllegalArgumentException from ApplicationContext and still clear contexts")
    void processChat_applicationContextThrows_cleansUpContext() {
        AgentPlan plan = new AgentPlan();
        plan.setRequiredTools(List.of("toolA"));
        AgentPlanHolder.setPlan(plan);

        ChatRequest req = new ChatRequest();
        req.setMessage("Hello error");
        req.setConversationId("conv-error");

        // Force getChatClientForProvider("openai") to throw
        when(applicationContext.getBean("openAiChatClient", ChatClient.class))
                .thenThrow(new IllegalArgumentException("no bean"));

        assertThrows(IllegalArgumentException.class,
                () -> chatService.processChat("openai", req));

        // Contexts and plan holder should be cleared in finally block
        assertThat(GlobalBrainContext.getReasoningState()).isNull();
        assertThat(AgentPlanHolder.getPlan()).isNull();
    }
}
