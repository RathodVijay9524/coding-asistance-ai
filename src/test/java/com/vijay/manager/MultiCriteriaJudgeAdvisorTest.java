package com.vijay.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.openai.OpenAiChatModel;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MultiCriteriaJudgeAdvisorTest {

    private OpenAiChatModel chatModel;
    private MultiCriteriaJudgeAdvisor advisor;

    @BeforeEach
    void setUp() {
        chatModel = Mockito.mock(OpenAiChatModel.class);
        advisor = new MultiCriteriaJudgeAdvisor(chatModel);
    }

    @Test
    @DisplayName("adviseCall should return null when chain returns null response")
    void adviseCall_nullResponse() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);

        when(chain.nextCall(request)).thenReturn(null);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isNull();
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should skip evaluation when response content is empty")
    void adviseCall_emptyContent_skipsEvaluation() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);

        when(chain.nextCall(request)).thenReturn(response);
        when(response.chatResponse().getResult().getOutput().getText()).thenReturn("   ");

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should perform multi-criteria evaluation for non-empty content and still return original response")
    void adviseCall_nonEmptyContent_evaluatesAndReturnsResponse() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);

        when(chain.nextCall(request)).thenReturn(response);
        when(response.chatResponse().getResult().getOutput().getText()).thenReturn("Some detailed answer content");

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should recover when chain throws by returning fallback response")
    void adviseCall_chainThrows_returnsFallback() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);

        when(chain.nextCall(request))
                .thenThrow(new RuntimeException("primary call failed"))
                .thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        // First call fails inside try, second call happens in catch block
        verify(chain, times(2)).nextCall(request);
    }

    // ===== Helper reflection methods for testing private helpers =====

    private double invokeParseScore(String input) throws Exception {
        Method m = MultiCriteriaJudgeAdvisor.class.getDeclaredMethod("parseScore", String.class);
        m.setAccessible(true);
        Object result = m.invoke(advisor, input);
        return (Double) result;
    }

    private Object invokeParseQualityAssessment(String evaluation) throws Exception {
        Method m = MultiCriteriaJudgeAdvisor.class.getDeclaredMethod("parseQualityAssessment", String.class);
        m.setAccessible(true);
        return m.invoke(advisor, evaluation);
    }

    private void invokeLogQualityIssues(Object assessment) throws Exception {
        Method m = MultiCriteriaJudgeAdvisor.class.getDeclaredMethod("logQualityIssues",
                Class.forName("com.vijay.manager.MultiCriteriaJudgeAdvisor$QualityAssessment"));
        m.setAccessible(true);
        m.invoke(advisor, assessment);
    }

    private double getAssessmentField(Object assessment, String fieldName) throws Exception {
        var field = assessment.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getDouble(assessment);
    }

    private String getAssessmentStringField(Object assessment, String fieldName) throws Exception {
        var field = assessment.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (String) field.get(assessment);
    }

    @Test
    @DisplayName("parseScore should handle normal, clamped, and invalid values")
    void parseScore_variousInputs() throws Exception {
        assertThat(invokeParseScore("4.5")).isEqualTo(4.5);
        // "4/5" becomes "45" after stripping non-digits and is clamped to 5.0
        assertThat(invokeParseScore("4/5")).isEqualTo(5.0);
        assertThat(invokeParseScore("10")).isEqualTo(5.0); // clamped to max
        assertThat(invokeParseScore("0")).isEqualTo(1.0);  // clamped to min
        assertThat(invokeParseScore("not-a-number")).isEqualTo(3.0); // default
    }

    @Test
    @DisplayName("parseQualityAssessment should populate scores and use provided OVERALL")
    void parseQualityAssessment_withOverall() throws Exception {
        String eval = "CLARITY: 4.0\n" +
                "RELEVANCE: 4.5\n" +
                "FACTUAL: 3.5\n" +
                "HELPFULNESS: 5\n" +
                "OVERALL: 4.2\n" +
                "ISSUES: none";

        Object assessment = invokeParseQualityAssessment(eval);

        assertThat(getAssessmentField(assessment, "clarityScore")).isEqualTo(4.0);
        assertThat(getAssessmentField(assessment, "relevanceScore")).isEqualTo(4.5);
        assertThat(getAssessmentField(assessment, "factualScore")).isEqualTo(3.5);
        assertThat(getAssessmentField(assessment, "helpfulnessScore")).isEqualTo(5.0);
        assertThat(getAssessmentField(assessment, "overallScore")).isEqualTo(4.2);
        assertThat(getAssessmentStringField(assessment, "factualIssues")).isEqualTo("none");
    }

    @Test
    @DisplayName("parseQualityAssessment should compute OVERALL when not provided")
    void parseQualityAssessment_computesOverallWhenMissing() throws Exception {
        String eval = "CLARITY: 4\n" +
                "RELEVANCE: 3\n" +
                "FACTUAL: 2\n" +
                "HELPFULNESS: 5\n" +
                "ISSUES: factual issue present";

        Object assessment = invokeParseQualityAssessment(eval);

        double overall = getAssessmentField(assessment, "overallScore");
        // (4 + 3 + 2 + 5) / 4 = 3.5
        assertThat(overall).isEqualTo(3.5);
        assertThat(getAssessmentStringField(assessment, "factualIssues"))
                .isEqualTo("factual issue present");
    }

    @Test
    @DisplayName("parseQualityAssessment should fall back when evaluation string is null")
    void parseQualityAssessment_nullInput_usesFallback() throws Exception {
        Object assessment = invokeParseQualityAssessment(null);

        // Fallback assessment initializes all scores to 3.0
        assertThat(getAssessmentField(assessment, "overallScore")).isEqualTo(3.0);
        assertThat(getAssessmentStringField(assessment, "factualIssues"))
                .isEqualTo("Unable to verify - evaluation failed");
    }

    @Test
    @DisplayName("logQualityIssues should run without error for low scores and custom issues")
    void logQualityIssues_executesForLowScores() throws Exception {
        // Build a minimal assessment instance via reflection
        Class<?> clazz = Class.forName("com.vijay.manager.MultiCriteriaJudgeAdvisor$QualityAssessment");
        var ctor = clazz.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object assessment = ctor.newInstance();

        var clarityField = clazz.getDeclaredField("clarityScore");
        var relevanceField = clazz.getDeclaredField("relevanceScore");
        var factualField = clazz.getDeclaredField("factualScore");
        var helpfulField = clazz.getDeclaredField("helpfulnessScore");
        var issuesField = clazz.getDeclaredField("factualIssues");
        clarityField.setAccessible(true);
        relevanceField.setAccessible(true);
        factualField.setAccessible(true);
        helpfulField.setAccessible(true);
        issuesField.setAccessible(true);

        clarityField.setDouble(assessment, 2.5);
        relevanceField.setDouble(assessment, 2.0);
        factualField.setDouble(assessment, 1.5);
        helpfulField.setDouble(assessment, 2.5);
        issuesField.set(assessment, "Some factual issues detected");

        // We don't assert logs; just ensure the method executes without throwing
        invokeLogQualityIssues(assessment);
    }
}
