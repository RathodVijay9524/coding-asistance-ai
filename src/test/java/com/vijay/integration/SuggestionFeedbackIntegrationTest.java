package com.vijay.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 🌐 INTEGRATION TEST: Suggestion Feedback
 *
 * Uses real Spring context + real DB.
 * Verifies that recording feedback persists and is reflected in stats.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class SuggestionFeedbackIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Record feedback and verify it appears in stats")
    void recordFeedback_andVerifyStats() throws Exception {
        // Arrange
        String userId = "it-user-feedback";

        String feedbackRequestJson = "{" +
                "\"userId\":\"" + userId + "\"," +
                "\"suggestionId\":1," +
                "\"suggestionType\":\"extract_method\"," +
                "\"suggestionContent\":\"public void helper() {}\"," +
                "\"rating\":5," +
                "\"action\":\"accepted\"," +
                "\"feedback\":\"Great suggestion\"," +
                "\"userModification\":\"Added logs\"," +
                "\"reason\":\"Clearer code\"," +
                "\"helpful\":true," +
                "\"relevant\":true," +
                "\"accurate\":true," +
                "\"sentiment\":\"positive\"" +
                "}";

        // Act: record feedback
        mockMvc.perform(post("/api/feedback/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(feedbackRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.feedbackId", notNullValue()));

        // Assert: stats reflect at least one feedback
        mockMvc.perform(get("/api/feedback/stats/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.totalFeedback", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.helpfulCount", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.acceptanceRate", notNullValue()));
    }

    @Test
    @DisplayName("Feedback filters: rating and helpful endpoints should reflect stored feedback")
    void feedbackFilters_ratingAndHelpful_shouldWork() throws Exception {
        // Arrange
        String userId = "it-user-filters";

        // First feedback: rating 5, accepted, helpful
        String feedback1 = "{" +
                "\"userId\":\"" + userId + "\"," +
                "\"suggestionId\":10," +
                "\"suggestionType\":\"extract_method\"," +
                "\"suggestionContent\":\"code1\"," +
                "\"rating\":5," +
                "\"action\":\"accepted\"," +
                "\"feedback\":\"Nice\"," +
                "\"userModification\":\"mod1\"," +
                "\"reason\":\"reason1\"," +
                "\"helpful\":true," +
                "\"relevant\":true," +
                "\"accurate\":true," +
                "\"sentiment\":\"positive\"" +
                "}";

        // Second feedback: rating 3, rejected, not helpful
        String feedback2 = "{" +
                "\"userId\":\"" + userId + "\"," +
                "\"suggestionId\":11," +
                "\"suggestionType\":\"rename_variable\"," +
                "\"suggestionContent\":\"code2\"," +
                "\"rating\":3," +
                "\"action\":\"rejected\"," +
                "\"feedback\":\"Not good\"," +
                "\"userModification\":\"mod2\"," +
                "\"reason\":\"reason2\"," +
                "\"helpful\":false," +
                "\"relevant\":false," +
                "\"accurate\":false," +
                "\"sentiment\":\"negative\"" +
                "}";

        // Insert two feedback records
        mockMvc.perform(post("/api/feedback/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(feedback1))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/feedback/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(feedback2))
                .andExpect(status().isOk());

        // Filter by rating=5
        mockMvc.perform(get("/api/feedback/rating/{userId}", userId)
                        .param("rating", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.feedbackCount", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.feedback[0].rating").value(5));

        // Helpful feedback endpoint
        mockMvc.perform(get("/api/feedback/helpful/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.feedbackCount", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.feedback[0].helpful").value(true));
    }

    @Test
    @DisplayName("Feedback filters: sentiment endpoint should filter by sentiment")
    void feedbackFilters_sentiment_shouldWork() throws Exception {
        // Arrange
        String userId = "it-user-sentiment";

        String feedbackPositive = "{" +
                "\"userId\":\"" + userId + "\"," +
                "\"suggestionId\":21," +
                "\"suggestionType\":\"extract_method\"," +
                "\"suggestionContent\":\"code-pos\"," +
                "\"rating\":5," +
                "\"action\":\"accepted\"," +
                "\"feedback\":\"Great\"," +
                "\"userModification\":\"m1\"," +
                "\"reason\":\"r1\"," +
                "\"helpful\":true," +
                "\"relevant\":true," +
                "\"accurate\":true," +
                "\"sentiment\":\"positive\"" +
                "}";

        String feedbackNegative = "{" +
                "\"userId\":\"" + userId + "\"," +
                "\"suggestionId\":22," +
                "\"suggestionType\":\"rename_variable\"," +
                "\"suggestionContent\":\"code-neg\"," +
                "\"rating\":2," +
                "\"action\":\"rejected\"," +
                "\"feedback\":\"Bad\"," +
                "\"userModification\":\"m2\"," +
                "\"reason\":\"r2\"," +
                "\"helpful\":false," +
                "\"relevant\":false," +
                "\"accurate\":false," +
                "\"sentiment\":\"negative\"" +
                "}";

        // Insert two feedback records with different sentiments
        mockMvc.perform(post("/api/feedback/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(feedbackPositive))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/feedback/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(feedbackNegative))
                .andExpect(status().isOk());

        // Filter by sentiment=positive
        mockMvc.perform(get("/api/feedback/sentiment/{userId}", userId)
                        .param("sentiment", "positive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.sentiment").value("positive"))
                .andExpect(jsonPath("$.feedbackCount", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.feedback[0].sentiment").value("positive"));
    }
}
