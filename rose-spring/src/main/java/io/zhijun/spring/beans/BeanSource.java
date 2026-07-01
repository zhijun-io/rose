package io.zhijun.spring.beans;

import io.zhijun.core.annotation.Nullable;
import io.zhijun.spring.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.util.*;

import static io.zhijun.spring.beans.factory.BeanFactoryUtils.*;
import static io.zhijun.spring.beans.factory.support.BeanRegistrar.registerGenericBeans;
import static io.zhijun.spring.core.io.SpringFactoriesLoaderUtils.loadFactoryClasses;

/**
 * Bean 来源枚举。
 *
 * @see BeanFactory
 * @see SpringFactoriesLoader
 * @see ServiceLoader
 */
public enum BeanSource {

    BEAN_FACTORY {
        @Override
        public <T> Set<Class<T>> getBeanTypes(ConfigurableListableBeanFactory beanFactory, Class<T> beanType) {
            return BeanFactoryUtils.getBeanTypes(beanFactory, beanType, true, false);
        }

        @Override
        Map<Class<?>, String> registerBeans(ConfigurableListableBeanFactory beanFactory, BeanDefinitionRegistry registry,
                                            Set<Class<?>> beanClasses) {
            Map<Class<?>, String> beanTypesAndNames = new HashMap<>(beanClasses.size());
            for (Class<?> beanClass : beanClasses) {
                String[] beanNames = beanFactory.getBeanNamesForType(beanClass, true, false);
                for (String beanName : beanNames) {
                    Class<?> beanType = getBeanClass(beanFactory, beanName);
                    beanType = beanType != null ? beanType : beanClass;
                    beanTypesAndNames.put(beanType, beanName);
                }
            }
            return beanTypesAndNames;
        }
    },

    SPRING_FACTORIES {
        @Override
        public <T> Set<Class<T>> getBeanTypes(ConfigurableListableBeanFactory beanFactory, Class<T> beanType) {
            return loadFactoryClasses(beanType, beanFactory.getBeanClassLoader());
        }
    },

    JAVA_SERVICE_PROVIDER {
        @Override
        public <T> Set<Class<T>> getBeanTypes(ConfigurableListableBeanFactory beanFactory, Class<T> beanType) {
            Set<Class<T>> types = new LinkedHashSet<>();
            ServiceLoader<T> loader = ServiceLoader.load(beanType, beanFactory.getBeanClassLoader());
            for (T impl : loader) {
                types.add((Class<T>) impl.getClass());
            }
            return Collections.unmodifiableSet(types);
        }
    };

    public abstract <T> Set<Class<T>> getBeanTypes(ConfigurableListableBeanFactory beanFactory, Class<T> beanType);

    public Map<Class<?>, String> registerBeans(BeanDefinitionRegistry registry, Class<?>... beanTypes) {
        return registerBeans(asConfigurableListableBeanFactory(registry), registry, beanTypes);
    }

    public Map<Class<?>, String> registerBeans(ConfigurableListableBeanFactory beanFactory, Class<?>... beanTypes) {
        return registerBeans(beanFactory, asBeanDefinitionRegistry(beanFactory), beanTypes);
    }

    public Map<Class<?>, String> registerBeans(ConfigurableListableBeanFactory beanFactory, BeanDefinitionRegistry registry,
                                               Class<?>... beanTypes) {
        int length = beanTypes != null ? beanTypes.length : 0;
        if (length == 0) {
            return Collections.emptyMap();
        }
        Map<Class<?>, String> beanTypesAndNames = registerBeans(beanFactory, registry, beanTypes, length);
        return Collections.unmodifiableMap(beanTypesAndNames);
    }

    Map<Class<?>, String> registerBeans(ConfigurableListableBeanFactory beanFactory, BeanDefinitionRegistry registry,
                                        Class<?>[] beanTypes, int length) {
        Map<Class<?>, String> beanTypesAndNames = new HashMap<>(length * 2);
        for (int i = 0; i < length; i++) {
            Class<?> beanType = beanTypes[i];
            beanTypesAndNames.putAll(registerBean(beanFactory, registry, beanType));
        }
        return beanTypesAndNames;
    }

    Map<Class<?>, String> registerBean(ConfigurableListableBeanFactory beanFactory, BeanDefinitionRegistry registry,
                                       Class<?> beanType) {
        Set<Class<?>> beanClasses = (Set) getBeanTypes(beanFactory, beanType);
        return registerBeans(beanFactory, registry, beanClasses);
    }

    @Nullable
    Map<Class<?>, String> registerBeans(ConfigurableListableBeanFactory beanFactory, BeanDefinitionRegistry registry,
                                        Set<Class<?>> beanClasses) {
        return registerGenericBeans(registry, beanClasses);
    }

    public static Map<Class<?>, String> registerBeans(ConfigurableListableBeanFactory beanFactory, BeanSource[] beanSources, Class<?>... beanTypes) {
        return registerBeans(beanFactory, asBeanDefinitionRegistry(beanFactory), beanSources, beanTypes);
    }

    public static Map<Class<?>, String> registerBeans(BeanDefinitionRegistry registry, BeanSource[] beanSources, Class<?>... beanTypes) {
        return registerBeans(asConfigurableListableBeanFactory(registry), registry, beanSources, beanTypes);
    }

    public static Map<Class<?>, String> registerBeans(ConfigurableListableBeanFactory beanFactory, BeanDefinitionRegistry registry,
                                                      BeanSource[] beanSources, Class<?>... beanTypes) {
        int length = beanTypes != null ? beanTypes.length : 0;
        if (length == 0) {
            return Collections.emptyMap();
        }
        Map<Class<?>, String> beanTypesAndNames = new HashMap<>(length);
        for (BeanSource beanSource : beanSources) {
            beanTypesAndNames.putAll(beanSource.registerBeans(beanFactory, registry, beanTypes));
        }
        return Collections.unmodifiableMap(beanTypesAndNames);
    }
}
