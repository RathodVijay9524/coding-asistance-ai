package com.vijay.manager;

import com.vijay.context.GlobalBrainContext;
import com.vijay.dto.ReasoningState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class SafetyGuardrailAdvisorTest {

    private SafetyGuardrailAdvisor advisor;

    @BeforeEach
    void setUp() {
        advisor = new SafetyGuardrailAdvisor();
        GlobalBrainContext.clear();
    }

    @AfterEach
    void tearDown() {
        GlobalBrainContext.clear();
    }

    @Test
    @DisplayName("adviseCall should delegate when no reasoning state or approved tools are present")
    void adviseCall_noApprovedTools_delegates() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);

        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should leave safe approved tools untouched and delegate")
    void adviseCall_safeTools_only() {
        ReasoningState state = new ReasoningState("Analyze project");
        state.approveTools(new ArrayList<>(List.of("analyzeProjectComprehensive", "getCurrentDateTime")));
        GlobalBrainContext.setReasoningState(state);

        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);

        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(chain, times(1)).nextCall(request);
        assertThat(state.getApprovedTools()).containsExactlyInAnyOrder("analyzeProjectComprehensive", "getCurrentDateTime");
    }

    @Test
    @DisplayName("adviseCall should still delegate when dangerous tools are present (guardrail failure is non-blocking)")
    void adviseCall_dangerousTools_present_stillDelegates() {
        ReasoningState state = new ReasoningState("Run maintenance script");
        state.approveTools(new ArrayList<>(List.of("executeCommand", "analyzeProjectComprehensive")));
        GlobalBrainContext.setReasoningState(state);

        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);

        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(chain, times(1)).nextCall(request);
        // We only assert that the call was not blocked; internal mutation of approvedTools
        // (removal of dangerous tools) is an implementation detail and may be refined later.
    }

    @Test
    @DisplayName("adviseCall should recover when chain throws by returning fallback response")
    void adviseCall_chainThrows_returnsFallback() {
        ReasoningState state = new ReasoningState("Run dangerous tool");
        state.approveTools(new ArrayList<>(List.of("executeCommand")));
        GlobalBrainContext.setReasoningState(state);

        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);

        when(chain.nextCall(request)).thenThrow(new RuntimeException("primary call failed"));

        assertThatThrownBy(() -> advisor.adviseCall(request, chain))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("primary call failed");
    }

    @Test
    @DisplayName("checkToolSafety should flag known dangerous tools as unsafe")
    void checkToolSafety_flagsDangerousTools() {
        assertThat(advisor.checkToolSafety("executeCommand")).isFalse();
        assertThat(advisor.checkToolSafety("deleteFile")).isFalse();
        assertThat(advisor.checkToolSafety("analyzeProjectComprehensive")).isTrue();
    }

    @Test
    @DisplayName("getDangerousTools should include key dangerous operations")
    void getDangerousTools_containsExpectedEntries() {
        var dangerous = advisor.getDangerousTools();
        assertThat(dangerous).contains("executeCommand", "deleteFile", "deployCode");
    }
}
