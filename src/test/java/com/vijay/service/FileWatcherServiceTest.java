package com.vijay.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FileWatcherServiceTest {

    @Test
    @DisplayName("newly constructed FileWatcherService should report not running and zero changes")
    void initialState_statistics() {
        FileWatcherService watcher = new FileWatcherService();

        assertThat(watcher.isRunning()).isFalse();

        Map<String, Object> stats = watcher.getStatistics();
        assertThat(stats.get("watching")).isEqualTo(false);
        assertThat((Integer) stats.get("modifiedFiles")).isZero();
        assertThat((Integer) stats.get("newFiles")).isZero();
        assertThat((Integer) stats.get("deletedFiles")).isZero();
        assertThat((Integer) stats.get("totalChanges")).isZero();
    }
}
