package io.zhijun.core.spi;
import io.zhijun.core.spi.annotation.Spi;
import io.zhijun.core.spi.annotation.SpiImpl;
import io.zhijun.core.spi.exception.SpiConfigurationException;
import io.zhijun.core.spi.exception.SpiInstantiationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.lang.annotation.Annotation;
import java.util.concurrent.ConcurrentHashMap;
import io.zhijun.core.spi.condition.annotation.ConditionAnnotation;
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
     * SPI allowed packages security white list.
     * Configure via JVM parameter: -Drose.spi.allowed-packages=io.zhijun,com.yourcompany
     * Empty means no restriction.
     */
    private static final Set<String> ALLOWED_PACKAGES;
    static {
        String config = System.getProperty("rose.spi.allowed-packages", "");
        Set<String> allowed = new LinkedHashSet<>();
        for (String s : config.split(",")) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                allowed.add(trimmed);
            }
        }
        ALLOWED_PACKAGES = Collections.unmodifiableSet(allowed);
    }
    /**
     * Cached InstanceCreators, loaded once on class initialization.
     * Special case: InstanceCreators themselves are not loaded via SpiLoader to avoid circular dependency.
     */
    private static final List<InstanceCreator> INSTANCE_CREATORS = loadInstanceCreators();
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
     * <p>This is intended for tests and controlled classpath reload scenarios only.
     */
    public static void clearCache() {
        LOADERS.clear();
    }
    /**
     * Clear cached loaders bound to a specific class loader.
     * <p>This is intended for tests and controlled classpath reload scenarios only.
     */
    public static void clearCache(ClassLoader classLoader) {
        if (classLoader != null) {
            LOADERS.keySet().removeIf(key -> key.classLoader.equals(classLoader));
        }
    }
    /**
     * Get all loaded SPI information for monitoring.
     * @return SPI interface -> implementation info map
     */
    public static Map<Class<?>, List<SpiInfo>> getAllSpiInfo() {
        Map<Class<?>, List<SpiInfo>> result = new ConcurrentHashMap<>();
        for (Map.Entry<LoaderKey, SpiLoader<?>> entry : LOADERS.entrySet()) {
            SpiLoader<?> loader = entry.getValue();
            List<SpiInfo> infos = new ArrayList<>();
            for (ImplementationDefinition<?> def : loader.definitions) {
                infos.add(new SpiInfo(
                        def.implementationType.getName(),
                        loader.getAlias(def.implementationType),
                        def.priority,
                        def.singleton,
                        def.enabled
                ));
            }
            result.put(entry.getKey().serviceType, Collections.unmodifiableList(infos));
        }
        return Collections.unmodifiableMap(result);
    }
    /**
     * SPI information for monitoring.
     */
    public static class SpiInfo {
        private final String className;
        private final String alias;
        private final int priority;
        private final boolean singleton;
        private final boolean enabled;
        public SpiInfo(String className, String alias, int priority, boolean singleton, boolean enabled) {
            this.className = className;
            this.alias = alias;
            this.priority = priority;
            this.singleton = singleton;
            this.enabled = enabled;
        }
        public String getClassName() { return className; }
        public String getAlias() { return alias; }
        public int getPriority() { return priority; }
        public boolean isSingleton() { return singleton; }
        public boolean isEnabled() { return enabled; }
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
        // Sort by priority first, then by class name for deterministic order
        loaded.sort(Comparator.comparingInt((ImplementationDefinition<S> left) -> left.priority)
                .thenComparing(left -> left.implementationType.getName()));
        // Detect priority conflicts and warn
        Map<Integer, List<ImplementationDefinition<S>>> priorityGroups = loaded.stream()
                .collect(Collectors.groupingBy(def -> def.priority));
        priorityGroups.forEach((priority, group) -> {
            if (group.size() > 1) {
                String implNames = group.stream()
                        .map(def -> def.implementationType.getName())
                        .collect(Collectors.joining(", "));
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.warning(String.format(
                            "[SPI: %s] Multiple implementations with same priority %d: %s. " +
                                    "Order will be determined by class name.",
                            serviceType.getName(), priority, implNames));
                }
            }
        });
        // Handle implementation override (same alias, higher priority overrides lower)
        Map<String, ImplementationDefinition<S>> aliasMap = new LinkedHashMap<>();
        for (ImplementationDefinition<S> def : loaded) {
            String alias = getAlias(def.implementationType);
            SpiImpl spiImpl = def.implementationType.getAnnotation(SpiImpl.class);
            boolean override = spiImpl != null && spiImpl.override();
            if (!aliasMap.containsKey(alias) || override) {
                aliasMap.put(alias, def);
            }
        }
        List<ImplementationDefinition<S>> result = new ArrayList<>(aliasMap.values());
        result.sort(Comparator.comparingInt(def -> def.priority));
        return Collections.unmodifiableList(result);
    }
    private ImplementationDefinition<S> loadDefinition(String className) {
        // Security white list check
        if (!ALLOWED_PACKAGES.isEmpty()) {
            boolean allowed = ALLOWED_PACKAGES.stream().anyMatch(className::startsWith);
            if (!allowed) {
                throw new SecurityException(String.format(
                        "SPI implementation %s is not in allowed packages: %s. " +
                                "Configure 'rose.spi.allowed-packages' JVM parameter to add it.",
                        className, ALLOWED_PACKAGES
                ));
            }
        }
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
            // Check load conditions: 1. @SpiImpl.conditions attribute
            if (spiImpl != null && spiImpl.conditions().length > 0) {
                for (Class<? extends Condition> conditionClass : spiImpl.conditions()) {
                    if (!matchCondition(implementationType, conditionClass)) {
                        return ImplementationDefinition.disabled(implementationType);
                    }
                }
            }
            // Check load conditions: 2. Condition annotations on implementation class
            for (Annotation annotation : implementationType.getAnnotations()) {
                ConditionAnnotation conditionMeta = annotation.annotationType().getAnnotation(ConditionAnnotation.class);
                if (conditionMeta == null) {
                    continue;
                }
                Class<? extends Condition> conditionClass = conditionMeta.value();
                if (!matchCondition(implementationType, conditionClass)) {
                    return ImplementationDefinition.disabled(implementationType);
                }
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
        // Custom first letter lowercase, avoid dependency on java.bean.Introspector
        String simpleName = implClass.getSimpleName();
        if (simpleName.isEmpty()) {
            return implClass.getName();
        }
        char firstChar = simpleName.charAt(0);
        return Character.isLowerCase(firstChar)
                ? simpleName
                : Character.toLowerCase(firstChar) + simpleName.substring(1);
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
            String json;
            try {
                byte[] buffer = new byte[is.available()];
                is.read(buffer);
                json = new String(buffer, StandardCharsets.UTF_8);
            } finally {
                is.close();
            }
            // 解析JSON，格式很固定，直接查找当前SPI接口
            String spiKey = "\"" + serviceType.getName() + "\":";
            int spiIndex = json.indexOf(spiKey);
            if (spiIndex == -1) {
                // 元数据里没有当前SPI，返回null走降级逻辑
                return null;
            }
            // 找到当前SPI的实现数组
            int arrayStart = json.indexOf('[', spiIndex + spiKey.length());
            int arrayEnd = findMatchingBracket(json, arrayStart, '[', ']');
            if (arrayStart == -1 || arrayEnd == -1) {
                return null;
            }
            String arrayJson = json.substring(arrayStart + 1, arrayEnd);
            List<ImplementationDefinition<S>> definitions = new ArrayList<>();
            // 逐个解析实现对象
            int objStart = arrayJson.indexOf('{');
            while (objStart != -1) {
                int objEnd = findMatchingBracket(arrayJson, objStart, '{', '}');
                if (objEnd == -1) {
                    break;
                }
                String objJson = arrayJson.substring(objStart + 1, objEnd);
                // 解析属性
                String className = getJsonStringValue(objJson, "className");
                if (className == null || excludedImplementationClassNames.contains(className)) {
                    objStart = arrayJson.indexOf('{', objEnd + 1);
                    continue;
                }
                String alias = getJsonStringValue(objJson, "alias");
                int priority = getJsonIntValue(objJson, "priority", DEFAULT_PRIORITY);
                boolean singleton = getJsonBooleanValue(objJson, "singleton", DEFAULT_SINGLETON);
                boolean enabled = getJsonBooleanValue(objJson, "enabled", true);
                boolean override = getJsonBooleanValue(objJson, "override", false);
                List<String> conditions = getJsonStringArray(objJson, "conditions");
                // 加载实现类
                Class<?> implClass = Class.forName(className, false, classLoader);
                if (!serviceType.isAssignableFrom(implClass)) {
                    LOGGER.warning(String.format("SPI implementation %s does not implement %s, skipped",
                            className, serviceType.getName()));
                    objStart = arrayJson.indexOf('{', objEnd + 1);
                    continue;
                }
                // 检查条件
                boolean conditionMatch = true;
                for (String conditionClass : conditions) {
                    try {
                        Class<?> condClass = Class.forName(conditionClass, true, classLoader);
                        if (!Condition.class.isAssignableFrom(condClass)) {
                            continue;
                        }
                        Condition condition = (Condition) condClass.getDeclaredConstructor().newInstance();
                        if (!condition.matches((Class<?>) implClass)) {
                            conditionMatch = false;
                            break;
                        }
                    } catch (Exception e) {
                        LOGGER.warning(String.format("Failed to check condition %s for %s: %s",
                                conditionClass, className, e.getMessage()));
                        conditionMatch = false;
                        break;
                    }
                }
                if (!enabled || !conditionMatch) {
                    objStart = arrayJson.indexOf('{', objEnd + 1);
                    continue;
                }
                // 创建定义
                ImplementationDefinition<S> definition = new ImplementationDefinition<>(
                        (Class<? extends S>) implClass,
                        priority,
                        singleton,
                        true
                );
                definitions.add(definition);
                objStart = arrayJson.indexOf('{', objEnd + 1);
            }
            // 排序
            definitions.sort(Comparator.comparingInt((ImplementationDefinition<S> d) -> d.priority)
                    .thenComparing(d -> d.implementationType.getName()));
            // 处理覆盖逻辑
            Map<String, ImplementationDefinition<S>> aliasMap = new LinkedHashMap<>();
            for (ImplementationDefinition<S> def : definitions) {
                String implAlias = getAlias(def.implementationType);
                SpiImpl spiImpl = def.implementationType.getAnnotation(SpiImpl.class);
                boolean isOverride = spiImpl != null && spiImpl.override();
                if (!aliasMap.containsKey(implAlias) || isOverride) {
                    aliasMap.put(implAlias, def);
                }
            }
            List<ImplementationDefinition<S>> result = new ArrayList<>(aliasMap.values());
            result.sort(Comparator.comparingInt(d -> d.priority));
            return Collections.unmodifiableList(result);
        } catch (Exception e) {
            // 元数据加载失败，降级到原来的方式
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("Failed to load SPI metadata for %s, fallback to scanning: %s",
                        serviceType.getName(), e.getMessage()));
            }
            return null;
        }
    }
    /**
     * 查找匹配的括号位置
     */
    private int findMatchingBracket(String json, int startIndex, char openBracket, char closeBracket) {
        int count = 1;
        for (int i = startIndex + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == openBracket) {
                count++;
            } else if (c == closeBracket) {
                count--;
                if (count == 0) {
                    return i;
                }
            }
        }
        return -1;
    }
    /**
     * 从JSON对象中获取字符串值
     */
    private String getJsonStringValue(String objJson, String key) {
        String keyPattern = "\"" + key + "\":";
        int keyIndex = objJson.indexOf(keyPattern);
        if (keyIndex == -1) {
            return null;
        }
        int valueStart = objJson.indexOf('"', keyIndex + keyPattern.length()) + 1;
        int valueEnd = objJson.indexOf('"', valueStart);
        if (valueStart < valueEnd) {
            return unescapeJson(objJson.substring(valueStart, valueEnd));
        }
        return null;
    }
    /**
     * 从JSON对象中获取int值
     */
    private int getJsonIntValue(String objJson, String key, int defaultValue) {
        String keyPattern = "\"" + key + "\":";
        int keyIndex = objJson.indexOf(keyPattern);
        if (keyIndex == -1) {
            return defaultValue;
        }
        int valueStart = keyIndex + keyPattern.length();
        // 跳过空白
        while (valueStart < objJson.length() && Character.isWhitespace(objJson.charAt(valueStart))) {
            valueStart++;
        }
        // 读取数字
        int valueEnd = valueStart;
        while (valueEnd < objJson.length() && (Character.isDigit(objJson.charAt(valueEnd)) || objJson.charAt(valueEnd) == '-')) {
            valueEnd++;
        }
        if (valueStart < valueEnd) {
            try {
                return Integer.parseInt(objJson.substring(valueStart, valueEnd).trim());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    /**
     * 从JSON对象中获取boolean值
     */
    private boolean getJsonBooleanValue(String objJson, String key, boolean defaultValue) {
        String keyPattern = "\"" + key + "\":";
        int keyIndex = objJson.indexOf(keyPattern);
        if (keyIndex == -1) {
            return defaultValue;
        }
        int valueStart = keyIndex + keyPattern.length();
        // 跳过空白
        while (valueStart < objJson.length() && Character.isWhitespace(objJson.charAt(valueStart))) {
            valueStart++;
        }
        // 检查是true还是false
        if (valueStart + 4 <= objJson.length() && "true".equals(objJson.substring(valueStart, valueStart + 4))) {
            return true;
        } else if (valueStart + 5 <= objJson.length() && "false".equals(objJson.substring(valueStart, valueStart + 5))) {
            return false;
        }
        return defaultValue;
    }
    /**
     * 从JSON对象中获取字符串数组
     */
    private List<String> getJsonStringArray(String objJson, String key) {
        String keyPattern = "\"" + key + "\":";
        int keyIndex = objJson.indexOf(keyPattern);
        if (keyIndex == -1) {
            return Collections.emptyList();
        }
        int arrayStart = objJson.indexOf('[', keyIndex + keyPattern.length());
        int arrayEnd = findMatchingBracket(objJson, arrayStart, '[', ']');
        if (arrayStart == -1 || arrayEnd == -1) {
            return Collections.emptyList();
        }
        String arrayJson = objJson.substring(arrayStart + 1, arrayEnd);
        List<String> values = new ArrayList<>();
        int current = 0;
        while (current < arrayJson.length()) {
            // 找字符串开始
            int strStart = arrayJson.indexOf('"', current);
            if (strStart == -1) {
                break;
            }
            int strEnd = arrayJson.indexOf('"', strStart + 1);
            if (strEnd == -1) {
                break;
            }
            values.add(unescapeJson(arrayJson.substring(strStart + 1, strEnd)));
            current = strEnd + 1;
        }
        return values;
    }
    /**
     * JSON字符串反转义
     */
    private String unescapeJson(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (escape) {
                switch (c) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        // Unicode转义，处理\\uXXXX
                        if (i + 4 < str.length()) {
                            String hex = str.substring(i + 1, i + 5);
                            try {
                                sb.append((char) Integer.parseInt(hex, 16));
                                i += 4;
                            } catch (NumberFormatException e) {
                                sb.append(c);
                            }
                        } else {
                            sb.append(c);
                        }
                        break;
                    default: sb.append(c); break;
                }
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
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
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources("META-INF/services/" + InstanceCreator.class.getName());
            Set<String> classNames = new LinkedHashSet<>();
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                try (InputStream is = resource.openStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String candidate = line.substring(0, line.indexOf('#') >= 0 ? line.indexOf('#') : line.length()).trim();
                        if (!candidate.isEmpty()) {
                            classNames.add(candidate);
                        }
                    }
                }
            }
            for (String className : classNames) {
                try {
                    Class<?> clazz = Class.forName(className, true, classLoader);
                    if (InstanceCreator.class.isAssignableFrom(clazz)) {
                        Constructor<?> constructor = clazz.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        creators.add((InstanceCreator) constructor.newInstance());
                    }
                } catch (Exception e) {
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        LOGGER.warning(String.format(
                                "Failed to load InstanceCreator implementation %s: %s. Skipping.",
                                className, e.getMessage()));
                    }
                }
            }
            // Sort by priority
            creators.sort((a, b) -> {
                int priorityA = getInstanceCreatorPriority(a.getClass());
                int priorityB = getInstanceCreatorPriority(b.getClass());
                return Integer.compare(priorityA, priorityB);
            });
        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.warning("Failed to load InstanceCreator configuration: " + e.getMessage());
            }
        }
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
        }
        private static <S> ImplementationDefinition<S> disabled(Class<? extends S> implementationType) {
            return new ImplementationDefinition<>(implementationType, Integer.MAX_VALUE, true, false);
        }
        private S getInstance() {
            if (!enabled) {
                throw new IllegalStateException("Disabled SPI implementation should not be instantiated: "
                        + implementationType.getName());
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
            // Try custom instance creators first
            for (InstanceCreator creator : INSTANCE_CREATORS) {
                try {
                    S instance = creator.createInstance(implementationType);
                    if (instance != null) {
                        return instance;
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
                return constructor.newInstance();
            } catch (Exception ex) {
                throw new SpiInstantiationException(
                        implementationType.getEnclosingClass(),
                        implementationType,
                        "Failed to instantiate SPI implementation",
                        ex
                );
            }
        }
    }
}
