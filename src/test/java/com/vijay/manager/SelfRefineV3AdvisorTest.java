package com.vijay.manager;

import com.vijay.dto.AgentPlan;
import com.vijay.service.ConsistencyCheckService;
import com.vijay.service.HallucinationDetector;
import com.vijay.service.OutputMerger;
import com.vijay.service.SupervisorBrain;
import com.vijay.service.TokenCountingService;
import com.vijay.util.AgentPlanHolder;
import org.junit.jupiter.api.AfterEach;
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

class SelfRefineV3AdvisorTest {

    private OpenAiChatModel chatModel;
    private SupervisorBrain supervisorBrain;
    private TokenCountingService tokenCountingService;
    private ConsistencyCheckService consistencyCheckService;
    private HallucinationDetector hallucinationDetector;
    private OutputMerger outputMerger;
    private SelfRefineV3Advisor advisor;

    @BeforeEach
    void setUp() {
        chatModel = Mockito.mock(OpenAiChatModel.class);
        supervisorBrain = Mockito.mock(SupervisorBrain.class);
        tokenCountingService = Mockito.mock(TokenCountingService.class);
        consistencyCheckService = Mockito.mock(ConsistencyCheckService.class);
        hallucinationDetector = Mockito.mock(HallucinationDetector.class);
        outputMerger = Mockito.mock(OutputMerger.class);

        advisor = new SelfRefineV3Advisor(
                chatModel,
                supervisorBrain,
                tokenCountingService,
                consistencyCheckService,
                hallucinationDetector,
                outputMerger
        );
    }

    @AfterEach
    void tearDown() {
        AgentPlanHolder.clear();
    }

    @Test
    @DisplayName("adviseCall should bypass heavy evaluation for simple queries (complexity <= 3)")
    void adviseCall_simpleQuery_skipsEvaluation() {
        AgentPlan plan = new AgentPlan()
                .setIntent("GENERAL")
                .setComplexity(2)
                .setStrategy("FAST_RECALL");
        AgentPlanHolder.setPlan(plan);

        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn("Hello, how are you?");
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        // For simple queries, evaluation is skipped and no heavy services should be invoked
        verifyNoInteractions(tokenCountingService, consistencyCheckService, hallucinationDetector, outputMerger);
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should perform enhanced evaluation for complex queries and invoke services")
    void adviseCall_complexQuery_performsEvaluation() {
        AgentPlan plan = new AgentPlan()
                .setIntent("GENERAL")
                .setComplexity(5)
                .setStrategy("BALANCED");
        AgentPlanHolder.setPlan(plan);

        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn("What is today's date?");

        String content = "Today's date is November 17, 2025. This example response clearly " +
                "provides the exact date you asked for, and you can rely on it because it " +
                "demonstrates how the assistant answers date questions.\n" +
                "- Additional context line 1\n" +
                "- Additional context line 2\n" +
                "public class Example { public void date() {} }";

        when(chain.nextCall(request)).thenReturn(response);
        when(response.chatResponse().getResult().getOutput().getText()).thenReturn(content);

        ConsistencyCheckService.ConsistencyReport consistencyReport = mock(ConsistencyCheckService.ConsistencyReport.class);
        when(consistencyReport.isConsistent()).thenReturn(true);
        when(consistencyReport.getIssues()).thenReturn(List.of());
        when(consistencyCheckService.checkConsistency(anyString())).thenReturn(consistencyReport);
        when(consistencyCheckService.validateCodeStructure(anyString()))
                .thenReturn(new ConsistencyCheckService.CodeStructureReport());

        HallucinationDetector.HallucinationReport hallucinationReport =
                new HallucinationDetector.HallucinationReport();
        when(hallucinationDetector.detectHallucinations(anyString())).thenReturn(hallucinationReport);

        when(tokenCountingService.countTokens(anyString())).thenReturn(50);
        TokenCountingService.TokenUsageRecord tokenRecord =
                new TokenCountingService.TokenUsageRecord("user", 10, 40, 50, 10.0);
        when(tokenCountingService.recordTokenUsage(anyString(), anyString(), anyString()))
                .thenReturn(tokenRecord);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(consistencyCheckService).checkConsistency(content);
        verify(hallucinationDetector).detectHallucinations(content);
        verify(tokenCountingService).countTokens(content);
        verify(tokenCountingService).recordTokenUsage(anyString(), anyString(), eq(content));
        // Only one downstream call when no exception occurs
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should recover when chain throws by returning fallback response")
    void adviseCall_chainThrows_returnsFallback() {
        // No plan or complex plan -> we go through normal path, but chain fails on first call
        AgentPlanHolder.clear();

        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn("Some complex query that triggers evaluation");

        when(chain.nextCall(request))
                .thenThrow(new RuntimeException("primary call failed"))
                .thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        // First call fails inside try, second call happens in catch block
        verify(chain, times(2)).nextCall(request);
    }
}
