package com.vijay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.model.EditHistory;
import com.vijay.model.UserPattern;
import com.vijay.service.EditHistoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

@WebMvcTest(EditHistoryController.class)
class EditHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EditHistoryService editHistoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/edits/track should return success with editId")
    void trackEdit_shouldReturnSuccess() throws Exception {
        // Arrange
        EditHistory saved = EditHistory.builder()
                .id(1L)
                .userId("user123")
                .filePath("Test.java")
                .createdAt(LocalDateTime.now())
                .build();

        when(editHistoryService.trackEdit(
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), anyString()
        )).thenReturn(saved);

        Map<String, Object> request = Map.of(
                "userId", "user123",
                "filePath", "Test.java",
                "originalCode", "public void test() {}",
                "editedCode", "public void testMethod() {}",
                "editType", "rename_method",
                "suggestionSource", "AI",
                "accepted", true,
                "description", "Renamed method"
        );

        // Act & Assert
        mockMvc.perform(post("/api/edits/track")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.editId").value(1L));
    }

    @Test
    @DisplayName("POST /api/edits/track should return error when service throws")
    void trackEdit_whenServiceThrows_returnsError() throws Exception {
        Mockito.when(editHistoryService.trackEdit(
                        anyString(), anyString(), anyString(), anyString(),
                        anyString(), anyString(), any(), anyString()))
                .thenThrow(new RuntimeException("failure"));

        Map<String, Object> request = Map.of("userId", "user123", "filePath", "Test.java");

        mockMvc.perform(post("/api/edits/track")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @DisplayName("GET /api/edits/history/{userId} should return paginated history")
    void getUserEditHistory_shouldReturnPagedHistory() throws Exception {
        // Arrange
        String userId = "user123";

        EditHistory e1 = EditHistory.builder().id(1L).userId(userId).filePath("A.java").build();
        EditHistory e2 = EditHistory.builder().id(2L).userId(userId).filePath("B.java").build();
        Page<EditHistory> page = new PageImpl<>(List.of(e1, e2), PageRequest.of(0, 10), 2);

        when(editHistoryService.getUserEditHistory(userId, 0, 10)).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/edits/history/{userId}", userId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.totalEdits").value(2))
                .andExpect(jsonPath("$.edits", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/edits/recent/{userId} should return recent edits")
    void getRecentEdits_shouldReturnRecentEdits() throws Exception {
        // Arrange
        String userId = "user123";

        EditHistory e1 = EditHistory.builder().id(1L).userId(userId).filePath("A.java").build();
        EditHistory e2 = EditHistory.builder().id(2L).userId(userId).filePath("B.java").build();

        when(editHistoryService.getRecentEdits(userId, 5)).thenReturn(List.of(e1, e2));

        // Act & Assert
        mockMvc.perform(get("/api/edits/recent/{userId}", userId)
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.edits", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/edits/stats/{userId} should return statistics")
    void getUserStatistics_shouldReturnStats() throws Exception {
        // Arrange
        String userId = "user123";

        Map<String, Object> stats = Map.of(
                "userId", userId,
                "totalEdits", 5L,
                "acceptedEdits", 3L,
                "rejectedEdits", 2L,
                "acceptanceRate", "60.00%"
        );

        when(editHistoryService.getUserStatistics(userId)).thenReturn(stats);

        // Act & Assert
        mockMvc.perform(get("/api/edits/stats/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.totalEdits").value(5))
                .andExpect(jsonPath("$.acceptedEdits").value(3))
                .andExpect(jsonPath("$.rejectedEdits").value(2))
                .andExpect(jsonPath("$.acceptanceRate").value("60.00%"));
    }

    @Test
    @DisplayName("GET /api/edits/patterns/{userId} should return user patterns")
    void getUserPatterns_shouldReturnPatterns() throws Exception {
        String userId = "user123";
        UserPattern pattern = UserPattern.builder()
                .userId(userId)
                .patternType("extract_method")
                .frequency(10)
                .acceptanceRate(0.8)
                .active(true)
                .build();

        when(editHistoryService.getUserPatterns(userId)).thenReturn(List.of(pattern));

        mockMvc.perform(get("/api/edits/patterns/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.patternCount").value(1));
    }

    @Test
    @DisplayName("GET /api/edits/patterns/high-acceptance/{userId} should return high-acceptance patterns")
    void getHighAcceptancePatterns_shouldReturnPatterns() throws Exception {
        String userId = "user123";
        UserPattern pattern = UserPattern.builder()
                .userId(userId)
                .patternType("rename_variable")
                .frequency(5)
                .acceptanceRate(0.9)
                .active(true)
                .build();

        when(editHistoryService.getHighAcceptancePatterns(userId, 0.7)).thenReturn(List.of(pattern));

        mockMvc.perform(get("/api/edits/patterns/high-acceptance/{userId}", userId)
                        .param("minRate", "0.7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.minAcceptanceRate").value(0.7))
                .andExpect(jsonPath("$.patternCount").value(1));
    }

    @Test
    @DisplayName("GET /api/edits/patterns/frequent/{userId} should return most frequent patterns")
    void getMostFrequentPatterns_shouldReturnPatterns() throws Exception {
        String userId = "user123";
        UserPattern pattern = UserPattern.builder()
                .userId(userId)
                .patternType("add_comments")
                .frequency(20)
                .acceptanceRate(0.6)
                .active(true)
                .build();

        when(editHistoryService.getMostFrequentPatterns(userId)).thenReturn(List.of(pattern));

        mockMvc.perform(get("/api/edits/patterns/frequent/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.patternCount").value(1));
    }

    @Test
    @DisplayName("GET /api/edits/acceptance-rate/{userId} should return formatted acceptance rate")
    void getAcceptanceRate_shouldReturnFormattedRate() throws Exception {
        String userId = "user123";
        when(editHistoryService.getAcceptanceRate(userId)).thenReturn(0.75);

        mockMvc.perform(get("/api/edits/acceptance-rate/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.acceptanceRate").value("0.75%"))
                .andExpect(jsonPath("$.rawRate").value(0.75));
    }

    @Test
    @DisplayName("GET /api/edits/acceptance-rate/{userId} should return N/A when rate is null")
    void getAcceptanceRate_nullRate_returnsNA() throws Exception {
        String userId = "user123";
        when(editHistoryService.getAcceptanceRate(userId)).thenReturn(null);

        mockMvc.perform(get("/api/edits/acceptance-rate/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.acceptanceRate").value("N/A"))
                .andExpect(jsonPath("$.rawRate").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    @DisplayName("GET /api/edits/range/{userId} should return edits for date range")
    void getEditsByDateRange_shouldReturnEdits() throws Exception {
        String userId = "user123";
        EditHistory e1 = EditHistory.builder().id(1L).userId(userId).filePath("A.java").build();

        when(editHistoryService.getEditsByDateRange(anyString(), any(), any()))
                .thenReturn(List.of(e1));

        mockMvc.perform(get("/api/edits/range/{userId}", userId)
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.editCount").value(1));
    }
}
