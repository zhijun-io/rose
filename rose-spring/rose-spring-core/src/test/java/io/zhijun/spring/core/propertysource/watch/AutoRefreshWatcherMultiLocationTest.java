package io.zhijun.spring.core.propertysource.watch;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import io.zhijun.spring.core.io.watch.FakeFileWatchService;
import io.zhijun.spring.core.io.watch.FileChangedEvent;

class AutoRefreshWatcherMultiLocationTest {

    @Test
    void watchesAllRegisteredLocations() throws Exception {
        File first = File.createTempFile("first", ".properties");
        File second = File.createTempFile("second", ".properties");
        FakeFileWatchService watchService = new FakeFileWatchService();
        AtomicInteger reloadCount = new AtomicInteger();

        AutoRefreshWatcher watcher = new AutoRefreshWatcher(
                new org.springframework.core.io.support.PathMatchingResourcePatternResolver(), watchService);
        PropertySourceReloadCallback callback = new PropertySourceReloadCallback() {
            @Override
            public void onReload(String resourceValue, Resource resource) {
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
