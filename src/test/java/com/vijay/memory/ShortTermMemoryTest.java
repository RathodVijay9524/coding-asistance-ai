package com.vijay.memory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Deque;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShortTermMemoryTest {

    private ShortTermMemory shortTermMemory;

    @BeforeEach
    void setUp() {
        shortTermMemory = new ShortTermMemory();
    }

    @Test
    @DisplayName("addMessage should cap memory at 10 most recent messages")
    void addMessage_capsAtMaxMessages() {
        for (int i = 0; i < 15; i++) {
            shortTermMemory.addMessage("user", "user", "msg-" + i);
        }

        assertThat(shortTermMemory.size()).isEqualTo(10);

        List<ShortTermMemory.MemoryMessage> lastMessages = shortTermMemory.getLastMessages(10);
        assertThat(lastMessages).hasSize(10);
        assertThat(lastMessages.get(0).content).isEqualTo("msg-5");
        assertThat(lastMessages.get(9).content).isEqualTo("msg-14");
    }

    @Test
    @DisplayName("getLastMessages should return most recent messages in chronological order")
    void getLastMessages_returnsMostRecentInOrder() {
        shortTermMemory.addMessage("user", "user", "first");
        shortTermMemory.addMessage("user", "assistant", "second");
        shortTermMemory.addMessage("user", "user", "third");

        List<ShortTermMemory.MemoryMessage> lastTwo = shortTermMemory.getLastMessages(2);

        assertThat(lastTwo).hasSize(2);
        assertThat(lastTwo.get(0).content).isEqualTo("second");
        assertThat(lastTwo.get(1).content).isEqualTo("third");
    }

    @Test
    @DisplayName("getLastMessages should skip expired messages based on timestamp")
    void getLastMessages_skipsExpiredMessages() throws Exception {
        // Access internal messages deque via reflection so we can control timestamps
        Field messagesField = ShortTermMemory.class.getDeclaredField("messages");
        messagesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Deque<ShortTermMemory.MemoryMessage> messages =
                (Deque<ShortTermMemory.MemoryMessage>) messagesField.get(shortTermMemory);

        messages.clear();

        ShortTermMemory.MemoryMessage expired =
                new ShortTermMemory.MemoryMessage("user", "user", "old");
        ShortTermMemory.MemoryMessage recent =
                new ShortTermMemory.MemoryMessage("user", "assistant", "new");

        // Make the first message older than expiry threshold
        Field expiryField = ShortTermMemory.class.getDeclaredField("EXPIRY_TIME_MS");
        expiryField.setAccessible(true);
        long expiryMs = (long) expiryField.get(null);
        expired.timestamp = System.currentTimeMillis() - expiryMs - 1_000L;

        messages.addLast(expired);
        messages.addLast(recent);

        List<ShortTermMemory.MemoryMessage> result = shortTermMemory.getLastMessages(5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).content).isEqualTo("new");
    }

    @Test
    @DisplayName("getContextForLLM should return empty string when there is no context")
    void getContextForLLM_emptyMemory_returnsEmptyString() {
        String context = shortTermMemory.getContextForLLM();
        assertThat(context).isEmpty();
    }

    @Test
    @DisplayName("getContextForLLM should format recent messages for LLM")
    void getContextForLLM_withMessages_formatsContext() {
        shortTermMemory.addMessage("user", "user", "hello");
        shortTermMemory.addMessage("user", "assistant", "hi there");

        String context = shortTermMemory.getContextForLLM();

        assertThat(context).contains("Recent conversation context:");
        assertThat(context).contains("user: hello");
        assertThat(context).contains("assistant: hi there");
    }

    @Test
    @DisplayName("clear should remove all stored messages")
    void clear_removesAllMessages() {
        shortTermMemory.addMessage("user", "user", "one");
        shortTermMemory.addMessage("user", "assistant", "two");
        assertThat(shortTermMemory.size()).isGreaterThan(0);

        shortTermMemory.clear();

        assertThat(shortTermMemory.size()).isEqualTo(0);
        assertThat(shortTermMemory.getLastMessages(5)).isEmpty();
    }
}
