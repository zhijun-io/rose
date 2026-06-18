package io.zhijun.spring.core.env.refresh;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

/**
 * Closes auto-refresh watchers when the application context shuts down.
 */
public class ResourcePropertySourceRefreshLifecycle implements ApplicationListener<ContextClosedEvent> {

    private static final List<AutoCloseable> WATCHERS = new CopyOnWriteArrayList<AutoCloseable>();

    public static void register(AutoCloseable watcher) {
        WATCHERS.add(watcher);
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        for (AutoCloseable watcher : WATCHERS) {
            try {
                watcher.close();
            } catch (Exception ignored) {
                // Best-effort cleanup during shutdown.
            }
        }
        WATCHERS.clear();
    }
}
