package com.vijay.memory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationMemoryServiceTest {

    private ConversationMemoryService service;

    @BeforeEach
    void setUp() {
        service = new ConversationMemoryService();
    }

    @Test
    @DisplayName("getOrCreateContext should create new context and reuse existing one for same id")
    void getOrCreateContext_createsAndReusesContext() {
        String conversationId = "conv-1";
        String userId = "user-1";

        ConversationMemoryService.ConversationContext first =
                service.getOrCreateContext(conversationId, userId);

        assertThat(first).isNotNull();
        assertThat(first.getConversationId()).isEqualTo(conversationId);
        assertThat(first.getUserId()).isEqualTo(userId);

        ConversationMemoryService.ConversationContext second =
                service.getOrCreateContext(conversationId, "other-user");

        // Same object should be returned and original userId preserved
        assertThat(second).isSameAs(first);
        assertThat(second.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("storeUserInfo and getUserInfo should read/write from existing context")
    void storeAndGetUserInfo_existingContext() {
        String conversationId = "conv-user-info";
        service.getOrCreateContext(conversationId, "user-1");

        service.storeUserInfo(conversationId, "name", "Vijay");

        Object value = service.getUserInfo(conversationId, "name");
        assertThat(value).isEqualTo("Vijay");
    }

    @Test
    @DisplayName("storeUserInfo on missing context should not create new context")
    void storeUserInfo_missingContext_doesNotCreateConversation() {
        String conversationId = "missing-conv";

        service.storeUserInfo(conversationId, "key", "value");

        assertThat(service.getContext(conversationId)).isNull();
        assertThat(service.getUserInfo(conversationId, "key")).isNull();
    }

    @Test
    @DisplayName("addMessage and getConversationHistory should append and return a copy of history")
    void addMessage_andGetConversationHistory() {
        String conversationId = "conv-history";
        service.getOrCreateContext(conversationId, "user-1");

        ConversationMemoryService.ChatMessage msg1 =
                new ConversationMemoryService.ChatMessage("user", "hello");
        ConversationMemoryService.ChatMessage msg2 =
                new ConversationMemoryService.ChatMessage("assistant", "hi there");

        service.addMessage(conversationId, msg1);
        service.addMessage(conversationId, msg2);

        List<ConversationMemoryService.ChatMessage> history =
                service.getConversationHistory(conversationId);

        assertThat(history).hasSize(2);
        assertThat(history.get(0).getContent()).isEqualTo("hello");
        assertThat(history.get(1).getContent()).isEqualTo("hi there");

        // Mutating returned list should not affect internal history
        history.clear();
        List<ConversationMemoryService.ChatMessage> historyAgain =
                service.getConversationHistory(conversationId);
        assertThat(historyAgain).hasSize(2);
    }

    @Test
    @DisplayName("getConversationHistory should return empty list for unknown conversation")
    void getConversationHistory_missingConversation_returnsEmptyList() {
        List<ConversationMemoryService.ChatMessage> history =
                service.getConversationHistory("unknown");

        assertThat(history).isNotNull();
        assertThat(history).isEmpty();
    }

    @Test
    @DisplayName("storeUserProfile and getUserProfile should persist profile")
    void storeAndGetUserProfile() {
        String userId = "user-123";
        ConversationMemoryService.UserProfile profile =
                new ConversationMemoryService.UserProfile(userId);
        profile.setName("Vijay");
        profile.setEmail("vijay@example.com");

        service.storeUserProfile(userId, profile);

        ConversationMemoryService.UserProfile loaded = service.getUserProfile(userId);
        assertThat(loaded.getUserId()).isEqualTo(userId);
        assertThat(loaded.getName()).isEqualTo("Vijay");
        assertThat(loaded.getEmail()).isEqualTo("vijay@example.com");
    }

    @Test
    @DisplayName("getUserProfile should return new default profile when none exists")
    void getUserProfile_missing_returnsDefaultProfile() {
        String userId = "new-user";

        ConversationMemoryService.UserProfile profile = service.getUserProfile(userId);

        assertThat(profile).isNotNull();
        assertThat(profile.getUserId()).isEqualTo(userId);
        assertThat(profile.getPreferences()).isEmpty();
        assertThat(profile.getConversationIds()).isEmpty();
    }

    @Test
    @DisplayName("extractAndRememberName should parse name and store it in user info")
    void extractAndRememberName_validMessage() {
        String conversationId = "conv-name";
        service.getOrCreateContext(conversationId, "user-1");

        String name = service.extractAndRememberName(conversationId, "Hi, my name is Vijay!");

        assertThat(name).isEqualTo("Vijay");
        assertThat(service.getRememberedName(conversationId)).isEqualTo("Vijay");
    }

    @Test
    @DisplayName("extractAndRememberName should return null when no name is present")
    void extractAndRememberName_noName_returnsNull() {
        String conversationId = "conv-noname";
        service.getOrCreateContext(conversationId, "user-1");

        String name = service.extractAndRememberName(conversationId, "Tell me a joke");

        assertThat(name).isNull();
        assertThat(service.getRememberedName(conversationId)).isNull();
    }

    @Test
    @DisplayName("storeMetadata and getMetadata should round-trip values")
    void storeAndGetMetadata() {
        String conversationId = "conv-meta";
        service.getOrCreateContext(conversationId, "user-1");

        service.storeMetadata(conversationId, "topic", "spring-boot");

        Object value = service.getMetadata(conversationId, "topic");
        assertThat(value).isEqualTo("spring-boot");
    }

    @Test
    @DisplayName("clearConversation should remove context and history")
    void clearConversation_removesContext() {
        String conversationId = "conv-clear";
        service.getOrCreateContext(conversationId, "user-1");
        service.addMessage(conversationId, new ConversationMemoryService.ChatMessage("user", "hello"));

        assertThat(service.getContext(conversationId)).isNotNull();

        service.clearConversation(conversationId);

        assertThat(service.getContext(conversationId)).isNull();
        assertThat(service.getConversationHistory(conversationId)).isEmpty();
    }

    @Test
    @DisplayName("getUserConversations should filter conversations by user id")
    void getUserConversations_filtersByUserId() {
        service.getOrCreateContext("c1", "user-A");
        service.getOrCreateContext("c2", "user-B");
        service.getOrCreateContext("c3", "user-A");

        List<ConversationMemoryService.ConversationContext> userAConversations =
                service.getUserConversations("user-A");

        assertThat(userAConversations)
                .extracting(ConversationMemoryService.ConversationContext::getConversationId)
                .containsExactlyInAnyOrder("c1", "c3");

        assertThat(service.getUserConversations("user-B"))
                .extracting(ConversationMemoryService.ConversationContext::getConversationId)
                .containsExactly("c2");
    }
}
