package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EmbeddingCacheManagerTest {

    private EmbeddingCacheManager manager;

    @BeforeEach
    void setUp() {
        manager = new EmbeddingCacheManager();
    }

    private void setCachePathAndEnabled(Path dir, boolean enabled) throws Exception {
        Field cachePathField = EmbeddingCacheManager.class.getDeclaredField("cachePath");
        cachePathField.setAccessible(true);
        cachePathField.set(manager, dir.toString());

        Field cacheEnabledField = EmbeddingCacheManager.class.getDeclaredField("cacheEnabled");
        cacheEnabledField.setAccessible(true);
        cacheEnabledField.setBoolean(manager, enabled);
    }

    @Test
    @DisplayName("cacheFileExists should return true only when both embeddings and hash files exist")
    void cacheFileExists_basic() throws Exception {
        Path dir = Files.createTempDirectory("emb-cache-exists");
        setCachePathAndEnabled(dir, true);

        assertThat(manager.cacheFileExists()).isFalse();

        Files.writeString(dir.resolve("embeddings.json"), "{}");
        Files.writeString(dir.resolve("documents.hash"), "hash");

        assertThat(manager.cacheFileExists()).isTrue();
    }

    @Test
    @DisplayName("isCacheValid should compare documents hash when cache enabled")
    void isCacheValid_hashComparison() throws Exception {
        Path dir = Files.createTempDirectory("emb-cache-valid");
        setCachePathAndEnabled(dir, true);

        Files.writeString(dir.resolve("documents.hash"), "abc123");

        assertThat(manager.isCacheValid("abc123")).isTrue();
        assertThat(manager.isCacheValid("different"));
    }

    @Test
    @DisplayName("saveToCache should create cache directory and files")
    void saveToCache_createsFiles() throws Exception {
        Path dir = Files.createTempDirectory("emb-cache-save");
        setCachePathAndEnabled(dir, true);

        manager.saveToCache(null, "hash123");

        assertThat(Files.exists(dir.resolve("embeddings.json"))).isTrue();
        assertThat(Files.exists(dir.resolve("documents.hash"))).isTrue();

        String storedHash = Files.readString(dir.resolve("documents.hash"));
        assertThat(storedHash).isEqualTo("hash123");
    }

    @Test
    @DisplayName("calculateDocumentsHash should be deterministic regardless of file order")
    void calculateDocumentsHash_deterministic() throws Exception {
        Path dir = Files.createTempDirectory("emb-cache-hash");
        setCachePathAndEnabled(dir, true);

        Path fileA = dir.resolve("a.txt");
        Path fileB = dir.resolve("b.txt");
        Files.writeString(fileA, "hello");
        Files.writeString(fileB, "world");

        String hash1 = manager.calculateDocumentsHash(List.of(fileA.toString(), fileB.toString()));
        String hash2 = manager.calculateDocumentsHash(List.of(fileB.toString(), fileA.toString()));

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    @DisplayName("clearCache should delete cache directory contents")
    void clearCache_deletesFiles() throws Exception {
        Path dir = Files.createTempDirectory("emb-cache-clear");
        setCachePathAndEnabled(dir, true);

        Files.writeString(dir.resolve("embeddings.json"), "{}");
        Files.writeString(dir.resolve("documents.hash"), "hash");

        manager.clearCache();

        assertThat(Files.exists(dir)).isFalse();
    }

    @Test
    @DisplayName("getCacheStats should report basic cache information")
    void getCacheStats_basic() throws Exception {
        Path dir = Files.createTempDirectory("emb-cache-stats");
        setCachePathAndEnabled(dir, true);

        Files.writeString(dir.resolve("embeddings.json"), "{}");

        Map<String, Object> stats = manager.getCacheStats();

        assertThat(stats.get("cacheEnabled")).isEqualTo(true);
        assertThat(stats.get("cachePath")).isEqualTo(dir.toString());
        assertThat(stats.get("cacheExists")).isEqualTo(true);
        assertThat(stats.get("cacheSize")).isNotNull();
        assertThat(stats.get("cacheSize_MB")).isNotNull();
    }
}
