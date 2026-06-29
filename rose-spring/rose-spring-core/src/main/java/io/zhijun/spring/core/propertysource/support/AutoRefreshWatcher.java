package io.zhijun.spring.core.propertysource.support;

import java.io.File;
import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import io.zhijun.spring.core.io.watch.FileWatchService;
import io.zhijun.spring.core.io.watch.StandardFileWatchService;
import io.zhijun.spring.core.propertysource.support.internal.ResourceFileChangedListener;

/**
 * Watches file-backed resources and triggers reload callbacks.
 */
public class AutoRefreshWatcher implements AutoCloseable {

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
}
