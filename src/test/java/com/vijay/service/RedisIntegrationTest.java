package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RedisIntegrationTest {

    private LocalCacheImpl localCache;
    private RedisIntegration redis;

    @BeforeEach
    void setUp() {
        localCache = mock(LocalCacheImpl.class);
        redis = new RedisIntegration(localCache);
    }

    @Test
    @DisplayName("connectToRedis should mark Redis as available when host and port valid")
    void connectToRedis_marksAvailable() {
        redis.connectToRedis("localhost", 6379);
        assertThat(redis.isRedisAvailable()).isTrue();

        RedisIntegration.RedisStats stats = redis.getRedisStats();
        assertThat(stats.available).isTrue();
        assertThat(stats.host).isEqualTo("localhost");
        assertThat(stats.port).isEqualTo(6379);
    }

    @Test
    @DisplayName("putInRedis and getFromRedis should delegate to LocalCacheImpl when Redis not available")
    void putAndGet_fallbackToLocalCache() {
        assertThat(redis.isRedisAvailable()).isFalse();

        redis.putInRedis("k1", "v1", 10);
        verify(localCache).put("k1", "v1", 10);

        when(localCache.get("k1")).thenReturn("v1");
        Object value = redis.getFromRedis("k1");
        assertThat(value).isEqualTo("v1");
    }

    @Test
    @DisplayName("getRedisStats should reflect local cache size and availability")
    void getRedisStats_reflectsState() {
        when(localCache.size()).thenReturn(5);

        RedisIntegration.RedisStats statsBefore = redis.getRedisStats();
        assertThat(statsBefore.available).isFalse();
        assertThat(statsBefore.localCacheSize).isEqualTo(5);

        redis.connectToRedis("localhost", 6379);
        RedisIntegration.RedisStats statsAfter = redis.getRedisStats();

        assertThat(statsAfter.available).isTrue();
        assertThat(statsAfter.localCacheSize).isEqualTo(5);
    }

    @Test
    @DisplayName("disconnect should set Redis as unavailable")
    void disconnect_setsUnavailable() {
        redis.connectToRedis("localhost", 6379);
        assertThat(redis.isRedisAvailable()).isTrue();

        redis.disconnect();
        assertThat(redis.isRedisAvailable()).isFalse();
    }
}
