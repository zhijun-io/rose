package io.zhijun.spring.config.property;

import io.zhijun.core.spi.SpiLoader;
import io.zhijun.core.watch.WatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Watches file-backed resources and triggers reload callbacks.
 */
public class AutoRefreshWatcher implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(AutoRefreshWatcher.class);

    private final ResourcePatternResolver resourcePatternResolver;

    private final WatchService fileWatchService;

    public AutoRefreshWatcher() throws IOException {
        this(new PathMatchingResourcePatternResolver(), createWatchService());
    }

    private static WatchService createWatchService() {
        Optional<WatchService> spi = SpiLoader.defaults().loadFirst(WatchService.class);
        if (spi.isPresent()) {
            return spi.get();
        }
        return WatchService.defaults();
    }

    AutoRefreshWatcher(ResourcePatternResolver resourcePatternResolver, WatchService fileWatchService) {
        this.resourcePatternResolver = resourcePatternResolver;
        this.fileWatchService = fileWatchService;
    }

    public void watch(String resourceValue, BiConsumer<String, Resource> callback) throws Exception {
        Resource[] resources = resourcePatternResolver.getResources(resourceValue);
        for (Resource resource : resources) {
            if (resource.isFile()) {
                File file = resource.getFile();
                fileWatchService.watch(file, event -> {
                    if (!file.equals(event.getFile())) {
                        return;
                    }
                    try {
                        callback.accept(resourceValue, new FileSystemResource(file));
                    } catch (Exception ex) {
                        logger.warn("Failed to reload property source for {}", resourceValue, ex);
                    }
                });
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
