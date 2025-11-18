package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfilingServiceTest {

    private UserProfilingService service;

    @BeforeEach
    void setUp() {
        service = new UserProfilingService();
    }

    @Test
    @DisplayName("recordInteraction should update expertise, specialization and interaction stats")
    void recordInteraction_updatesProfile() {
        String userId = "user1";

        service.recordInteraction(userId, "ARCHITECTURE", 5, "spring");
        service.recordInteraction(userId, "DEBUGGING", 4, "java");
        service.recordInteraction(userId, "GENERAL", 3, "spring");

        int expertise = service.detectExpertiseLevel(userId);
        List<String> specs = service.getUserSpecializations(userId);
        UserProfilingService.UserProfileSummary summary = service.getProfileSummary(userId);

        assertThat(expertise).isGreaterThanOrEqualTo(2);
        assertThat(specs).contains("spring");
        assertThat(summary.interactionCount).isEqualTo(3);
        assertThat(summary.averageQuality).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("recordFeedback and updatePreference should adjust expertise and response format")
    void recordFeedback_and_updatePreference() {
        String userId = "user2";

        for (int i = 0; i < 5; i++) {
            service.recordFeedback(userId, 5, "great");
        }

        int expertise = service.detectExpertiseLevel(userId);
        assertThat(expertise).isGreaterThanOrEqualTo(3);

        service.updatePreference(userId, "responseFormat", "code-heavy");
        assertThat(service.getPreferredResponseFormat(userId)).isEqualTo("code-heavy");
    }
}
