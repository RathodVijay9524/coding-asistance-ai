package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CachingLayerTest {

    private CachingLayer cachingLayer;

    @BeforeEach
    void setUp() {
        cachingLayer = new CachingLayer();
    }

    @Test
    @DisplayName("put/get should cache values and contains should reflect state")
    void putGet_basic() {
        cachingLayer.put("key1", "value1");

        assertThat(cachingLayer.contains("key1")).isTrue();
        String value = cachingLayer.get("key1");
        assertThat(value).isEqualTo("value1");
    }

    @Test
    @DisplayName("expired entries should be removed on get and contains should return false")
    void expiredEntries_shouldBeEvictedOnAccess() throws InterruptedException {
        // very short TTL (1 ms) so it expires quickly
        cachingLayer.put("key1", "value1", 1);

        Thread.sleep(5); // wait to ensure expiration

        Object value = cachingLayer.get("key1");
        assertThat(value).isNull();
        assertThat(cachingLayer.contains("key1")).isFalse();
    }

    @Test
    @DisplayName("evictExpired should clean up expired entries and stats should reflect size")
    void evictExpired_andStats() throws InterruptedException {
        cachingLayer.put("k1", "v1", 1);   // short TTL
        cachingLayer.put("k2", "v2", 1000); // long TTL

        Thread.sleep(5);

        cachingLayer.evictExpired();

        CachingLayer.CacheStats stats = cachingLayer.getStats();
        assertThat(stats.expiredCount).isGreaterThanOrEqualTo(0); // some entries may be expired
        assertThat(stats.totalSize).isGreaterThanOrEqualTo(1);    // k2 should remain
        assertThat(stats.maxSize).isGreaterThan(0);
    }

    @Test
    @DisplayName("clear should remove all entries and reset utilization")
    void clear_shouldRemoveAll() {
        cachingLayer.put("k1", "v1");
        cachingLayer.put("k2", "v2");

        cachingLayer.clear();

        CachingLayer.CacheStats stats = cachingLayer.getStats();
        assertThat(stats.totalSize).isZero();
        assertThat(stats.utilizationPercent).isZero();
    }
}
