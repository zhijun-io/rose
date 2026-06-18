package io.zhijun.spring.core.env.refresh;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;

class ResourcePropertySourceRefreshWatcherTests {

    @Test
    void shouldTriggerRefreshCallbackForFileBackedResource() throws Exception {
        File file = File.createTempFile("app", ".properties");
        List<String> callbacks = new ArrayList<String>();
        io.zhijun.spring.core.io.watch.FakeFileWatchService watchService = new io.zhijun.spring.core.io.watch.FakeFileWatchService();

        ResourcePropertySourceRefreshWatcher watcher = new ResourcePropertySourceRefreshWatcher(
                new org.springframework.core.io.support.PathMatchingResourcePatternResolver(), watchService);
        try {
            watcher.watch("file:" + file.getAbsolutePath(), new ResourcePropertySourcesRefresher() {
                @Override
                public void refresh(String resourceValue, Resource resource) {
                    callbacks.add(resourceValue);
                    callbacks.add(resource.getFilename());
                }
            });
            watchService.publish(file, io.zhijun.spring.core.io.watch.FileChangedEvent.Kind.MODIFIED);
        } finally {
            watcher.close();
        }

        assertThat(callbacks).contains("file:" + file.getAbsolutePath(), file.getName());
    }
}
