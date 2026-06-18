package io.zhijun.spring.core.io.watch;

import java.io.File;
import java.io.IOException;

/**
 * File watch service.
 */
public interface FileWatchService extends AutoCloseable {

    void watch(File file, FileChangedListener listener) throws IOException;

    void start();

    @Override
    void close() throws IOException;
}
