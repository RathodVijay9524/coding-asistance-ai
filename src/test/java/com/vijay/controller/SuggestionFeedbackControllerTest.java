package com.vijay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.model.SuggestionFeedback;
import com.vijay.service.SuggestionFeedbackService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SuggestionFeedbackController.class)
class SuggestionFeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SuggestionFeedbackService suggestionFeedbackService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/feedback/record should return success with feedbackId")
    void recordFeedback_shouldReturnSuccess() throws Exception {
        // Arrange
        SuggestionFeedback saved = SuggestionFeedback.builder()
                .id(100L)
                .userId("user123")
                .suggestionId(10L)
                .rating(5)
                .createdAt(LocalDateTime.now())
                .build();

        when(suggestionFeedbackService.recordFeedback(
                anyString(), anyLong(), anyString(), anyString(),
                any(), anyString(), anyString(), anyString(), anyString(),
                any(), any(), any(), anyString()
        )).thenReturn(saved);

        Map<String, Object> request = new java.util.HashMap<>();
        request.put("userId", "user123");
        request.put("suggestionId", 10);
        request.put("suggestionType", "extract_method");
        request.put("suggestionContent", "public void helper() {}");
        request.put("rating", 5);
        request.put("action", "accepted");
        request.put("feedback", "Great suggestion");
        request.put("userModification", "Added logs");
        request.put("reason", "More readable");
        request.put("helpful", true);
        request.put("relevant", true);
        request.put("accurate", true);
        request.put("sentiment", "positive");

        // Act & Assert
        mockMvc.perform(post("/api/feedback/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.feedbackId").value(100L));
    }

    @Test
    @DisplayName("GET /api/feedback/history/{userId} should return paged feedback")
    void getUserFeedback_shouldReturnPagedFeedback() throws Exception {
        // Arrange
        String userId = "user123";

        SuggestionFeedback f1 = SuggestionFeedback.builder().id(1L).userId(userId).rating(5).build();
        SuggestionFeedback f2 = SuggestionFeedback.builder().id(2L).userId(userId).rating(3).build();
        Page<SuggestionFeedback> page = new PageImpl<>(List.of(f1, f2), PageRequest.of(0, 10), 2);

        when(suggestionFeedbackService.getUserFeedback(userId, 0, 10)).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/feedback/history/{userId}", userId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.totalFeedback").value(2))
                .andExpect(jsonPath("$.feedback", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/feedback/stats/{userId} should return feedback statistics")
    void getUserFeedbackStatistics_shouldReturnStats() throws Exception {
        // Arrange
        String userId = "user123";
        Map<String, Object> stats = Map.of(
                "userId", userId,
                "totalFeedback", 10,
                "averageRating", "4.20",
                "helpfulCount", 8,
                "acceptanceRate", "80.00%"
        );

        when(suggestionFeedbackService.getUserFeedbackStatistics(userId)).thenReturn(stats);

        // Act & Assert
        mockMvc.perform(get("/api/feedback/stats/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.totalFeedback").value(10))
                .andExpect(jsonPath("$.averageRating").value("4.20"))
                .andExpect(jsonPath("$.helpfulCount").value(8))
                .andExpect(jsonPath("$.acceptanceRate").value("80.00%"));
    }

    @Test
    @DisplayName("GET /api/feedback/suggestion/{suggestionId} should return feedback list")
    void getSuggestionFeedback_shouldReturnList() throws Exception {
        Long suggestionId = 10L;

        SuggestionFeedback f1 = SuggestionFeedback.builder().id(1L).suggestionId(suggestionId).build();
        SuggestionFeedback f2 = SuggestionFeedback.builder().id(2L).suggestionId(suggestionId).build();

        when(suggestionFeedbackService.getSuggestionFeedback(suggestionId))
                .thenReturn(List.of(f1, f2));

        mockMvc.perform(get("/api/feedback/suggestion/{id}", suggestionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.suggestionId").value(suggestionId))
                .andExpect(jsonPath("$.feedbackCount").value(2))
                .andExpect(jsonPath("$.feedback", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/feedback/rating/{userId} should return feedback filtered by rating")
    void getFeedbackByRating_shouldReturnList() throws Exception {
        String userId = "user123";

        SuggestionFeedback f1 = SuggestionFeedback.builder().id(1L).userId(userId).rating(5).build();

        when(suggestionFeedbackService.getFeedbackByRating(userId, 5))
                .thenReturn(List.of(f1));

        mockMvc.perform(get("/api/feedback/rating/{userId}", userId)
                        .param("rating", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.feedbackCount").value(1));
    }

    @Test
    @DisplayName("GET /api/feedback/action/{userId} should return feedback filtered by action")
    void getFeedbackByAction_shouldReturnList() throws Exception {
        String userId = "user123";

        SuggestionFeedback f1 = SuggestionFeedback.builder().id(1L).userId(userId).action("accepted").build();

        when(suggestionFeedbackService.getFeedbackByAction(userId, "accepted"))
                .thenReturn(List.of(f1));

        mockMvc.perform(get("/api/feedback/action/{userId}", userId)
                        .param("action", "accepted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.action").value("accepted"))
                .andExpect(jsonPath("$.feedbackCount").value(1));
    }

    @Test
    @DisplayName("GET /api/feedback/helpful/{userId} should return helpful feedback list")
    void getHelpfulFeedback_shouldReturnList() throws Exception {
        String userId = "user123";

        SuggestionFeedback f1 = SuggestionFeedback.builder().id(1L).userId(userId).helpful(true).build();

        when(suggestionFeedbackService.getHelpfulFeedback(userId))
                .thenReturn(List.of(f1));

        mockMvc.perform(get("/api/feedback/helpful/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.feedbackCount").value(1));
    }

    @Test
    @DisplayName("GET /api/feedback/not-helpful/{userId} should return not helpful feedback list")
    void getNotHelpfulFeedback_shouldReturnList() throws Exception {
        String userId = "user123";

        SuggestionFeedback f1 = SuggestionFeedback.builder().id(1L).userId(userId).helpful(false).build();

        when(suggestionFeedbackService.getNotHelpfulFeedback(userId))
                .thenReturn(List.of(f1));

        mockMvc.perform(get("/api/feedback/not-helpful/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.feedbackCount").value(1));
    }

    @Test
    @DisplayName("GET /api/feedback/sentiment/{userId} should return feedback filtered by sentiment")
    void getFeedbackBySentiment_shouldReturnList() throws Exception {
        String userId = "user123";

        SuggestionFeedback f1 = SuggestionFeedback.builder().id(1L).userId(userId).sentiment("positive").build();

        when(suggestionFeedbackService.getFeedbackBySentiment(userId, "positive"))
                .thenReturn(List.of(f1));

        mockMvc.perform(get("/api/feedback/sentiment/{userId}", userId)
                        .param("sentiment", "positive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.sentiment").value("positive"))
                .andExpect(jsonPath("$.feedbackCount").value(1));
    }

    @Test
    @DisplayName("GET /api/feedback/effectiveness/{userId} should return effectiveness map")
    void getSuggestionEffectiveness_shouldReturnMap() throws Exception {
        String userId = "user123";
        Map<String, Object> effectiveness = Map.of("accepted", 5, "rejected", 2);

        when(suggestionFeedbackService.getSuggestionEffectiveness(userId)).thenReturn(effectiveness);

        mockMvc.perform(get("/api/feedback/effectiveness/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.effectiveness.accepted").value(5));
    }

    @Test
    @DisplayName("GET /api/feedback/most-helpful/{userId} should return most helpful suggestion types")
    void getMostHelpfulSuggestionTypes_shouldReturnList() throws Exception {
        String userId = "user123";
        Map<String, Object> type = Map.of("type", "extract_method", "count", 3);

        when(suggestionFeedbackService.getMostHelpfulSuggestionTypes(userId))
                .thenReturn(List.of(type));

        mockMvc.perform(get("/api/feedback/most-helpful/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.suggestionTypes", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/feedback/least-helpful/{userId} should return least helpful suggestion types")
    void getLeastHelpfulSuggestionTypes_shouldReturnList() throws Exception {
        String userId = "user123";
        Map<String, Object> type = Map.of("type", "rename_variable", "count", 1);

        when(suggestionFeedbackService.getLeastHelpfulSuggestionTypes(userId))
                .thenReturn(List.of(type));

        mockMvc.perform(get("/api/feedback/least-helpful/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.suggestionTypes", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/feedback/recent/{userId} should return recent feedback list")
    void getRecentFeedback_shouldReturnList() throws Exception {
        String userId = "user123";

        SuggestionFeedback f1 = SuggestionFeedback.builder().id(1L).userId(userId).build();

        when(suggestionFeedbackService.getRecentFeedback(userId, 5))
                .thenReturn(List.of(f1));

        mockMvc.perform(get("/api/feedback/recent/{userId}", userId)
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @DisplayName("GET /api/feedback/range/{userId} should return feedback for date range")
    void getFeedbackByDateRange_shouldReturnList() throws Exception {
        String userId = "user123";

        SuggestionFeedback f1 = SuggestionFeedback.builder().id(1L).userId(userId).build();

        when(suggestionFeedbackService.getFeedbackByDateRange(anyString(), any(), any()))
                .thenReturn(List.of(f1));

        mockMvc.perform(get("/api/feedback/range/{userId}", userId)
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.feedbackCount").value(1));
    }

    @Test
    @DisplayName("PUT /api/feedback/update/{feedbackId} should return success when updated")
    void updateFeedback_shouldReturnSuccess() throws Exception {
        Long feedbackId = 42L;
        SuggestionFeedback updated = SuggestionFeedback.builder().id(feedbackId).build();

        when(suggestionFeedbackService.updateFeedback(eq(feedbackId), any(), anyString(), anyString(), any(), any(), any(), anyString()))
                .thenReturn(updated);

        Map<String, Object> request = Map.of(
                "rating", 4,
                "action", "modified",
                "feedback", "Updated feedback",
                "helpful", true,
                "relevant", true,
                "accurate", true,
                "sentiment", "positive"
        );

        mockMvc.perform(put("/api/feedback/update/{feedbackId}", feedbackId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.feedbackId").value(feedbackId));
    }

    @Test
    @DisplayName("DELETE /api/feedback/delete/{feedbackId} should return success when deleted")
    void deleteFeedback_shouldReturnSuccess() throws Exception {
        Long feedbackId = 55L;

        mockMvc.perform(delete("/api/feedback/delete/{feedbackId}", feedbackId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.feedbackId").value(feedbackId));
    }

    @Test
    @DisplayName("POST /api/feedback/record should return error when service throws")
    void recordFeedback_whenServiceThrows_returnsError() throws Exception {
        when(suggestionFeedbackService.recordFeedback(anyString(), anyLong(), anyString(), anyString(),
                any(), anyString(), anyString(), anyString(), anyString(), any(), any(), any(), anyString()))
                .thenThrow(new RuntimeException("failure"));

        Map<String, Object> request = Map.of("userId", "user123", "suggestionId", 10);

        mockMvc.perform(post("/api/feedback/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }
}
