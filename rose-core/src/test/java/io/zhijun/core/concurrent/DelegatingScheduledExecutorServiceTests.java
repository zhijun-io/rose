package io.zhijun.core.concurrent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class DelegatingScheduledExecutorServiceTests {

    private DelegatingScheduledExecutorService service;

    @AfterEach
    void tearDown() {
        if (service != null) {
            service.shutdownNow();
        }
    }

    @Test
    void shouldRejectNullDelegate() {
        assertThatThrownBy(() -> new DelegatingScheduledExecutorService(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("delegate cannot be null");
    }

    @Test
    void shouldRejectNullDelegateOnSetDelegate() {
        service = new DelegatingScheduledExecutorService(
                java.util.concurrent.Executors.newSingleThreadScheduledExecutor());
        assertThatThrownBy(() -> service.setDelegate(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("delegate cannot be null");
    }

    @Test
    void shouldExecuteRunnable() throws Exception {
        service = new DelegatingScheduledExecutorService(
                java.util.concurrent.Executors.newSingleThreadScheduledExecutor());
        java.util.concurrent.atomic.AtomicBoolean ran = new java.util.concurrent.atomic.AtomicBoolean(false);
        service.execute(() -> ran.set(true));
        Thread.sleep(100);
        assertThat(ran).isTrue();
    }

    @Test
    void shouldSubmitCallable() throws Exception {
        service = new DelegatingScheduledExecutorService(
                java.util.concurrent.Executors.newSingleThreadScheduledExecutor());
        java.util.concurrent.Future<String> future = service.submit(() -> "hello");
        assertThat(future.get()).isEqualTo("hello");
    }

    @Test
    void shouldScheduleRunnable() throws Exception {
        service = new DelegatingScheduledExecutorService(
                java.util.concurrent.Executors.newSingleThreadScheduledExecutor());
        java.util.concurrent.atomic.AtomicBoolean ran = new java.util.concurrent.atomic.AtomicBoolean(false);
        ScheduledFuture<?> future = service.schedule(() -> ran.set(true), 10, TimeUnit.MILLISECONDS);
        future.get(1, TimeUnit.SECONDS);
        assertThat(ran).isTrue();
    }

    @Test
    void shouldScheduleCallable() throws Exception {
        service = new DelegatingScheduledExecutorService(
                java.util.concurrent.Executors.newSingleThreadScheduledExecutor());
        ScheduledFuture<String> future = service.schedule(() -> "delayed", 10, TimeUnit.MILLISECONDS);
        assertThat(future.get(1, TimeUnit.SECONDS)).isEqualTo("delayed");
    }

    @Test
    void shouldInvokeAll() throws InterruptedException, ExecutionException {
        service = new DelegatingScheduledExecutorService(
                java.util.concurrent.Executors.newSingleThreadScheduledExecutor());
        Callable<String> task1 = () -> "one";
        Callable<String> task2 = () -> "two";
        java.util.List<java.util.concurrent.Future<String>> futures =
                service.invokeAll(Arrays.asList(task1, task2));
        assertThat(futures).hasSize(2);
        assertThat(futures.get(0).get()).isEqualTo("one");
        assertThat(futures.get(1).get()).isEqualTo("two");
    }

    @Test
    void shouldSwapDelegateAtRuntime() throws Exception {
        java.util.concurrent.ScheduledExecutorService executor1 =
                java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
        service = new DelegatingScheduledExecutorService(executor1);

        java.util.concurrent.Future<String> f1 = service.submit(() -> "from-executor1");
        assertThat(f1.get()).isEqualTo("from-executor1");

        java.util.concurrent.ScheduledExecutorService executor2 =
                java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
        service.setDelegate(executor2);

        java.util.concurrent.Future<String> f2 = service.submit(() -> "from-executor2");
        assertThat(f2.get()).isEqualTo("from-executor2");

        executor1.shutdownNow();
    }

    @Test
    void shouldReportShutdownState() {
        service = new DelegatingScheduledExecutorService(
                java.util.concurrent.Executors.newSingleThreadScheduledExecutor());
        assertThat(service.isShutdown()).isFalse();
        service.shutdown();
        assertThat(service.isShutdown()).isTrue();
    }

    @Test
    void shouldInvokeAnyWithTimeout() throws InterruptedException, ExecutionException, java.util.concurrent.TimeoutException {
        service = new DelegatingScheduledExecutorService(
                java.util.concurrent.Executors.newSingleThreadScheduledExecutor());
        String result = service.invokeAny(Collections.singletonList(() -> "result"), 1, TimeUnit.SECONDS);
        assertThat(result).isEqualTo("result");
    }
}
