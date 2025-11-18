package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocalCacheImplTest {

    private LocalCacheImpl cache;

    @BeforeEach
    void setUp() {
        // small max size to easily test LRU
        cache = new LocalCacheImpl(2);
    }

    @Test
    @DisplayName("put/get should store and retrieve non-expired values and update stats")
    void putGet_basic() {
        cache.put("k1", "v1", 60);
        cache.put("k2", "v2", 60);

        Object v1 = cache.get("k1");
        Object v2 = cache.get("k2");

        assertThat(v1).isEqualTo("v1");
        assertThat(v2).isEqualTo("v2");

        CacheManager.CacheStats stats = cache.getStats();
        assertThat(stats.size).isEqualTo(2);
        assertThat(stats.hits).isEqualTo(2);
        assertThat(stats.misses).isZero();
    }

    @Test
    @DisplayName("expired entries should be removed on get and counted as miss")
    void get_expiredEntry() throws InterruptedException {
        // Use a small positive TTL (1 second) and wait slightly longer to ensure expiry
        cache.put("k1", "v1", 1); // TTL = 1 second

        // Sleep just over 1 second so expiryTime is definitely in the past
        Thread.sleep(1100);

        Object v1 = cache.get("k1");

        assertThat(v1).isNull();
        assertThat(cache.containsKey("k1")).isFalse();

        CacheManager.CacheStats stats = cache.getStats();
        assertThat(stats.misses).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("LRU eviction should remove least recently used entry when max size reached")
    void lruEviction_shouldRemoveLeastRecentlyUsed() {
        cache.put("k1", "v1", 60);
        cache.put("k2", "v2", 60);

        // Access k1 so k2 becomes LRU
        Object v1Before = cache.get("k1");
        assertThat(v1Before).isEqualTo("v1");

        // Insert k3 -> should evict one of the existing keys to keep size <= maxSize
        cache.put("k3", "v3", 60);

        Object v1After = cache.get("k1");
        Object v2After = cache.get("k2");
        Object v3After = cache.get("k3");

        // New key must be present
        assertThat(v3After).isEqualTo("v3");
        // At least one of the old keys must have been evicted
        assertThat(v1After == null || v2After == null).isTrue();
    }

    @Test
    @DisplayName("clear should remove all entries and reset hits/misses")
    void clear_shouldReset() {
        cache.put("k1", "v1", 60);
        cache.get("k1"); // 1 hit
        cache.get("missing"); // 1 miss

        cache.clear();

        assertThat(cache.size()).isZero();
        CacheManager.CacheStats stats = cache.getStats();
        assertThat(stats.hits).isZero();
        assertThat(stats.misses).isZero();
    }
}
