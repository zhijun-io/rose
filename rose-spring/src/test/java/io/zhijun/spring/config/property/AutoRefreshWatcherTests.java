package io.zhijun.spring.config.property;

import io.zhijun.core.watch.WatchService;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AutoRefreshWatcherTests {

    @Test
    void shouldWatchFileResources() throws Exception {
        ResourcePatternResolver resolver = mock(ResourcePatternResolver.class);
        WatchService watchService = mock(WatchService.class);
        Resource resource = mock(Resource.class);

        when(resolver.getResources("file:/tmp/test.txt")).thenReturn(new Resource[]{resource});
        when(resource.isFile()).thenReturn(true);
        when(resource.getFile()).thenReturn(new java.io.File("/tmp/test.txt"));

        AutoRefreshWatcher watcher = new AutoRefreshWatcher(resolver, watchService);
        BiConsumer<String, Resource> callback = mock(BiConsumer.class);
        watcher.watch("file:/tmp/test.txt", callback);

        verify(watchService).watch(any(java.io.File.class), any());
    }

    @Test
    void shouldSkipNonFileResources() throws Exception {
        ResourcePatternResolver resolver = mock(ResourcePatternResolver.class);
        WatchService watchService = mock(WatchService.class);
        Resource resource = mock(Resource.class);

        when(resolver.getResources("classpath:/test.txt")).thenReturn(new Resource[]{resource});
        when(resource.isFile()).thenReturn(false);

        AutoRefreshWatcher watcher = new AutoRefreshWatcher(resolver, watchService);
        watcher.watch("classpath:/test.txt", mock(BiConsumer.class));

        verify(watchService, never()).watch(any(), any());
    }

    @Test
    void shouldStartWatchService() {
        WatchService watchService = mock(WatchService.class);
        AutoRefreshWatcher watcher = new AutoRefreshWatcher(mock(ResourcePatternResolver.class), watchService);
        watcher.start();
        verify(watchService).start();
    }

    @Test
    void shouldCloseWatchService() throws Exception {
        WatchService watchService = mock(WatchService.class);
        AutoRefreshWatcher watcher = new AutoRefreshWatcher(mock(ResourcePatternResolver.class), watchService);
        watcher.close();
        verify(watchService).close();
    }
}
