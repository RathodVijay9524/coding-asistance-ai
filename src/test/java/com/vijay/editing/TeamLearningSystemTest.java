package com.vijay.editing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TeamLearningSystemTest {

    private ObjectMapper objectMapper;
    private TeamLearningSystem system;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        system = new TeamLearningSystem(objectMapper);
    }

    @Test
    @DisplayName("learnFromCodebase should produce a team profile with patterns and practices")
    void learnFromCodebase_basic() throws Exception {
        String json = system.learnFromCodebase("/project/path", "TeamA");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("teamProfile").get("teamName").asText()).isEqualTo("TeamA");
        assertThat(root.get("patternsFound").asInt()).isGreaterThan(0);
        assertThat(root.get("practicesFound").asInt()).isGreaterThan(0);
    }

    @Test
    @DisplayName("extractTeamPatterns should return patterns and categorized map")
    void extractTeamPatterns_basic() throws Exception {
        String json = system.extractTeamPatterns("/project/path");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("patterns").isArray()).isTrue();
        assertThat(root.get("categorized").isObject()).isTrue();
    }

    @Test
    @DisplayName("getTeamSuggestions should use team profile to generate suggestions")
    void getTeamSuggestions_basic() throws Exception {
        // Build a simple TeamProfile JSON
        TeamLearningSystem.TeamProfile profile = new TeamLearningSystem.TeamProfile();
        profile.setTeamName("TeamA");
        TeamLearningSystem.NamingConvention convention = new TeamLearningSystem.NamingConvention();
        convention.setStyle("camelCase");
        convention.setExamples(java.util.List.of("userName"));
        convention.setConsistency(0.9);
        profile.setNamingConvention(convention);
        profile.setPatterns(java.util.List.of(
                new TeamLearningSystem.CodePattern("Singleton", "Design Patterns", "desc")
        ));
        profile.setBestPractices(java.util.List.of(
                new TeamLearningSystem.BestPractice("Use try-catch", "Error handling")
        ));

        String profileJson = objectMapper.writeValueAsString(profile);
        String code = "public class Singleton {}";

        String json = system.getTeamSuggestions(code, profileJson);

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("status").asText()).isEqualTo("success");
        assertThat(root.get("suggestions").isArray()).isTrue();
        assertThat(root.get("teamName").asText()).isEqualTo("TeamA");
    }
}
