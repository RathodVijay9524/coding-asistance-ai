package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileWatchingToolServiceTest {

    private ObjectMapper objectMapper;
    private FileWatchingToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new FileWatchingToolService(objectMapper);
    }

    @Test
    @DisplayName("startWatching and stopWatching should update session state")
    void startAndStopWatching_basic() throws Exception {
        String startJson = service.startWatching("/tmp/project", "*.java");

        JsonNode start = objectMapper.readTree(startJson);
        String sessionId = start.get("sessionId").asText();
        assertThat(start.get("status").asText()).isEqualTo("success");

        String stopJson = service.stopWatching(sessionId);
        JsonNode stop = objectMapper.readTree(stopJson);
        assertThat(stop.get("status").asText()).isEqualTo("success");
        assertThat(stop.get("sessionId").asText()).isEqualTo(sessionId);
    }

    @Test
    @DisplayName("getActiveSessions should return JSON with sessions array")
    void getActiveSessions_basic() throws Exception {
        service.startWatching("/tmp/project", "*.java");

        String json = service.getActiveSessions();

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("totalSessions")).isTrue();
        assertThat(root.get("sessions").isArray()).isTrue();
    }
}
