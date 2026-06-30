package io.zhijun.core.spi;

import io.zhijun.core.spi.annotation.Priority;

import java.util.*;

/**
 * 默认 SPI 加载器实现，基于 {@link java.util.ServiceLoader} + {@link Priority @Priority} 排序。
 *
 * <p>包级私有，通过 {@link SpiLoader#defaults()} 访问。单例设计。
 */
final class DefaultSpiLoader implements SpiLoader {

    static final SpiLoader INSTANCE = new DefaultSpiLoader();

    private DefaultSpiLoader() {}

    @Override
    public <S> List<S> loadAll(Class<S> type) {
        List<S> instances = new ArrayList<S>();
        for (S impl : ServiceLoader.load(type)) {
            instances.add(impl);
        }
        instances.sort(Comparator.comparingInt(
                impl -> resolvePriority(impl.getClass())));
        return Collections.unmodifiableList(instances);
    }

    private static int resolvePriority(Class<?> implClass) {
        Priority p = implClass.getAnnotation(Priority.class);
        return p != null ? p.value() : Integer.MAX_VALUE;
    }
}
