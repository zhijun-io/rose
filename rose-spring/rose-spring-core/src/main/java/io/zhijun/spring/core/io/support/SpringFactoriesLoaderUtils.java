package io.zhijun.spring.core.io.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * Utilities for Spring factories loading.
 */
public abstract class SpringFactoriesLoaderUtils {

    private SpringFactoriesLoaderUtils() {}

    public static <T> List<T> loadFactories(Class<T> factoryType) {
        return loadFactories(factoryType, (ClassLoader) null);
    }

    public static <T> List<T> loadFactories(Class<T> factoryType, ClassLoader classLoader) {
        ClassLoader resolvedClassLoader = resolveClassLoader(classLoader);
        List<T> factories = new ArrayList<T>(SpringFactoriesLoader.loadFactories(factoryType, resolvedClassLoader));
        sort(factories);
        return Collections.unmodifiableList(factories);
    }

    public static <T> List<T> loadFactories(BeanFactory beanFactory, Class<T> factoryType) {
        ClassLoader classLoader = beanFactory instanceof ConfigurableBeanFactory
                ? ((ConfigurableBeanFactory) beanFactory).getBeanClassLoader()
                : resolveClassLoader(null);
        List<T> factories = new ArrayList<T>(SpringFactoriesLoader.loadFactories(factoryType, classLoader));
        sort(factories);
        return Collections.unmodifiableList(factories);
    }

    private static ClassLoader resolveClassLoader(ClassLoader classLoader) {
        if (classLoader != null) {
            return classLoader;
        }
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            return contextClassLoader;
        }
        return SpringFactoriesLoaderUtils.class.getClassLoader();
    }

    private static void sort(List<?> factories) {
        if (factories.size() > 1) {
            AnnotationAwareOrderComparator.sort(factories);
        }
    }
}
