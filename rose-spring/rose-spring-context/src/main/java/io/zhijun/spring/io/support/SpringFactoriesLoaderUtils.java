package io.zhijun.spring.io.support;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class SpringFactoriesLoaderUtils {

    public static <T> List<T> loadFactories(Class<T> type, ClassLoader classLoader) {
        return SpringFactoriesLoader.loadFactories(type, classLoader);
    }

    public static <T> List<T> loadFactories(ConfigurableListableBeanFactory beanFactory, Class<T> type) {
        ClassLoader classLoader = beanFactory.getBeanClassLoader();
        return SpringFactoriesLoader.loadFactories(type, classLoader);
    }

    public static <T> Set<Class<T>> loadFactoryClasses(Class<T> type, ClassLoader classLoader) {
        List<String> classNames = SpringFactoriesLoader.loadFactoryNames(type, classLoader);
        Set<Class<T>> classes = new LinkedHashSet<>();
        for (String className : classNames) {
            try {
                classes.add((Class<T>) Class.forName(className, false, classLoader));
            } catch (ClassNotFoundException e) {
                // skip
            }
        }
        return classes;
    }
}
