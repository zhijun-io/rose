package io.zhijun.core.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import io.zhijun.core.spi.annotation.Priority;

/**
 * Lightweight SPI utility backed by {@link java.util.ServiceLoader}.
 *
 * <p>Discovery uses the standard {@code META-INF/services/} files
 * (auto-generated at compile time by {@code SpiImplProcessor}).
 * Ordering uses {@link Priority @Priority} on implementation classes.
 * Callers manage their own caching — Spring beans are a natural fit.
 */
public final class SpiSupport {

    private SpiSupport() {}

    /**
     * Load all implementations of {@code type}, ordered by priority.
     */
    public static <S> List<S> loadAll(Class<S> type) {
        List<S> instances = new ArrayList<S>();
        for (S impl : ServiceLoader.load(type)) {
            instances.add(impl);
        }
        instances.sort(Comparator.comparingInt(
                impl -> resolvePriority(impl.getClass())));
        return Collections.unmodifiableList(instances);
    }

    /**
     * Load the highest-priority implementation of {@code type}.
     */
    public static <S> Optional<S> loadFirst(Class<S> type) {
        return loadAll(type).stream().findFirst();
    }

    private static int resolvePriority(Class<?> implClass) {
        Priority p = implClass.getAnnotation(Priority.class);
        return p != null ? p.value() : Integer.MAX_VALUE;
    }
}
