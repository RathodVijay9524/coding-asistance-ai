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
 * 🌐 INTEGRATION TEST: Edit History
 *
 * Uses real Spring context + real DB (MySQL via application.properties).
 * Verifies that tracking an edit persists correctly and can be queried back.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class EditHistoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Track edit and verify it appears in user history")
    void trackEdit_andVerifyHistory() throws Exception {
        // Arrange
        String userId = "it-user-1";

        String trackRequestJson = "{" +
                "\"userId\":\"" + userId + "\"," +
                "\"filePath\":\"src/main/java/Test.java\"," +
                "\"originalCode\":\"public void test() {}\"," +
                "\"editedCode\":\"public void testMethod() {}\"," +
                "\"editType\":\"rename_method\"," +
                "\"suggestionSource\":\"AI\"," +
                "\"accepted\":true," +
                "\"description\":\"Renamed method\"" +
                "}";

        // Act: track edit
        mockMvc.perform(post("/api/edits/track")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(trackRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.editId", notNullValue()));

        // Assert: fetch history and see at least one entry
        mockMvc.perform(get("/api/edits/history/{userId}", userId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.totalEdits", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.edits", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.edits[0].filePath", containsString("Test.java")));
    }
}
