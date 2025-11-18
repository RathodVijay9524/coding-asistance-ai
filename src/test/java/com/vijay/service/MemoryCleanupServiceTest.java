package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class MemoryCleanupServiceTest {

    private ConversationMemoryManager memoryManager;
    private MemoryCleanupService cleanupService;

    @BeforeEach
    void setUp() {
        memoryManager = Mockito.mock(ConversationMemoryManager.class);
        cleanupService = new MemoryCleanupService(memoryManager);
    }

    @Test
    @DisplayName("cleanupOldSessions should delegate to ConversationMemoryManager")
    void cleanupOldSessions_delegates() {
        assertThatCode(() -> cleanupService.cleanupOldSessions()).doesNotThrowAnyException();
        verify(memoryManager, times(1)).cleanupOldSessions();
    }

    @Test
    @DisplayName("logMemoryStatistics should execute without throwing")
    void logMemoryStatistics_safe() {
        assertThatCode(() -> cleanupService.logMemoryStatistics()).doesNotThrowAnyException();
    }
}
