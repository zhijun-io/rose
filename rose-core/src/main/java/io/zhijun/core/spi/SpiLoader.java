package io.zhijun.core.spi;

import java.util.List;
import java.util.Optional;

/**
 * SPI 加载器接口，将 SPI 实现类的<b>发现</b>与<b>排序</b>封装为一个 seam。
 *
 * <p>消费者可以：
 * <ul>
 *   <li>在测试中注入 mock 实现</li>
 *   <li>在未来切换 ServiceLoader 的实现策略</li>
 * </ul>
 *
 * <p><b>职责边界</b>：只做发现和排序。缓存、生命周期、条件过滤由上层框架负责。
 *
 * @see DefaultSpiLoader
 */
@FunctionalInterface
public interface SpiLoader {

    /**
     * 加载 {@code type} 的所有实现，按 {@link io.zhijun.core.spi.annotation.Priority @Priority} 排序。
     */
    <S> List<S> loadAll(Class<S> type);

    /**
     * 加载 {@code type} 优先级最高的实现。
     */
    default <S> Optional<S> loadFirst(Class<S> type) {
        return loadAll(type).stream().findFirst();
    }

    /**
     * 返回默认 SPI 加载器实现。
     *
     * <p>基于 {@link java.util.ServiceLoader} + {@link io.zhijun.core.spi.annotation.Priority @Priority} 排序。
     */
    static SpiLoader defaults() {
        return DefaultSpiLoader.INSTANCE;
    }
}
