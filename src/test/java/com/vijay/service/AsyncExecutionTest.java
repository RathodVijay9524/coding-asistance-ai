package com.vijay.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncExecutionTest {

    private AsyncExecution asyncExecution;

    @BeforeEach
    void setUp() {
        asyncExecution = new AsyncExecution();
    }

    @AfterEach
    void tearDown() {
        asyncExecution.shutdown();
    }

    @Test
    @DisplayName("analyzeCodeAsync should complete and return language-specific result")
    void analyzeCodeAsync_basic() {
        CompletableFuture<String> future = asyncExecution.analyzeCodeAsync("code", "Java");

        String result = asyncExecution.waitForResult(future, 1000);

        assertThat(result).isEqualTo("Analysis result for: Java");
    }

    @Test
    @DisplayName("executeToolsParallel should run all tools and return their results")
    void executeToolsParallel_basic() {
        List<String> tools = List.of("tool1", "tool2");

        CompletableFuture<List<String>> future = asyncExecution.executeToolsParallel(tools);
        List<String> results = asyncExecution.waitForResult(future, 1000);

        assertThat(results).containsExactlyInAnyOrder("Result from tool1", "Result from tool2");
    }

    @Test
    @DisplayName("indexInBackground should complete without error")
    void indexInBackground_completes() {
        CompletableFuture<Void> future = asyncExecution.indexInBackground("data", "index1");

        Void result = asyncExecution.waitForResult(future, 1000);

        assertThat(future).isCompleted();
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("scheduleTask should execute the task after delay")
    void scheduleTask_executes() throws Exception {
        AtomicBoolean executed = new AtomicBoolean(false);

        ScheduledFuture<?> future = asyncExecution.scheduleTask(() -> executed.set(true), 50);

        future.get(500, TimeUnit.MILLISECONDS);

        assertThat(executed.get()).isTrue();
    }

    @Test
    @DisplayName("scheduleRecurring should execute task multiple times")
    void scheduleRecurring_executesMultipleTimes() throws Exception {
        CountDownLatch latch = new CountDownLatch(3);

        ScheduledFuture<?> future = asyncExecution.scheduleRecurring(latch::countDown, 0, 50);

        boolean completed = latch.await(1000, TimeUnit.MILLISECONDS);
        future.cancel(true);

        assertThat(completed).isTrue();
    }

    @Test
    @DisplayName("waitForResult should return null and cancel on timeout")
    void waitForResult_timeoutReturnsNull() {
        CompletableFuture<String> future = new CompletableFuture<>();

        String result = asyncExecution.waitForResult(future, 50);

        assertThat(result).isNull();
        assertThat(future).isCancelled();
    }

    @Test
    @DisplayName("combineAsync should join all futures and return their results")
    void combineAsync_basic() {
        CompletableFuture<String> f1 = CompletableFuture.completedFuture("A");
        CompletableFuture<String> f2 = CompletableFuture.completedFuture("B");

        CompletableFuture<List<String>> combined = asyncExecution.combineAsync(List.of(f1, f2));
        List<String> results = asyncExecution.waitForResult(combined, 500);

        assertThat(results).containsExactlyInAnyOrder("A", "B");
    }

    @Test
    @DisplayName("getStats should return non-negative executor statistics")
    void getStats_basic() {
        AsyncExecution.ExecutorStats stats = asyncExecution.getStats();

        assertThat(stats.activeThreads).isGreaterThanOrEqualTo(0);
        assertThat(stats.poolSize).isGreaterThanOrEqualTo(0);
        assertThat(stats.corePoolSize).isGreaterThanOrEqualTo(0);
        assertThat(stats.maxPoolSize).isGreaterThanOrEqualTo(0);
        assertThat(stats.tasksCompleted).isGreaterThanOrEqualTo(0L);
        assertThat(stats.tasksPending).isGreaterThanOrEqualTo(0L);
    }
}
