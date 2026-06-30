package io.zhijun.core.watch;

import io.zhijun.core.spi.SpiLoader;
import io.zhijun.core.spi.annotation.Spi;
import io.zhijun.core.watch.internal.StandardWatchService;
import java.io.File;
import java.io.IOException;

/**
 * 文件/资源变更监控服务 SPI 接口。
 *
 * <p>提供用于发现和排序的 SPI 接口（{@link Spi}{@code @WatchService}），
 * 配合 {@link io.zhijun.core.spi.SpiLoader} 可装载自定义实现。
 *
 * <p>若无自定义实现，可通过 {@link #defaults()} 获取内置的基于
 * {@link java.nio.file.WatchService nio WatchService} 的标准实现。
 */
@Spi
public interface WatchService extends AutoCloseable {

    void watch(File file, FileChangedListener listener) throws IOException;

    void start();

    @Override
    void close() throws IOException;

    /**
     * 返回默认 WatchService 实现（{@link StandardWatchService}）。
     *
     * <p>与 {@link SpiLoader} 配合使用时，消费者可先尝试 SPI 发现，
     * 未发现时回退到此默认实现。此方法封装了 internal 实现细节。
     */
    static WatchService defaults() {
        try {
            return new StandardWatchService();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create default WatchService", e);
        }
    }
}
