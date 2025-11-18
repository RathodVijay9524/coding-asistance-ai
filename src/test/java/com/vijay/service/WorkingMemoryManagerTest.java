package com.vijay.service;

import com.vijay.dto.WorkingMemoryState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WorkingMemoryManagerTest {

    private WorkingMemoryManager manager;

    @BeforeEach
    void setUp() {
        manager = new WorkingMemoryManager();
    }

    @Test
    @DisplayName("recordUserMessage, recordBrainOutput, recordIntent, recordTone should populate working memory")
    void recordData_populatesMemory() {
        String userId = "u1";

        manager.recordUserMessage(userId, "First message");
        manager.recordUserMessage(userId, "Second message");
        manager.recordBrainOutput(userId, "BrainA", "Analysis 1");
        manager.recordIntent(userId, "ask_question", 0.9);
        manager.recordTone(userId, "neutral", 0.5);

        List<WorkingMemoryState.UserMessage> messages = manager.getLastUserMessages(userId);
        List<WorkingMemoryState.BrainOutput> outputs = manager.getLastBrainOutputs(userId);
        List<WorkingMemoryState.ConversationIntent> intents = manager.getIntentHistory(userId);
        List<WorkingMemoryState.EmotionalTone> tones = manager.getToneHistory(userId);

        assertThat(messages).hasSize(2);
        assertThat(outputs).hasSize(1);
        assertThat(intents).hasSize(1);
        assertThat(tones).hasSize(1);

        String summary = manager.getWorkingMemorySummary(userId);
        assertThat(summary).isNotEmpty();

        String mostRecentIntent = manager.getMostRecentIntent(userId);
        String dominantTone = manager.getDominantTone(userId);
        assertThat(mostRecentIntent).isEqualTo("ask_question");
        assertThat(dominantTone).isEqualTo("neutral");
    }

    @Test
    @DisplayName("clearWorkingMemory should remove memory for user")
    void clearWorkingMemory_removesState() {
        String userId = "u2";
        manager.recordUserMessage(userId, "msg");

        assertThat(manager.getLastUserMessages(userId)).isNotEmpty();

        manager.clearWorkingMemory(userId);

        // After clearing, a new WorkingMemoryState will be created on access, so lists should be empty
        assertThat(manager.getLastUserMessages(userId)).isEmpty();
        assertThat(manager.getIntentHistory(userId)).isEmpty();
    }
}
