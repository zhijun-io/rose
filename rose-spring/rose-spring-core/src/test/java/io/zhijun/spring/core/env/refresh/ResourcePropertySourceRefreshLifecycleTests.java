package io.zhijun.spring.core.env.refresh;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

import static org.assertj.core.api.Assertions.assertThat;

class ResourcePropertySourceRefreshLifecycleTests {

    @Test
    void shouldCloseRegisteredWatchersOnContextClosedEvent() throws IOException {
        RecordingWatcher watcher = new RecordingWatcher();
        ResourcePropertySourceRefreshLifecycle.register(watcher);

        ResourcePropertySourceRefreshLifecycle lifecycle = new ResourcePropertySourceRefreshLifecycle();
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
