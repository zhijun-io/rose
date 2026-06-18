package io.zhijun.spring.core.env.refresh;

import java.io.File;
import java.io.IOException;

import io.zhijun.spring.core.io.watch.FileChangedEvent;
import io.zhijun.spring.core.io.watch.FileChangedListener;
import io.zhijun.spring.core.io.watch.FileWatchService;
import io.zhijun.spring.core.io.watch.StandardFileWatchService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Watches file-backed resources and triggers refresh callbacks.
 */
public class ResourcePropertySourceRefreshWatcher implements AutoCloseable {

    private final ResourcePatternResolver resourcePatternResolver;

    private final FileWatchService fileWatchService;

    public ResourcePropertySourceRefreshWatcher() throws IOException {
        this(new PathMatchingResourcePatternResolver(), new StandardFileWatchService());
    }

    ResourcePropertySourceRefreshWatcher(ResourcePatternResolver resourcePatternResolver,
            FileWatchService fileWatchService) {
        this.resourcePatternResolver = resourcePatternResolver;
        this.fileWatchService = fileWatchService;
    }

    public void watch(String resourceValue, final ResourcePropertySourcesRefresher refresher) throws Exception {
        Resource[] resources = resourcePatternResolver.getResources(resourceValue);
        for (Resource resource : resources) {
            if (resource.isFile()) {
                File file = resource.getFile();
                fileWatchService.watch(file, new ResourceFileChangedListener(resourceValue, refresher, file));
            }
        }
        fileWatchService.start();
    }

    @Override
    public void close() throws IOException {
        fileWatchService.close();
    }

    private static class ResourceFileChangedListener implements FileChangedListener {

        private final String resourceValue;

        private final ResourcePropertySourcesRefresher refresher;

        private final File watchedFile;

        ResourceFileChangedListener(String resourceValue, ResourcePropertySourcesRefresher refresher, File watchedFile) {
            this.resourceValue = resourceValue;
            this.refresher = refresher;
            this.watchedFile = watchedFile;
        }

        @Override
        public void onFileChanged(FileChangedEvent event) {
            if (!watchedFile.equals(event.getFile())) {
                return;
            }
            try {
                refresher.refresh(resourceValue, new FileSystemResource(watchedFile));
            } catch (Throwable ignored) {
                // Keep the watcher resilient; refresh failures should not kill the watch loop.
            }
        }
    }
}
