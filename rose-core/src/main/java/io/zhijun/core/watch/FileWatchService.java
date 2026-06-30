package io.zhijun.core.watch;

import io.zhijun.core.spi.annotation.Spi;
import java.io.File;
import java.io.IOException;

/**
 * File watch service.
 */
@Spi
public interface FileWatchService extends AutoCloseable {

    void watch(File file, FileChangedListener listener) throws IOException;

    void start();

    @Override
    void close() throws IOException;
}
