package com.vijay.memory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LongTermMemoryTest {

    private LongTermMemory longTermMemory;

    @BeforeEach
    void setUp() {
        longTermMemory = new LongTermMemory();
    }

    @Test
    @DisplayName("getOrCreateProfile should create and reuse profile for same user")
    void getOrCreateProfile_createsAndReusesProfile() {
        String userId = "user-1";

        LongTermMemory.UserProfile first = longTermMemory.getOrCreateProfile(userId);
        LongTermMemory.UserProfile second = longTermMemory.getOrCreateProfile(userId);

        assertThat(first).isNotNull();
        assertThat(second).isSameAs(first);
        assertThat(first.userId).isEqualTo(userId);
    }

    @Test
    @DisplayName("updateProfile should replace existing profile contents")
    void updateProfile_replacesExistingProfile() {
        String userId = "user-2";
        LongTermMemory.UserProfile original = longTermMemory.getOrCreateProfile(userId);
        original.setName("Original");

        LongTermMemory.UserProfile updated = new LongTermMemory.UserProfile(userId);
        updated.setName("Updated");
        updated.setEmail("updated@example.com");

        longTermMemory.updateProfile(userId, updated);

        Optional<LongTermMemory.UserProfile> loadedOpt = longTermMemory.getProfile(userId);
        assertThat(loadedOpt).isPresent();
        LongTermMemory.UserProfile loaded = loadedOpt.get();
        assertThat(loaded).isSameAs(updated);
        assertThat(loaded.name).isEqualTo("Updated");
        assertThat(loaded.email).isEqualTo("updated@example.com");
    }

    @Test
    @DisplayName("getProfile should return empty Optional when no profile exists")
    void getProfile_missing_returnsEmptyOptional() {
        Optional<LongTermMemory.UserProfile> profile = longTermMemory.getProfile("missing-user");
        assertThat(profile).isEmpty();
    }

    @Test
    @DisplayName("addProject, addTool and setPreference should update profile state")
    void addProject_addTool_setPreference_updatesProfile() {
        String userId = "user-3";

        longTermMemory.addProject(userId, "project-alpha");
        longTermMemory.addTool(userId, "IntelliJ");
        longTermMemory.setPreference(userId, "theme", "dark");

        LongTermMemory.UserProfile profile = longTermMemory.getOrCreateProfile(userId);
        assertThat(profile.projects).containsExactly("project-alpha");
        assertThat(profile.tools).containsExactly("IntelliJ");
        assertThat(profile.preferences).containsEntry("theme", "dark");

        String pref = longTermMemory.getPreference(userId, "theme", "light");
        assertThat(pref).isEqualTo("dark");
    }

    @Test
    @DisplayName("getPreference should fall back to default when key missing")
    void getPreference_missingKey_returnsDefaultValue() {
        String userId = "user-4";
        longTermMemory.getOrCreateProfile(userId);

        String pref = longTermMemory.getPreference(userId, "missing-key", "default");
        assertThat(pref).isEqualTo("default");
    }

    @Test
    @DisplayName("getContextForLLM should include name, projects, tools, skills and preferences when present")
    void getContextForLLM_includesAvailableFields() {
        String userId = "user-5";

        LongTermMemory.UserProfile profile = longTermMemory.getOrCreateProfile(userId);
        profile.setName("Vijay");
        profile.setEmail("vijay@example.com");
        profile.addSkill("Java");

        longTermMemory.addProject(userId, "Coding-Assistance");
        longTermMemory.addTool(userId, "Spring Boot");
        longTermMemory.setPreference(userId, "timezone", "IST");

        String context = longTermMemory.getContextForLLM(userId);

        assertThat(context).contains("User Profile:");
        assertThat(context).contains("Name: Vijay");
        assertThat(context).contains("Projects: Coding-Assistance");
        assertThat(context).contains("Tools: Spring Boot");
        assertThat(context).contains("Skills: Java");
        assertThat(context).contains("timezone: IST");
    }
}
