package io.zhijun.spring.core.io.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * Utilities for Spring factories loading.
 */
public abstract class SpringFactoriesLoaderUtils {

    private static final Logger logger = LoggerFactory.getLogger(SpringFactoriesLoaderUtils.class);

    private SpringFactoriesLoaderUtils() {
    }

    public static <T> List<String> loadFactoryNames(Class<T> factoryType) {
        return loadFactoryNames(factoryType, null);
    }

    public static <T> List<String> loadFactoryNames(Class<T> factoryType, ClassLoader classLoader) {
        ClassLoader resolvedClassLoader = resolveClassLoader(classLoader);
        List<String> factoryNames = SpringFactoriesLoader.loadFactoryNames(factoryType, resolvedClassLoader);
        if (logger.isTraceEnabled()) {
            logger.trace("Loaded factory names for {}: {}", factoryType.getName(), factoryNames);
        }
        return factoryNames;
    }

    public static <T> List<Class<? extends T>> loadFactoryClasses(Class<T> factoryType) {
        return loadFactoryClasses(factoryType, null);
    }

    public static <T> List<Class<? extends T>> loadFactoryClasses(Class<T> factoryType, ClassLoader classLoader) {
        List<String> factoryNames = loadFactoryNames(factoryType, classLoader);
        List<Class<? extends T>> factoryClasses = new ArrayList<Class<? extends T>>(factoryNames.size());
        ClassLoader resolvedClassLoader = resolveClassLoader(classLoader);
        for (String factoryName : factoryNames) {
            try {
                Class<?> factoryClass = Class.forName(factoryName, false, resolvedClassLoader);
                factoryClasses.add((Class<? extends T>) factoryClass);
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException("Failed to load factory class " + factoryName, ex);
            }
        }
        return factoryClasses;
    }

    public static <T> List<T> loadFactories(Class<T> factoryType) {
        return loadFactories(factoryType, (ClassLoader) null);
    }

    public static <T> List<T> loadFactories(Class<T> factoryType, ClassLoader classLoader) {
        ClassLoader resolvedClassLoader = resolveClassLoader(classLoader);
        List<T> factories = new ArrayList<T>(SpringFactoriesLoader.loadFactories(factoryType, resolvedClassLoader));
        sort(factories);
        return Collections.unmodifiableList(factories);
    }

    public static <T> List<T> loadFactories(ApplicationContext applicationContext, Class<T> factoryType) {
        if (applicationContext == null) {
            return loadFactories(factoryType);
        }
        return loadFactories((BeanFactory) applicationContext, factoryType);
    }

    public static <T> List<T> loadFactories(BeanFactory beanFactory, Class<T> factoryType) {
        if (beanFactory instanceof ApplicationContext) {
            return loadFactories((ApplicationContext) beanFactory, factoryType);
        }
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
