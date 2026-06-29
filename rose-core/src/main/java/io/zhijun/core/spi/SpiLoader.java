package io.zhijun.core.spi;

import io.zhijun.core.spi.annotation.Spi;
import io.zhijun.core.spi.annotation.SpiImpl;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPI loader with deterministic ordering and lightweight runtime policies.
 *
 * @param <S> service type
 */
public final class SpiLoader<S> {

    private static final int DEFAULT_PRIORITY = Integer.MAX_VALUE;

    private static final boolean DEFAULT_SINGLETON = true;

    private static final ConcurrentHashMap<LoaderKey, SpiLoader<?>> LOADERS = new ConcurrentHashMap<>();

    private final Class<S> serviceType;

    private final ClassLoader classLoader;

    private final Set<String> excludedImplementationClassNames;

    private final List<ImplementationDefinition<S>> definitions;

    private SpiLoader(Class<S> serviceType, ClassLoader classLoader, Set<String> excludedImplementationClassNames) {
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType must not be null");
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader must not be null");
        this.excludedImplementationClassNames = excludedImplementationClassNames;
        Spi spi = serviceType.getAnnotation(Spi.class);
        if (spi == null) {
            throw new IllegalArgumentException("SPI type must be annotated with @Spi: " + serviceType.getName());
        }
        this.definitions = loadDefinitions();
    }

    public static <S> SpiLoader<S> load(Class<S> serviceType) {
        return load(serviceType, defaultClassLoader(serviceType), Collections.<String>emptyList());
    }

    public static <S> SpiLoader<S> load(Class<S> serviceType, ClassLoader classLoader) {
        return load(serviceType, classLoader, Collections.<String>emptyList());
    }

    public static <S> SpiLoader<S> load(Class<S> serviceType, List<String> excludedImplementationClassNames) {
        return load(serviceType, Thread.currentThread().getContextClassLoader(), excludedImplementationClassNames);
    }

    public static <S> SpiLoader<S> load(
            Class<S> serviceType, ClassLoader classLoader, List<String> excludedImplementationClassNames) {
        ClassLoader effectiveClassLoader = classLoader != null ? classLoader : defaultClassLoader(serviceType);
        Set<String> excluded = excludedImplementationClassNames == null
                ? Collections.emptySet()
                : new LinkedHashSet<String>(excludedImplementationClassNames);
        LoaderKey key = new LoaderKey(serviceType, effectiveClassLoader, excluded);
        @SuppressWarnings("unchecked")
        SpiLoader<S> loader = (SpiLoader<S>) LOADERS.computeIfAbsent(
                key,
                ignored -> new SpiLoader<S>(
                        serviceType, effectiveClassLoader, Collections.unmodifiableSet(excluded)));
        return loader;
    }

    public List<S> get() {
        List<S> instances = new ArrayList<S>(definitions.size());
        for (ImplementationDefinition<S> definition : definitions) {
            instances.add(definition.getInstance());
        }
        return Collections.unmodifiableList(instances);
    }

    public Optional<S> getFirst() {
        if (definitions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(definitions.get(0).getInstance());
    }

    public List<Class<? extends S>> getImplementationTypes() {
        List<Class<? extends S>> types = new ArrayList<Class<? extends S>>(definitions.size());
        for (ImplementationDefinition<S> definition : definitions) {
            types.add(definition.implementationType);
        }
        return Collections.unmodifiableList(types);
    }

    /**
     * Clears all cached loaders.
     * <p>This is intended for tests and controlled classpath reload scenarios only.
     */
    static void clearCache() {
        LOADERS.clear();
    }

    private List<ImplementationDefinition<S>> loadDefinitions() {
        List<String> classNames = readImplementationClassNames();
        List<ImplementationDefinition<S>> loaded = new ArrayList<ImplementationDefinition<S>>(classNames.size());
        for (String className : classNames) {
            if (excludedImplementationClassNames.contains(className)) {
                continue;
            }
            ImplementationDefinition<S> definition = loadDefinition(className);
            if (definition.enabled) {
                loaded.add(definition);
            }
        }
        Collections.sort(loaded, Comparator.comparingInt((ImplementationDefinition<S> left) -> left.priority).thenComparing(left -> left.implementationType.getName()));
        return Collections.unmodifiableList(loaded);
    }

    private ImplementationDefinition<S> loadDefinition(String className) {
        try {
            Class<?> candidate = Class.forName(className, true, classLoader);
            if (!serviceType.isAssignableFrom(candidate)) {
                throw new IllegalStateException(
                        "Implementation " + className + " does not implement " + serviceType.getName());
            }
            @SuppressWarnings("unchecked")
            Class<? extends S> implementationType = (Class<? extends S>) candidate;
            SpiImpl spiImpl = implementationType.getAnnotation(SpiImpl.class);
            if (spiImpl != null && !spiImpl.enabled()) {
                return ImplementationDefinition.disabled(implementationType);
            }
            int priority = resolvePriority(spiImpl);
            boolean singleton = spiImpl != null ? spiImpl.singleton() : DEFAULT_SINGLETON;
            return new ImplementationDefinition<S>(implementationType, priority, singleton);
        }
        catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Failed to load SPI implementation " + className, ex);
        }
    }

