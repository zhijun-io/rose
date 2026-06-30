package io.zhijun.spring.propertysource;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

class AutoRefreshWatcherLifecycleTests {

    @Test
    void shouldCloseRegisteredWatchersOnContextClosedEvent() throws IOException {
        RecordingWatcher watcher = new RecordingWatcher();
        AutoRefreshWatcherLifecycle.register(watcher);

        AutoRefreshWatcherLifecycle lifecycle = new AutoRefreshWatcherLifecycle();
        lifecycle.onApplicationEvent(new ContextClosedEvent(new AnnotationConfigApplicationContext()));

        assertThat(watcher.closed.get()).isTrue();
    }

    private static class RecordingWatcher implements AutoCloseable {

        private final AtomicBoolean closed = new AtomicBoolean(false);

        @Override
        public void close() {
            closed.set(true);
        }
    }
}
