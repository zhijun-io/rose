package io.zhijun.spring.core.propertysource.internal;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

import io.zhijun.spring.core.io.watch.FileChangedEvent;
import io.zhijun.spring.core.io.watch.FileChangedListener;
import io.zhijun.spring.core.propertysource.PropertySourceReloadCallback;

/**
 * Bridges file watch notifications to property-source reload callbacks.
 */
public final class ResourceFileChangedListener implements FileChangedListener {

    private static final Logger logger = LoggerFactory.getLogger(ResourceFileChangedListener.class);

    private final String resourceValue;

    private final PropertySourceReloadCallback callback;

    private final File watchedFile;

    public ResourceFileChangedListener(String resourceValue, PropertySourceReloadCallback callback, File watchedFile) {
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
        } catch (Throwable ex) {
            logger.warn("Failed to reload property source for {}", resourceValue, ex);
        }
    }
}
