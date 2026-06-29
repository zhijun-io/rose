package io.zhijun.spring.core.io.watch;

/**
 * Listener for file system changes.
 */
@FunctionalInterface
public interface FileChangedListener {

    void onFileChanged(FileChangedEvent event);
}
