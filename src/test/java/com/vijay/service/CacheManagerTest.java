package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CacheManagerTest {

    private LocalCacheImpl localCache;
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        localCache = mock(LocalCacheImpl.class);
        cacheManager = new CacheManager(localCache);
    }

    @Test
    @DisplayName("put and get should delegate to LocalCacheImpl and support advisor/model/user helpers")
    void putAndGet_delegatesAndHelpers() {
        cacheManager.put("key", "value", 10);
        verify(localCache).put("key", "value", 10);

        when(localCache.get("key")).thenReturn("value");
        Object value = cacheManager.get("key");
        assertThat(value).isEqualTo("value");

        cacheManager.cacheAdvisorOutput("Adv", "hash1", "out1");
        verify(localCache).put(eq("advisor:Adv:hash1"), eq("out1"), anyLong());

        when(localCache.get("advisor:Adv:hash1")).thenReturn("out1");
        assertThat(cacheManager.getCachedAdvisorOutput("Adv", "hash1")).isEqualTo("out1");

        cacheManager.cacheModelResponse("Model", "phash", "resp");
        verify(localCache).put(eq("model:Model:phash"), eq("resp"), anyLong());

        when(localCache.get("model:Model:phash")).thenReturn("resp");
        assertThat(cacheManager.getCachedModelResponse("Model", "phash")).isEqualTo("resp");

        cacheManager.cacheUserPreferences("u1", "prefs");
        verify(localCache).put(eq("user:u1:preferences"), eq("prefs"), anyLong());

        when(localCache.get("user:u1:preferences")).thenReturn("prefs");
        assertThat(cacheManager.getCachedUserPreferences("u1")).isEqualTo("prefs");
    }

    @Test
    @DisplayName("remove, clear and getStats should delegate to LocalCacheImpl")
    void removeClearAndStats_delegate() {
        CacheManager.CacheStats stats = new CacheManager.CacheStats(1, 10, 5, 2);
        when(localCache.getStats()).thenReturn(stats);

        cacheManager.remove("key");
        verify(localCache).remove("key");

        cacheManager.clear();
        verify(localCache).clear();

        CacheManager.CacheStats returned = cacheManager.getStats();
        assertThat(returned).isSameAs(stats);
        assertThat(returned.getHitRate()).isGreaterThanOrEqualTo(0.0);
    }
}
