package io.zhijun.core.spi;

import io.zhijun.core.spi.annotation.Spi;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Holds the current {@link SpiLoader} reference for one SPI type and allows safe manual reload.
 *
 * <p>This type does not watch the classpath or manage custom class loaders. It only provides
 * a stable indirection so upper layers can switch to a freshly reloaded loader atomically.
 *
 * @param <S> service type
 */
public final class ReloadableSpiHandle<S> {

    private final Class<S> serviceType;
    private final ClassLoader classLoader;
    private final AtomicReference<SpiLoader<S>> current;

    private ReloadableSpiHandle(Class<S> serviceType, ClassLoader classLoader, SpiLoader<S> initialLoader) {
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType must not be null");
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader must not be null");
        this.current = new AtomicReference<>(Objects.requireNonNull(initialLoader, "initialLoader must not be null"));
    }

    public static <S> ReloadableSpiHandle<S> of(Class<S> serviceType) {
        Class<S> checkedServiceType = Objects.requireNonNull(serviceType, "serviceType must not be null");
        if (!checkedServiceType.isAnnotationPresent(Spi.class)) {
            throw new IllegalArgumentException("SPI type must be annotated with @Spi: " + checkedServiceType.getName());
        }
        ClassLoader effectiveClassLoader = Thread.currentThread().getContextClassLoader();
        if (effectiveClassLoader == null) {
            effectiveClassLoader = checkedServiceType.getClassLoader();
        }
        return new ReloadableSpiHandle<>(
                checkedServiceType, effectiveClassLoader, SpiLoader.load(checkedServiceType, effectiveClassLoader));
    }

    public static <S> ReloadableSpiHandle<S> of(Class<S> serviceType, ClassLoader classLoader) {
        Class<S> checkedServiceType = Objects.requireNonNull(serviceType, "serviceType must not be null");
        ClassLoader effectiveClassLoader = classLoader != null ? classLoader : checkedServiceType.getClassLoader();
        return new ReloadableSpiHandle<>(
                checkedServiceType, effectiveClassLoader, SpiLoader.load(checkedServiceType, effectiveClassLoader));
    }

    public Class<S> getServiceType() {
        return serviceType;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public SpiLoader<S> getLoader() {
        return current.get();
    }

    public Optional<S> getFirst() {
        return current.get().getFirst();
    }

    public S requireFirst() {
        return getFirst().orElseThrow(() ->
                new IllegalStateException("No SPI implementation found for " + serviceType.getName()));
    }

    public SpiLoader<S> reload() {
        SpiLoader<S> reloaded = current.get().reload();
        current.set(reloaded);
        return reloaded;
    }
}
