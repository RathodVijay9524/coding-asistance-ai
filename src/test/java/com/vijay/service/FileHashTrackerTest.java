package com.vijay.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FileHashTrackerTest {

    @Test
    @DisplayName("calculateFileHash should return null for missing file")
    void calculateFileHash_missingFile() {
        FileHashTracker tracker = new FileHashTracker();
        String hash = tracker.calculateFileHash("non_existent_file_123456.txt");
        assertThat(hash).isNull();
    }

    @Test
    @DisplayName("trackFileHash and hasFileChanged should detect changes and maintain history")
    void trackAndDetectChanges() throws IOException, InterruptedException {
        FileHashTracker tracker = new FileHashTracker();

        Path tempFile = Files.createTempFile("hash-test", ".txt");
        tempFile.toFile().deleteOnExit();

        Files.writeString(tempFile, "first content");
        String path = tempFile.toString();

        tracker.trackFileHash(path);
        String firstHash = tracker.getFileHash(path);
        assertThat(firstHash).isNotNull();
        assertThat(tracker.isNewFile(path)).isFalse();

        // Modify file
        Files.writeString(tempFile, "second content");
        boolean changed = tracker.hasFileChanged(path);
        String secondHash = tracker.getFileHash(path);

        assertThat(changed).isTrue();
        assertThat(secondHash).isNotEqualTo(firstHash);

        String previous = tracker.getPreviousFileHash(path);
        assertThat(previous).isEqualTo(firstHash);

        List<FileHashTracker.HashHistoryEntry> history = tracker.getHashHistory(path);
        assertThat(history).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("getChangedFiles and getNewFiles should classify correctly")
    void changedAndNewFiles() throws IOException {
        FileHashTracker tracker = new FileHashTracker();

        Path f1 = Files.createTempFile("hash-test-new", ".txt");
        Path f2 = Files.createTempFile("hash-test-change", ".txt");
        f1.toFile().deleteOnExit();
        f2.toFile().deleteOnExit();

        Files.writeString(f1, "new file");
        Files.writeString(f2, "initial");

        String p1 = f1.toString();
        String p2 = f2.toString();

        // Initially both are new
        List<String> newFiles = tracker.getNewFiles(Arrays.asList(p1, p2));
        assertThat(newFiles).contains(p1, p2);

        // Change f2, but not tracked as new now
        Files.writeString(f2, "changed");
        List<String> changedFiles = tracker.getChangedFiles(Arrays.asList(p1, p2));

        assertThat(changedFiles).contains(p2);

        FileHashTracker.TrackerStatistics stats = tracker.getStatistics();
        assertThat(stats.trackedFiles).isGreaterThanOrEqualTo(2);
        assertThat(stats.filesWithHistory).isGreaterThanOrEqualTo(2);
        assertThat(stats.totalHistoryEntries).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("clearHashCache should reset tracked files and history")
    void clearHashCache_resets() throws IOException {
        FileHashTracker tracker = new FileHashTracker();

        Path f = Files.createTempFile("hash-test-clear", ".txt");
        f.toFile().deleteOnExit();
        Files.writeString(f, "content");

        tracker.trackFileHash(f.toString());
        assertThat(tracker.getAllTrackedFiles()).isNotEmpty();

        tracker.clearHashCache();

        assertThat(tracker.getAllTrackedFiles()).isEmpty();
        assertThat(tracker.getHashHistory(f.toString())).isEmpty();
    }
}
