package io.zhijun.spring.core.propertysource.watch;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

class AutoRefreshWatcherTest {

    @Test
    void shouldTriggerReloadCallbackForFileBackedResource() throws Exception {
        File file = File.createTempFile("app", ".properties");
        List<String> callbacks = new ArrayList<String>();
        io.zhijun.spring.core.io.watch.FakeFileWatchService watchService =
                new io.zhijun.spring.core.io.watch.FakeFileWatchService();

        AutoRefreshWatcher watcher = new AutoRefreshWatcher(
                new org.springframework.core.io.support.PathMatchingResourcePatternResolver(), watchService);
        try {
            watcher.watch("file:" + file.getAbsolutePath(), new PropertySourceReloadCallback() {
                @Override
                public void onReload(String resourceValue, Resource resource) {
                    callbacks.add(resourceValue);
                    callbacks.add(resource.getFilename());
                }
            });
            watcher.start();
            watchService.publish(file, io.zhijun.spring.core.io.watch.FileChangedEvent.Kind.MODIFIED);
        } finally {
            watcher.close();
        }

        assertThat(callbacks).contains("file:" + file.getAbsolutePath(), file.getName());
    }
}
