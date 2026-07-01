package io.zhijun.spring.beans.factory.support;

import io.zhijun.core.annotation.Nullable;
import io.zhijun.spring.beans.factory.BeanFactoryUtils;
import io.zhijun.spring.beans.factory.config.BeanDefinitionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static io.zhijun.spring.beans.factory.config.BeanDefinitionUtils.genericBeanDefinition;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

/**
 * 在 Spring {@link BeanDefinitionRegistry} 中注册 Bean 定义的静态工具类。
 *
 * <h3>设计说明</h3>
 * <ul>
 *   <li><b>只接受 {@link BeanDefinitionRegistry}，不接受 {@code BeanFactory}：</b>
 *   之前的版本提供了两组重载——一组接受 {@code BeanDefinitionRegistry}，另一组接受
 *   {@code BeanFactory}（内部调用 {@code asBeanDefinitionRegistry()} 转换）。
 *   但 {@code BeanFactory} 重载未被外部调用，且造成 API 膨胀（10 个额外方法）。
 *   调用方应使用 {@link BeanFactoryUtils#asBeanDefinitionRegistry(org.springframework.beans.factory.BeanFactory)}
 *   显式转换。</li>
 *   <li><b>核心入口：</b>{@link #registerBeanDefinition(BeanDefinitionRegistry, String, BeanDefinition, boolean)}
 *   是唯一真正执行注册的方法（含 {@code allowOverriding} 控制），其余重载都委派给它。</li>
 *   <li><b>类型注册：</b>传入 {@code Class<?>} 的方法自动创建 {@link org.springframework.beans.factory.support.BeanDefinitionBuilder}，
 *   支持通过 {@link java.util.function.Consumer} 定制 builder（如设置 role/primary 等）。</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 从 BeanFactory 环境注册
 * BeanDefinitionRegistry registry = BeanFactoryUtils.asBeanDefinitionRegistry(beanFactory);
 *
 * // 按类型注册，自动生成 bean name
 * BeanRegistrar.registerBeanDefinition(registry, MyService.class);
 *
 * // 指定 bean name 注册
 * BeanRegistrar.registerBeanDefinition(registry, "myService", MyService.class);
 *
 * // 基础设施 Bean（ROLE_INFRASTRUCTURE）
 * BeanRegistrar.registerInfrastructureBean(registry, "internalProcessor", MyProcessor.class);
 *
 * // 定制 BeanDefinition
 * BeanRegistrar.registerBeanDefinition(registry, "customBean", MyBean.class,
 *         builder -> builder.setPrimary(true));
 *
 * // 注册已有实例
 * BeanRegistrar.registerBean(registry, "existingBean", existingObject);
 *
 * // 批量注册，自动生成 bean name
 * Map<Class<?>, String> beans = BeanRegistrar.registerGenericBeans(registry,
 *         Arrays.asList(ServiceA.class, ServiceB.class));
 * }</pre>
 */
