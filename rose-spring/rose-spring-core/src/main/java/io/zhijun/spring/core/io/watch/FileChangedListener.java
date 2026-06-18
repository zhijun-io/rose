package io.zhijun.spring.core.io.watch;

/**
 * Listener for file system changes.
 */
public interface FileChangedListener {

    void onFileChanged(FileChangedEvent event);
}
