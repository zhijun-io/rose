package io.zhijun.spring.context.annotation;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import java.util.Set;

import static org.springframework.context.annotation.AnnotationConfigUtils.registerAnnotationConfigProcessors;

/**
 * A extension class of {@link ClassPathBeanDefinitionScanner} to expose some methods:
 * <ul>
 *     <li>{@link ClassPathBeanDefinitionScanner#doScan(String...)}</li>
 *     <li>{@link ClassPathBeanDefinitionScanner#checkCandidate(String, BeanDefinition)}</li>
 * </ul>
 * <p>
 * {@link ExposingClassPathBeanDefinitionScanner} also supports the features from {@link #getRegistry() BeanDefinitionRegistry}
 * and {@link #getSingletonBeanRegistry() SingletonBeanRegistry}
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *     // Create an instance with a BeanDefinitionRegistry
 *     ExposingClassPathBeanDefinitionScanner scanner = new ExposingClassPathBeanDefinitionScanner(registry, useDefaultFilters, environment, resourceLoader);
 *
 *     // Perform a scan to detect and register beans
 *     Set<BeanDefinitionHolder> beanDefinitions = scanner.doScan("com.example.package");
 *
 *     // Register a custom bean definition
 *     scanner.registerBeanDefinition("myBean", myBeanDefinition);
 *
 *     // Register a singleton instance
 *     scanner.registerSingleton("mySingleton", new MySingleton());
 * }</pre>
 *
 * @see ClassPathBeanDefinitionScanner
 * @see BeanDefinitionRegistry
 * @see SingletonBeanRegistry
 * @since 1.0.0
 */
public class ExposingClassPathBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {

    public ExposingClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters,
                                                  Environment environment, ResourceLoader resourceLoader) {
        super(registry, useDefaultFilters, environment);
        setResourceLoader(resourceLoader);
        registerAnnotationConfigProcessors(registry);
    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        return super.doScan(basePackages);
    }

    @Override
    public boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
        return super.checkCandidate(beanName, beanDefinition);
    }

    public SingletonBeanRegistry getSingletonBeanRegistry() {
        return (SingletonBeanRegistry) getRegistry();
    }

    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException {
        getRegistry().registerBeanDefinition(beanName, beanDefinition);
    }

    public void registerSingleton(String beanName, Object singletonObject) {
        getSingletonBeanRegistry().registerSingleton(beanName, singletonObject);
    }
}
