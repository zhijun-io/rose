package io.zhijun.spring.core.propertysource.watch;

import java.io.File;
import java.io.IOException;

import io.zhijun.spring.core.io.watch.FileChangedEvent;
import io.zhijun.spring.core.io.watch.FileChangedListener;
import io.zhijun.spring.core.io.watch.FileWatchService;
import io.zhijun.spring.core.io.watch.StandardFileWatchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Watches file-backed resources and triggers reload callbacks.
 */
public class AutoRefreshWatcher implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(AutoRefreshWatcher.class);

    private final ResourcePatternResolver resourcePatternResolver;

    private final FileWatchService fileWatchService;

    public AutoRefreshWatcher() throws IOException {
        this(new PathMatchingResourcePatternResolver(), new StandardFileWatchService());
    }

    AutoRefreshWatcher(ResourcePatternResolver resourcePatternResolver, FileWatchService fileWatchService) {
        this.resourcePatternResolver = resourcePatternResolver;
        this.fileWatchService = fileWatchService;
    }

    public void watch(String resourceValue, PropertySourceReloadCallback callback) throws Exception {
        Resource[] resources = resourcePatternResolver.getResources(resourceValue);
        for (Resource resource : resources) {
            if (resource.isFile()) {
                File file = resource.getFile();
                fileWatchService.watch(file, new ResourceFileChangedListener(resourceValue, callback, file));
            }
        }
    }

    public void start() {
        fileWatchService.start();
    }

    @Override
    public void close() throws IOException {
        fileWatchService.close();
    }

    private static class ResourceFileChangedListener implements FileChangedListener {

        private final String resourceValue;

        private final PropertySourceReloadCallback callback;

        private final File watchedFile;

        ResourceFileChangedListener(String resourceValue, PropertySourceReloadCallback callback, File watchedFile) {
            this.resourceValue = resourceValue;
            this.callback = callback;
            this.watchedFile = watchedFile;
        }

        @Override
        public void onFileChanged(FileChangedEvent event) {
            if (!watchedFile.equals(event.getFile())) {
                return;
            }
            try {
                callback.onReload(resourceValue, new FileSystemResource(watchedFile));
            }
            catch (Throwable ex) {
                logger.warn("Failed to reload property source for {}", resourceValue, ex);
            }
        }
    }
}
