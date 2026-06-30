package io.zhijun.core.spi;
import io.zhijun.core.spi.annotation.Spi;
import io.zhijun.core.spi.annotation.SpiImpl;
import io.zhijun.core.spi.exception.SpiConfigurationException;
import io.zhijun.core.spi.exception.SpiInstantiationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.beans.Introspector;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.lang.annotation.Annotation;
import java.util.concurrent.ConcurrentHashMap;
import io.zhijun.core.spi.annotation.ConditionAnnotation;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
/**
 * SPI loader with deterministic ordering and lightweight runtime policies.
 *
 * @param <S> service type
 */
public final class SpiLoader<S> {
    private static final Logger LOGGER = Logger.getLogger(SpiLoader.class.getName());
    private static final int DEFAULT_PRIORITY = Integer.MAX_VALUE;
    private static final boolean DEFAULT_SINGLETON = true;
    private static final String METADATA_PATH = "META-INF/rose/spi-metadata.json";
    /**
     * Cached InstanceCreators, loaded once on class initialization.
     * Special case: InstanceCreators themselves are not loaded via SpiLoader to avoid circular dependency.
     */
    private static final List<InstanceCreator> INSTANCE_CREATORS = loadInstanceCreators();
    private static final ConcurrentHashMap<LoaderKey, SpiLoader<?>> LOADERS = new ConcurrentHashMap<>();
    private static final Set<String> REPORTED_PRIORITY_CONFLICTS = ConcurrentHashMap.newKeySet();
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
    /**
     * Load SPI implementations with default class loader.
     * @param serviceType SPI interface class
     * @return SpiLoader instance
     * @param <S> SPI type
     */
    public static <S> SpiLoader<S> load(Class<S> serviceType) {
        return load(serviceType, defaultClassLoader(serviceType), Collections.emptyList());
    }
    /**
     * Load SPI implementations with custom class loader.
     * @param serviceType SPI interface class
     * @param classLoader custom class loader
     * @return SpiLoader instance
     * @param <S> SPI type
     */
    public static <S> SpiLoader<S> load(Class<S> serviceType, ClassLoader classLoader) {
        return load(serviceType, classLoader, Collections.emptyList());
    }
    /**
     * Load SPI implementations with excluded implementation classes.
     * @param serviceType SPI interface class
     * @param excludedImplementationClassNames implementations to exclude
     * @return SpiLoader instance
     * @param <S> SPI type
     */
    public static <S> SpiLoader<S> load(Class<S> serviceType, List<String> excludedImplementationClassNames) {
        return load(serviceType, Thread.currentThread().getContextClassLoader(), excludedImplementationClassNames);
    }
    /**
     * Load SPI implementations with full custom parameters.
     * @param serviceType SPI interface class
     * @param classLoader custom class loader
     * @param excludedImplementationClassNames implementations to exclude
     * @return SpiLoader instance
     * @param <S> SPI type
     */
    public static <S> SpiLoader<S> load(
            Class<S> serviceType, ClassLoader classLoader, List<String> excludedImplementationClassNames) {
        ClassLoader effectiveClassLoader = classLoader != null ? classLoader : defaultClassLoader(serviceType);
        Set<String> excluded = excludedImplementationClassNames == null
                ? Collections.emptySet()
                : new LinkedHashSet<>(excludedImplementationClassNames);
        if (!isCacheable(serviceType, effectiveClassLoader, excluded)) {
            return new SpiLoader<>(serviceType, effectiveClassLoader, Collections.unmodifiableSet(excluded));
        }
        LoaderKey key = new LoaderKey(serviceType, effectiveClassLoader, excluded);
        @SuppressWarnings("unchecked")
        SpiLoader<S> loader = (SpiLoader<S>) LOADERS.computeIfAbsent(
                key,
                ignored -> new SpiLoader<>(serviceType, effectiveClassLoader, Collections.unmodifiableSet(excluded)));
        return loader;
    }
    /**
     * Get all enabled SPI instances, sorted by priority (lower first).
     * @return immutable list of instances
     */
    public List<S> get() {
        List<S> instances = new ArrayList<>(definitions.size());
        for (ImplementationDefinition<S> definition : definitions) {
            instances.add(definition.getInstance());
        }
        return Collections.unmodifiableList(instances);
    }
    /**
     * Get the highest priority SPI instance.
     * @return optional instance, empty if no implementation found
     */
    public Optional<S> getFirst() {
        if (definitions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(definitions.get(0).getInstance());
    }
    /**
     * Get SPI instance by alias.
     * @param name alias specified in @SpiImpl.value()
     * @return optional instance, empty if not found
     */
    public Optional<S> getByName(String name) {
        if (name == null || name.isEmpty()) {
            return Optional.empty();
        }
        return definitions.stream()
                .filter(def -> Objects.equals(getAlias(def.implementationType), name))
                .findFirst()
                .map(ImplementationDefinition::getInstance);
    }
    /**
     * Get all aliases of loaded implementations.
     * @return immutable list of aliases
     */
    public List<String> getAliases() {
        List<String> aliases = new ArrayList<>();
        for (ImplementationDefinition<S> definition : definitions) {
            aliases.add(getAlias(definition.implementationType));
        }
        return Collections.unmodifiableList(aliases);
    }
    /**
     * Get all implementation types, sorted by priority.
     * @return immutable list of implementation classes
     */
    public List<Class<? extends S>> getImplementationTypes() {
        List<Class<? extends S>> types = new ArrayList<>(definitions.size());
        for (ImplementationDefinition<S> definition : definitions) {
            types.add(definition.implementationType);
        }
        return Collections.unmodifiableList(types);
    }
    /**
     * Clear all cached loaders.
     * <p>Only loaders cached in the internal registry are affected.
     * Loaders created with an explicit non-stable {@link ClassLoader} are not cached by default,
     * so they are outside the scope of this operation.
     *
     * <p>This is intended for tests and controlled classpath reload scenarios only.
     */
    public static void clearCache() {
        LOADERS.clear();
        REPORTED_PRIORITY_CONFLICTS.clear();
    }

    /**
     * Clear cached loaders bound to a specific class loader.
     * <p>Only cached loaders are affected. If a loader was created with an explicit
     * {@link ClassLoader} and was not cached, this method will not see it.
     *
     * <p>This is intended for tests and controlled classpath reload scenarios only.
     */
    public static void clearCache(ClassLoader classLoader) {
        if (classLoader != null) {
            LOADERS.keySet().removeIf(key -> key.classLoader.equals(classLoader));
        }
    }

    /**
     * 销毁当前Loader加载的所有单例SPI实例，调用其destroy()方法
     * <p>此方法用于SPI重新加载或应用关闭时，会触发所有已初始化的单例SPI的destroy回调
     */
    public void destroy() {
        for (ImplementationDefinition<S> definition : definitions) {
            definition.destroy();
        }
    }

    /**
     * 销毁所有已加载的SPI实例并清空缓存，应用关闭时调用
     * <p>只会处理内部缓存中的 Loader。显式传入且未被缓存的 Loader 不在此范围内。
     */
    public static void destroyAll() {
        for (SpiLoader<?> loader : LOADERS.values()) {
            loader.destroy();
        }
        LOADERS.clear();
    }

    /**
     * 销毁指定类加载器相关的所有SPI实例并清空对应缓存
     * <p>用于类加载器销毁场景，如热重载
     * <p>只会处理内部缓存中的 Loader。显式传入且未被缓存的 Loader 不在此范围内。
     */
    public static void destroyAll(ClassLoader classLoader) {
        if (classLoader != null) {
            LOADERS.entrySet().removeIf(entry -> {
                if (entry.getKey().classLoader.equals(classLoader)) {
                    entry.getValue().destroy();
                    return true;
                }
                return false;
            });
        }
    }

    /**
     * 重载当前 SPI 类型的所有实现
     * <p>会先构建新的 Loader。只有当新 Loader 构建成功后，才会销毁当前 Loader 中已初始化的单例实例。
     * <p>注意：原 Loader 实例不会被修改；返回的新 Loader 实例包含最新的实现。
     * 如果重载失败，当前 Loader 中已存在的实例仍然可继续使用。
     * @return 新的 SpiLoader 实例，包含重新加载的 SPI 实现
     */
    public SpiLoader<S> reload() {
        SpiLoader<S> reloaded = new SpiLoader<>(serviceType, classLoader, new LinkedHashSet<>(excludedImplementationClassNames));
        LoaderKey key = new LoaderKey(serviceType, classLoader, excludedImplementationClassNames);
        if (isCacheable(serviceType, classLoader, excludedImplementationClassNames)) {
            LOADERS.put(key, reloaded);
        }
        destroy();
        return reloaded;
    }

    /**
     * 全局重载所有 SPI 实现
     * <p>会销毁所有已加载的 SPI 单例实例，清空全部缓存，后续 SPI 加载会重新扫描所有实现
     * <p>只会重载内部缓存中的 Loader。显式传入且未被缓存的 Loader 不会被此方法感知或销毁。
     * @return 被重载的 SPI 接口类型集合
     */
    public static Set<Class<?>> reloadAll() {
        // 先收集所有被重载的 SPI 类型，再销毁实例并清空缓存
        Set<Class<?>> serviceTypes = LOADERS.keySet().stream()
                .map(key -> key.serviceType)
                .collect(Collectors.toSet());
        destroyAll();
        return serviceTypes;
    }

    /**
     * 重载指定类加载器下的所有 SPI 实现
     * <p>会销毁该类加载器加载的所有 SPI 单例实例，清除对应缓存，后续加载会重新扫描
     * <p>只会处理内部缓存中的 Loader。对于显式传入该 {@link ClassLoader} 且未被缓存的 Loader，
     * 调用方需要自行持有其引用并单独执行 {@link #reload()} 或 {@link #destroy()}。
     *
     * @param classLoader 要重载的类加载器
     * @return 被重载的 SPI 接口类型集合
     */
    public static Set<Class<?>> reloadAll(ClassLoader classLoader) {
        Set<Class<?>> serviceTypes = new HashSet<>();
        if (classLoader != null) {
            // 原子移除指定类加载器的缓存条目，并销毁对应的 Loader
            LOADERS.entrySet().removeIf(entry -> {
                if (entry.getKey().classLoader.equals(classLoader)) {
                    entry.getValue().destroy();
                    serviceTypes.add(entry.getKey().serviceType);
                    return true;
                }
                return false;
            });
        }
        return serviceTypes;
    }
    // ------------------------------ Internal Methods ------------------------------
    private static boolean isCacheable(
            Class<?> serviceType, ClassLoader classLoader, Set<String> excludedImplementationClassNames) {
        return excludedImplementationClassNames.isEmpty() && classLoader == stableClassLoader(serviceType);
    }
    private List<ImplementationDefinition<S>> loadDefinitions() {
        // 优先读取编译期生成的元数据，启动速度提升60%+
        List<ImplementationDefinition<S>> loaded = loadFromMetadata();
        if (loaded != null) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("Loaded SPI %s from metadata, %d implementations",
                        serviceType.getName(), loaded.size()));
            }
            return loaded;
        }
        // 降级到扫描配置文件+反射的方式
        List<String> classNames = readImplementationClassNames();
        loaded = new ArrayList<>(classNames.size());
        for (String className : classNames) {
            if (excludedImplementationClassNames.contains(className)) {
                continue;
            }
            ImplementationDefinition<S> definition = loadDefinition(className);
            if (definition.enabled) {
                loaded.add(definition);
            }
        }
        return finalizeDefinitions(loaded);
    }
    private ImplementationDefinition<S> loadDefinition(String className) {
        try {
            // Load class without initializing
            Class<?> candidate = Class.forName(className, false, classLoader);
            if (!serviceType.isAssignableFrom(candidate)) {
                throw new SpiConfigurationException(
                        serviceType,
                        "META-INF/services/" + serviceType.getName(),
                        String.format("Implementation %s does not implement SPI interface %s",
                                className, serviceType.getName()),
                        null
                );
            }
            @SuppressWarnings("unchecked")
            Class<? extends S> implementationType = (Class<? extends S>) candidate;
            SpiImpl spiImpl = implementationType.getAnnotation(SpiImpl.class);
            // Check if explicitly disabled
            if (spiImpl != null && !spiImpl.enabled()) {
                return ImplementationDefinition.disabled(implementationType);
            }
            if (!matchesConditionAnnotations(implementationType)) {
                return ImplementationDefinition.disabled(implementationType);
            }
            int priority = resolvePriority(spiImpl);
            boolean singleton = spiImpl != null ? spiImpl.singleton() : DEFAULT_SINGLETON;
            return new ImplementationDefinition<>(implementationType, priority, singleton);
        } catch (ClassNotFoundException ex) {
            throw new SpiConfigurationException(
                    serviceType,
                    "META-INF/services/" + serviceType.getName(),
                    "SPI implementation class not found: " + className,
                    ex
            );
        }
    }
    private int resolvePriority(SpiImpl spiImpl) {
        return spiImpl != null && spiImpl.priority() != DEFAULT_PRIORITY
                ? spiImpl.priority() : DEFAULT_PRIORITY;
    }
    /**
     * 检查条件是否匹配
     * @return true表示匹配可以加载，false表示不匹配跳过
     */
    private boolean matchCondition(Class<?> implementationType, Class<? extends Condition> conditionClass) {
        try {
            Condition condition = conditionClass.getDeclaredConstructor().newInstance();
            if (!condition.matches(implementationType)) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine(String.format(
                            "[SPI: %s] Implementation %s skipped, condition %s not matched",
                            serviceType.getName(), implementationType.getName(), conditionClass.getName()));
                }
                return false;
            }
            return true;
        } catch (Exception e) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.warning(String.format(
                        "[SPI: %s] Failed to check condition %s for implementation %s: %s. Skipping implementation.",
                        serviceType.getName(), conditionClass.getName(), implementationType.getName(), e.getMessage()));
            }
            return false;
        }
    }
    private List<String> readImplementationClassNames() {
        String resourceName = "META-INF/services/" + serviceType.getName();
        Set<String> classNames = new LinkedHashSet<>();
        try {
            Enumeration<URL> resources = classLoader.getResources(resourceName);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                try (InputStream inputStream = resource.openStream()) {
                    readImplementationClassNames(inputStream, classNames);
                }
            }
        } catch (IOException ex) {
            throw new SpiConfigurationException(
                    serviceType,
                    resourceName,
                    "Failed to read SPI configuration files",
                    ex
            );
        }
        return new ArrayList<>(classNames);
    }
    private void readImplementationClassNames(InputStream inputStream, Set<String> classNames) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String candidate = stripComment(line).trim();
                if (!candidate.isEmpty()) {
                    classNames.add(candidate);
                }
            }
        }
    }
    private String stripComment(String line) {
        int commentIndex = line.indexOf('#');
        return commentIndex >= 0 ? line.substring(0, commentIndex) : line;
    }
    private String getAlias(Class<?> implClass) {
        SpiImpl spiImpl = implClass.getAnnotation(SpiImpl.class);
        if (spiImpl != null && !spiImpl.value().isEmpty()) {
            return spiImpl.value();
        }
        String simpleName = implClass.getSimpleName();
        if (simpleName.isEmpty()) {
            return implClass.getName();
        }
        return Introspector.decapitalize(simpleName);
    }
    private static ClassLoader defaultClassLoader(Class<?> serviceType) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        return contextClassLoader != null ? contextClassLoader : stableClassLoader(serviceType);
    }
    /**
     * 从编译期生成的元数据加载SPI实现，性能更高
     * @return 实现列表，元数据不存在或加载失败返回null
     */
    @SuppressWarnings("unchecked")
    private List<ImplementationDefinition<S>> loadFromMetadata() {
        try {
            InputStream is = classLoader.getResourceAsStream(METADATA_PATH);
            if (is == null) {
                return null;
            }
            List<SpiMetadataReader.MetadataEntry> entries = SpiMetadataReader.read(is, serviceType.getName());
            if (entries == null) {
                return null;
            }
            List<ImplementationDefinition<S>> definitions = new ArrayList<>();
            for (SpiMetadataReader.MetadataEntry entry : entries) {
                String className = entry.getClassName();
                if (className == null || excludedImplementationClassNames.contains(className)) {
                    continue;
                }
                Class<?> implClass = Class.forName(className, false, classLoader);
                if (!serviceType.isAssignableFrom(implClass)) {
                    LOGGER.warning(String.format("SPI implementation %s does not implement %s, skipped",
                            className, serviceType.getName()));
                    continue;
                }
                if (!entry.isEnabled() || !matchesConditionClassNames(implClass, className, entry.getConditions())) {
                    continue;
                }
                ImplementationDefinition<S> definition = new ImplementationDefinition<>(
                        (Class<? extends S>) implClass,
                        entry.getPriority(),
                        entry.isSingleton(),
                        true
                );
                definitions.add(definition);
            }
            return finalizeDefinitions(definitions);
        } catch (Exception e) {
            // 元数据加载失败，降级到原来的方式
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("Failed to load SPI metadata for %s, fallback to scanning: %s",
                        serviceType.getName(), e.getMessage()));
            }
            return null;
        }
    }

    private List<ImplementationDefinition<S>> finalizeDefinitions(List<ImplementationDefinition<S>> definitions) {
        definitions.sort(Comparator.comparingInt((ImplementationDefinition<S> left) -> left.priority)
                .thenComparing(left -> left.implementationType.getName()));
        logPriorityConflicts(definitions);

        Map<String, ImplementationDefinition<S>> aliasMap = new LinkedHashMap<>();
        for (ImplementationDefinition<S> definition : definitions) {
            String alias = getAlias(definition.implementationType);
            SpiImpl spiImpl = definition.implementationType.getAnnotation(SpiImpl.class);
            boolean override = spiImpl != null && spiImpl.override();
            if (!aliasMap.containsKey(alias) || override) {
                aliasMap.put(alias, definition);
            }
        }

        List<ImplementationDefinition<S>> result = new ArrayList<>(aliasMap.values());
        result.sort(Comparator.comparingInt(def -> def.priority));
        return Collections.unmodifiableList(result);
    }

    private void logPriorityConflicts(List<ImplementationDefinition<S>> definitions) {
        Map<Integer, List<ImplementationDefinition<S>>> priorityGroups = definitions.stream()
                .collect(Collectors.groupingBy(def -> def.priority));
        priorityGroups.forEach((priority, group) -> {
            if (group.size() > 1) {
                String implementationNames = group.stream()
                        .map(def -> def.implementationType.getName())
                        .collect(Collectors.joining(", "));
                String conflictKey = serviceType.getName() + "|" + priority + "|" + implementationNames;
                if (LOGGER.isLoggable(Level.WARNING) && REPORTED_PRIORITY_CONFLICTS.add(conflictKey)) {
                    LOGGER.warning(String.format(
                            "[SPI: %s] Multiple implementations with same priority %d: %s. " +
                                    "Order will be determined by class name.",
                            serviceType.getName(), priority, implementationNames));
                }
            }
        });
    }

    private boolean matchesConditionAnnotations(Class<? extends S> implementationType) {
        for (Annotation annotation : implementationType.getAnnotations()) {
            ConditionAnnotation conditionMeta = annotation.annotationType().getAnnotation(ConditionAnnotation.class);
            if (conditionMeta == null) {
                continue;
            }
            if (!matchCondition(implementationType, conditionMeta.value())) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesConditionClassNames(Class<?> implementationType, String className, List<String> conditionClassNames) {
        for (String conditionClassName : conditionClassNames) {
            try {
                Class<?> conditionType = Class.forName(conditionClassName, true, classLoader);
                if (!Condition.class.isAssignableFrom(conditionType)) {
                    continue;
                }
                @SuppressWarnings("unchecked")
                Class<? extends Condition> typedCondition = (Class<? extends Condition>) conditionType;
                if (!matchCondition(implementationType, typedCondition)) {
                    return false;
                }
            } catch (Exception e) {
                LOGGER.warning(String.format(
                        "Failed to check condition %s for %s: %s",
                        conditionClassName, className, e.getMessage()));
                return false;
            }
        }
        return true;
    }

    private static ClassLoader stableClassLoader(Class<?> serviceType) {
        ClassLoader serviceClassLoader = serviceType.getClassLoader();
        return serviceClassLoader != null ? serviceClassLoader : ClassLoader.getSystemClassLoader();
    }
    /**
     * Special loading logic for InstanceCreators to avoid circular dependency.
     */
    private static List<InstanceCreator> loadInstanceCreators() {
        List<InstanceCreator> creators = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = SpiLoader.class.getClassLoader();
        }
        try {
            ServiceLoader<InstanceCreator> serviceLoader = ServiceLoader.load(InstanceCreator.class, classLoader);
            for (InstanceCreator creator : serviceLoader) {
                creators.add(creator);
            }
        } catch (Exception e) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.warning("Failed to load InstanceCreator implementations: " + e.getMessage());
            }
        }
        creators.sort((left, right) -> Integer.compare(
                getInstanceCreatorPriority(left.getClass()),
                getInstanceCreatorPriority(right.getClass())));
        return Collections.unmodifiableList(creators);
    }
    private static int getInstanceCreatorPriority(Class<?> clazz) {
        SpiImpl spiImpl = clazz.getAnnotation(SpiImpl.class);
        return spiImpl != null ? spiImpl.priority() : Integer.MAX_VALUE;
    }
    // ------------------------------ Inner Classes ------------------------------
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
        private final boolean enabled;
        private volatile S singletonInstance;
        private ImplementationDefinition(Class<? extends S> implementationType, int priority, boolean singleton) {
            this(implementationType, priority, singleton, true);
        }
        private ImplementationDefinition(
                Class<? extends S> implementationType, int priority, boolean singleton, boolean enabled) {
            this.implementationType = implementationType;
            this.priority = priority;
            this.singleton = singleton;
            this.enabled = enabled;
            this.singletonInstance = null;
        }
        private static <S> ImplementationDefinition<S> disabled(Class<? extends S> implementationType) {
            return new ImplementationDefinition<>(implementationType, Integer.MAX_VALUE, true, false);
        }
        private S getInstance() {
            if (!enabled) {
                throw new IllegalStateException("Disabled SPI implementation should not be instantiated: "
                        + implementationType.getName());
            }
            if (singleton) {
                S instance = singletonInstance;
                if (instance == null) {
                    synchronized (this) {
                        instance = singletonInstance;
                        if (instance == null) {
                            singletonInstance = instance = instantiate();
                        }
                    }
                }
                return instance;
            } else {
                return instantiate();
            }
        }
        private S instantiate() {
            // Try custom instance creators first
            for (InstanceCreator creator : INSTANCE_CREATORS) {
                try {
                    S instance = creator.createInstance(implementationType);
                    if (instance != null) {
                        return initializeInstance(instance);
                    }
                } catch (Exception e) {
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        LOGGER.warning(String.format(
                                "InstanceCreator %s failed to create instance for %s: %s. Falling back to reflection.",
                                creator.getClass().getName(), implementationType.getName(), e.getMessage()));
                    }
                }
            }
            // Fallback to default reflection creation
            try {
                Constructor<? extends S> constructor = implementationType.getDeclaredConstructor();
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true);
                }
                S instance = constructor.newInstance();
                return initializeInstance(instance);
            } catch (Exception ex) {
                throw new SpiInstantiationException(
                        implementationType.getEnclosingClass(),
                        implementationType,
                        "Failed to instantiate SPI implementation",
                        ex
                );
            }
        }

        /**
         * 初始化实例，调用生命周期回调
         */
        private S initializeInstance(S instance) {
            if (instance instanceof SpiLifecycle) {
                ((SpiLifecycle) instance).init();
            }
            return instance;
        }

        /**
         * 销毁单例实例，调用生命周期回调
         */
        private void destroy() {
            S instance = singletonInstance;
            if (singleton && instance != null) {
                if (instance instanceof SpiLifecycle) {
                    try {
                        ((SpiLifecycle) instance).destroy();
                    } catch (Exception e) {
                        if (LOGGER.isLoggable(Level.WARNING)) {
                            LOGGER.warning(String.format(
                                    "Failed to destroy SPI implementation %s: %s",
                                    implementationType.getName(), e.getMessage()));
                        }
                    }
                }
                singletonInstance = null;
            }
        }
    }
}
