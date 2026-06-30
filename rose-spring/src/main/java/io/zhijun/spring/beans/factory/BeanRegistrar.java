package io.zhijun.spring.beans.factory;

import io.zhijun.core.annotation.Nullable;
import io.zhijun.spring.beans.BeanDefinitionUtils;
import io.zhijun.spring.beans.GenericBeanNameGenerator;
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

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

/**
 * Bean 注册工具类。
 * 提供在 Spring {@link BeanDefinitionRegistry} 中注册 Bean 定义的静态方法。
 */
public abstract class BeanRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(BeanRegistrar.class);

    private static final BeanNameGenerator DEFAULT_BEAN_NAME_GENERATOR = GenericBeanNameGenerator.INSTANCE;

    // ========== 基础设施 Bean ==========

    public static boolean registerInfrastructureBean(BeanDefinitionRegistry registry, @Nullable String beanName, Class<?> beanType) {
        return registerBeanDefinition(registry, beanName, beanType, builder -> builder.setRole(ROLE_INFRASTRUCTURE));
    }

    // ========== Bean 定义注册 ==========

    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, @Nullable String beanName, Class<?> beanType) {
        BeanDefinitionBuilder builder = BeanDefinitionUtils.createBeanDefinitionBuilder(beanType);
        return registerBeanDefinition(registry, beanName, builder.getBeanDefinition());
    }

    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, @Nullable String beanName,
                                                 Class<?> beanType, Consumer<BeanDefinitionBuilder> builderConsumer) {
        BeanDefinitionBuilder builder = BeanDefinitionUtils.createBeanDefinitionBuilder(beanType);
        builderConsumer.accept(builder);
        return registerBeanDefinition(registry, beanName, builder.getBeanDefinition());
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
