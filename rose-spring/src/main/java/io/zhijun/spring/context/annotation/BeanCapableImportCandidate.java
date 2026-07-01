package io.zhijun.spring.context.annotation;

import io.zhijun.core.annotation.Nullable;
import io.zhijun.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.*;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 支持完整 Spring Bean 生命周期的 {@code @Import} 候选基类。
 * <p>
 * 相比直接实现 {@link ImportSelector} 或 {@link ImportBeanDefinitionRegistrar}，
 * 本基类确保实例获得完整的 Aware 回调和 Bean 初始化（populate、post-process、init-method）。
 * <p>
 * <b>子类必须同时实现 {@link ImportSelector} 或 {@link ImportBeanDefinitionRegistrar} 之一</b>。
 *
 * @see ImportSelector
 * @see ImportBeanDefinitionRegistrar
 */
public abstract class BeanCapableImportCandidate implements BeanClassLoaderAware, BeanFactoryAware,
        EnvironmentAware, ApplicationContextAware, ResourceLoaderAware {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected ClassLoader classLoader;

    protected ConfigurableListableBeanFactory beanFactory;

    protected BeanDefinitionRegistry registry;

    protected ConfigurableApplicationContext applicationContext;

    protected ConfigurableEnvironment environment;

    protected ResourceLoader resourceLoader;

    // ---- Aware 回调 ----

    @Override
    public final void setBeanClassLoader(ClassLoader classLoader) {
        if (this.classLoader == null) {
            this.classLoader = classLoader;
        }
    }

    @Override
    public final void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (this.beanFactory == null && beanFactory instanceof ConfigurableListableBeanFactory) {
            ConfigurableListableBeanFactory clbf = (ConfigurableListableBeanFactory) beanFactory;
            this.beanFactory = clbf;
            if (clbf instanceof BeanDefinitionRegistry) {
                this.registry = (BeanDefinitionRegistry) clbf;
            }
        }
    }

    @Override
    public final void setEnvironment(Environment environment) {
        if (this.environment == null && environment instanceof ConfigurableEnvironment) {
            this.environment = (ConfigurableEnvironment) environment;
        }
    }

    @Override
    public final void setResourceLoader(ResourceLoader resourceLoader) {
        if (this.resourceLoader == null) {
            this.resourceLoader = resourceLoader;
        }
    }

    @Override
    public final void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (this.applicationContext == null && applicationContext instanceof ConfigurableApplicationContext) {
            this.applicationContext = (ConfigurableApplicationContext) applicationContext;
            initializeSelfAsBean();
        }
    }

    // ---- Getter ----

    public final ClassLoader getClassLoader() {
        return classLoader;
    }

    public final ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    public final BeanDefinitionRegistry getBeanDefinitionRegistry() {
        return registry;
    }

    public final ConfigurableApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public final ConfigurableEnvironment getEnvironment() {
        return environment;
    }

    public final ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    // ---- 注解属性处理 ----

    /**
     * 获取指定注解的占位符已解析的注解属性，含 {@link OverrideAnnotationAttributes} 覆盖。
     */
    protected <A extends Annotation> ResolvablePlaceholderAnnotationAttributes<A> getAnnotationAttributes(
            AnnotationMetadata metadata, Class<A> annotationType) {
        String annotationClassName = annotationType.getName();
        Map<String, Object> raw = metadata.getAnnotationAttributes(annotationClassName);
        AnnotationAttributes overridden = getOverriddenAnnotationAttributes(raw, annotationType, metadata);
        return ResolvablePlaceholderAnnotationAttributes.of(overridden, annotationType, getEnvironment());
    }

    /**
     * 检查 {@link OverrideAnnotationAttributes} 元注解并应用策略。
     */
    @SuppressWarnings("deprecation")
    protected AnnotationAttributes getOverriddenAnnotationAttributes(
            @Nullable Map<String, Object> annotationAttributes,
            Class<? extends Annotation> annotationType,
            AnnotationMetadata metadata) {
        OverrideAnnotationAttributes overrideMeta = annotationType.getAnnotation(OverrideAnnotationAttributes.class);
        AnnotationAttributes original = annotationAttributes != null
                ? new AnnotationAttributes(annotationAttributes)
                : new AnnotationAttributes(0);
        if (overrideMeta == null) {
            return original;
        }
        Class<? extends OverrideAnnotationAttributesStrategy> strategyClass = overrideMeta.strategy();
        try {
            OverrideAnnotationAttributesStrategy strategy = strategyClass.newInstance();
            if (strategy instanceof EnvironmentAware) {
                ((EnvironmentAware) strategy).setEnvironment(environment);
            }
            if (strategy instanceof ApplicationContextAware) {
                ((ApplicationContextAware) strategy).setApplicationContext(applicationContext);
            }
            AnnotationAttributes result = strategy.override(original, annotationType, metadata);
            return result != null ? result : original;
        } catch (Exception e) {
            logger.warn("注解属性覆盖策略 [{}] 实例化失败，使用原始属性", strategyClass, e);
            return original;
        }
    }

    // ---- 自注册 ----

    private void initializeSelfAsBean() {
        if (this.beanFactory == null || this.registry == null) {
            logger.warn("BeanFactory 未就绪，跳过自注册");
            return;
        }
        String beanName = getClass().getName() + "@" + Integer.toHexString(hashCode());
        if (registry.containsBeanDefinition(beanName)) {
            return;
        }
        GenericBeanDefinition bd = new GenericBeanDefinition();
        bd.setBeanClass(getClass());
        bd.setInstanceSupplier(() -> this);
        registry.registerBeanDefinition(beanName, bd);
        // 触发初始化（post-processors, init-method 等）
        beanFactory.getBean(beanName);
    }
}
