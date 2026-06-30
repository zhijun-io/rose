package io.zhijun.core.watch;

/**
 * Listener for file system changes.
 */
@FunctionalInterface
public interface FileChangedListener {

    void onFileChanged(FileChangedEvent event);
}