    private int resolvePriority(SpiImpl spiImpl) {
        if (spiImpl != null && spiImpl.priority() != DEFAULT_PRIORITY) {
            return spiImpl.priority();
        }
        return DEFAULT_PRIORITY;
    }

    private List<String> readImplementationClassNames() {
        String resourceName = "META-INF/services/" + serviceType.getName();
        Set<String> classNames = new LinkedHashSet<String>();
        try {
            Enumeration<URL> resources = classLoader.getResources(resourceName);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                InputStream inputStream = resource.openStream();
                try {
                    readImplementationClassNames(inputStream, classNames);
                }
                finally {
                    inputStream.close();
                }
            }
        }
        catch (IOException ex) {
            throw new IllegalStateException("Failed to read SPI descriptors for " + serviceType.getName(), ex);
        }
        return new ArrayList<String>(classNames);
    }

    private void readImplementationClassNames(InputStream inputStream, Set<String> classNames) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            String candidate = stripComment(line).trim();
            if (!candidate.isEmpty()) {
                classNames.add(candidate);
            }
        }
    }

    private String stripComment(String line) {
        int commentIndex = line.indexOf('#');
        return commentIndex >= 0 ? line.substring(0, commentIndex) : line;
    }

    private static ClassLoader defaultClassLoader(Class<?> serviceType) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            return contextClassLoader;
        }
        ClassLoader serviceClassLoader = serviceType.getClassLoader();
        return serviceClassLoader != null ? serviceClassLoader : ClassLoader.getSystemClassLoader();
    }

    private static final class LoaderKey {

        private final Class<?> serviceType;

        private final ClassLoader classLoader;

        private final Set<String> excludedImplementationClassNames;

        private LoaderKey(Class<?> serviceType, ClassLoader classLoader, Set<String> excludedImplementationClassNames) {
            this.serviceType = serviceType;
            this.classLoader = classLoader;
            this.excludedImplementationClassNames = excludedImplementationClassNames;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof LoaderKey)) {
                return false;
            }
            LoaderKey that = (LoaderKey) other;
            return serviceType.equals(that.serviceType)
                    && classLoader.equals(that.classLoader)
                    && excludedImplementationClassNames.equals(that.excludedImplementationClassNames);
        }

        @Override
        public int hashCode() {
            return Objects.hash(serviceType, classLoader, excludedImplementationClassNames);
        }
    }

    private static final class ImplementationDefinition<S> {

        private final Class<? extends S> implementationType;

        private final int priority;

        private final boolean singleton;

        private volatile S singletonInstance;

        private final boolean enabled;

        private ImplementationDefinition(Class<? extends S> implementationType, int priority, boolean singleton) {
            this(implementationType, priority, singleton, true);
        }

        private ImplementationDefinition(
                Class<? extends S> implementationType, int priority, boolean singleton, boolean enabled) {
            this.implementationType = implementationType;
            this.priority = priority;
            this.singleton = singleton;
            this.enabled = enabled;
        }

        private static <S> ImplementationDefinition<S> disabled(Class<? extends S> implementationType) {
            return new ImplementationDefinition<S>(implementationType, Integer.MAX_VALUE, true, false);
        }

        private S getInstance() {
            if (!enabled) {
                throw new IllegalStateException("Disabled SPI implementation should not be instantiated");
            }
            if (!singleton) {
                return instantiate();
            }
            S instance = singletonInstance;
            if (instance == null) {
                synchronized (this) {
                    instance = singletonInstance;
                    if (instance == null) {
                        instance = instantiate();
                        singletonInstance = instance;
                    }
                }
            }
            return instance;
        }

        private S instantiate() {
            try {
                Constructor<? extends S> constructor = implementationType.getDeclaredConstructor();
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true);
                }
                return constructor.newInstance();
            }
            catch (Exception ex) {
                throw new IllegalStateException(
                        "Failed to instantiate SPI implementation " + implementationType.getName(), ex);
            }
        }
    }
}
