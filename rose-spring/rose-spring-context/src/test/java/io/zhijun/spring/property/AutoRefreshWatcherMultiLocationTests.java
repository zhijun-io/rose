package io.zhijun.spring.propertysource;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import io.zhijun.core.watch.FileChangedEvent;

class AutoRefreshWatcherMultiLocationTests {

    @Test
    void watchesAllRegisteredLocations() throws Exception {
        File first = File.createTempFile("first", ".properties");
        File second = File.createTempFile("second", ".properties");
        FakeFileWatchService watchService = new FakeFileWatchService();
        AtomicInteger reloadCount = new AtomicInteger();

        AutoRefreshWatcher watcher = new AutoRefreshWatcher(
                new org.springframework.core.io.support.PathMatchingResourcePatternResolver(), watchService);
        java.util.function.BiConsumer<String, Resource> callback = new java.util.function.BiConsumer<String, Resource>() {
            @Override
            public void accept(String resourceValue, Resource resource) {
                reloadCount.incrementAndGet();
            }
        };

        watcher.watch("file:" + first.getAbsolutePath(), callback);
        watcher.watch("file:" + second.getAbsolutePath(), callback);
        watcher.start();

        watchService.publish(first, FileChangedEvent.Kind.MODIFIED);
        watchService.publish(second, FileChangedEvent.Kind.MODIFIED);

        assertThat(reloadCount).hasValue(2);
    }
}