public abstract class BeanRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(BeanRegistrar.class);

    private static final BeanNameGenerator DEFAULT_BEAN_NAME_GENERATOR = GenericBeanNameGenerator.INSTANCE;

    // ========== 基础设施 Bean ==========

    /**
     * Registers an infrastructure BeanDefinition ({@code ROLE_INFRASTRUCTURE}).
     *
     * @param registry Bean definition registry
     * @param beanName Bean name, auto-generated if {@code null}
     * @param beanType Bean class type
     * @return {@code true} if registered, {@code false} if skipped
     */
    public static boolean registerInfrastructureBean(BeanDefinitionRegistry registry, @Nullable String beanName, Class<?> beanType) {
        return registerBeanDefinition(registry, beanName, beanType, builder -> builder.setRole(ROLE_INFRASTRUCTURE));
    }

    // ========== Bean 定义注册 ==========

    /**
     * Registers a bean by type with consumer-based {@link BeanDefinitionBuilder} customization.
     * <p>
     * Convenience for simple builder modifications such as setting role or primary,
     * without constructing a full {@link BeanDefinition} manually.
     *
     * @param registry        Bean definition registry
     * @param beanName        Bean name, auto-generated if {@code null}
     * @param beanType        Bean class type
     * @param builderConsumer Callback to customize the {@link BeanDefinitionBuilder}
     * @return {@code true} if registered, {@code false} if skipped
     */
    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, @Nullable String beanName,
                                                 Class<?> beanType, Consumer<BeanDefinitionBuilder> builderConsumer) {
        BeanDefinitionBuilder builder = BeanDefinitionUtils.createBeanDefinitionBuilder(beanType);
        builderConsumer.accept(builder);
        return registerBeanDefinition(registry, beanName, builder.getBeanDefinition());
    }

    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, @Nullable String beanName, Class<?> beanType) {
        BeanDefinition beanDefinition = genericBeanDefinition(beanType);
        return registerBeanDefinition(registry, beanName, beanDefinition);
    }


    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, Class<?> beanType) {
        return registerBeanDefinition(registry, null, beanType);
    }

    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, BeanDefinition beanDefinition) {
        return registerBeanDefinition(registry, null, beanDefinition);
    }

    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, @Nullable String beanName,
                                                 BeanDefinition beanDefinition) {
        return registerBeanDefinition(registry, beanName, beanDefinition, true);
    }

    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, @Nullable String beanName,
                                                 BeanDefinition beanDefinition, boolean allowOverriding) {
        String actualBeanName = beanName != null ? beanName : generateBeanName(beanDefinition, registry);
        if (!allowOverriding && registry.containsBeanDefinition(actualBeanName)) {
            if (logger.isWarnEnabled()) {
                logger.warn("BeanDefinition [name: '{}'] already exists, skipping registration", actualBeanName);
            }
            return false;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Registering BeanDefinition [name: '{}'] : {}", actualBeanName, beanDefinition);
        }
        registry.registerBeanDefinition(actualBeanName, beanDefinition);
        return true;
    }

    // ========== Singleton Bean ==========

    /**
     * Registers an existing singleton bean instance.
     * <p>
     * Skips silently if a singleton with the same name already exists.
     *
     * @param registry Singleton bean registry
     * @param beanName Bean name
     * @param bean     Bean instance
     */
    public static void registerSingleton(SingletonBeanRegistry registry, String beanName, Object bean) {
        if (registry.containsSingleton(beanName)) {
            if (logger.isWarnEnabled()) {
                logger.warn("Singleton bean [name: '{}'] already exists, skipping registration", beanName);
            }
            return;
        }
        registry.registerSingleton(beanName, bean);
    }

    // ========== 批量注册 ==========

    /**
     * Registers multiple bean types with auto-generated names.
     * <p>
     * Each type gets a generic BeanDefinition, and the returned map
     * provides the type-to-name mapping for subsequent lookups.
     *
     * @param registry    Bean definition registry
     * @param beanClasses Collection of bean class types
     * @return Unmodifiable map from type to auto-generated bean name
     */
    public static Map<Class<?>, String> registerGenericBeans(BeanDefinitionRegistry registry, Collection<Class<?>> beanClasses) {
        if (beanClasses == null || beanClasses.isEmpty()) {
            return emptyMap();
        }
        Map<Class<?>, String> beanTypesAndNames = new LinkedHashMap<>(beanClasses.size());
        for (Class<?> beanClass : beanClasses) {
            String beanName = registerGenericBean(registry, beanClass);
            beanTypesAndNames.put(beanClass, beanName);
        }
        return unmodifiableMap(beanTypesAndNames);
    }

    public static String registerGenericBean(BeanDefinitionRegistry registry, Class<?> beanClass) {
        return registerGenericBean(registry, beanClass, DEFAULT_BEAN_NAME_GENERATOR);
    }

    public static String registerGenericBean(BeanDefinitionRegistry registry, Class<?> beanClass,
                                             BeanNameGenerator beanNameGenerator) {
        AbstractBeanDefinition beanDefinition = BeanDefinitionUtils.createBeanDefinitionBuilder(beanClass).getBeanDefinition();
        String beanName = beanNameGenerator.generateBeanName(beanDefinition, registry);
        registerBeanDefinition(registry, beanName, beanDefinition);
        return beanName;
    }

    /**
     * Registers multiple bean types (varargs) with auto-generated names.
     *
     * @param registry    Bean definition registry
     * @param beanClasses Bean class types
     * @return Unmodifiable map from type to auto-generated bean name
     */
    public static Map<Class<?>, String> registerGenericBeans(BeanDefinitionRegistry registry, Class<?>... beanClasses) {
        if (beanClasses == null || beanClasses.length == 0) {
            return emptyMap();
        }
        Map<Class<?>, String> beanTypesAndNames = new LinkedHashMap<>(beanClasses.length);
        for (Class<?> beanClass : beanClasses) {
            String beanName = registerGenericBean(registry, beanClass);
            beanTypesAndNames.put(beanClass, beanName);
        }
        return unmodifiableMap(beanTypesAndNames);
    }

    // ========== 实例 Bean 注册 ==========

    /**
     * 注册一个已存在的 Bean 实例到容器。
     * 该方法包装实例为 RootBeanDefinition，注册后可通过 getBean 触发初始化回调。
     */
    public static void registerBean(BeanDefinitionRegistry registry, String beanName, Object bean) {
        org.springframework.beans.factory.support.RootBeanDefinition beanDefinition =
                new org.springframework.beans.factory.support.RootBeanDefinition(bean.getClass());
        beanDefinition.setInstanceSupplier(() -> bean);
        beanDefinition.setAutowireMode(org.springframework.beans.factory.support.AbstractBeanDefinition.AUTOWIRE_BY_NAME);
        registerBeanDefinition(registry, beanName, beanDefinition);
    }

    private static String generateBeanName(BeanDefinition beanDefinition, BeanDefinitionRegistry registry) {
        return DEFAULT_BEAN_NAME_GENERATOR.generateBeanName(beanDefinition, registry);
    }

    private BeanRegistrar() {
    }
}
