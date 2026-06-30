package io.zhijun.spring.beans.factory;

import io.zhijun.core.annotation.Nullable;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static io.zhijun.spring.beans.BeanDefinitionUtils.resolveBeanType;
import static org.springframework.beans.factory.BeanFactoryUtils.beanNamesForTypeIncludingAncestors;
import static org.springframework.util.Assert.isInstanceOf;

public abstract class BeanFactoryUtils {

    private BeanFactoryUtils() {
    }

    @Nullable
    public static BeanDefinitionRegistry asBeanDefinitionRegistry(Object beanFactory) {
        return cast(beanFactory, BeanDefinitionRegistry.class);
    }

    @Nullable
    public static ConfigurableListableBeanFactory asConfigurableListableBeanFactory(Object beanFactory) {
        return cast(beanFactory, ConfigurableListableBeanFactory.class);
    }

    @Nullable
    public static DefaultListableBeanFactory asDefaultListableBeanFactory(Object beanFactory) {
        return cast(beanFactory, DefaultListableBeanFactory.class);
    }

    @Nullable
    public static ListableBeanFactory asListableBeanFactory(Object beanFactory) {
        return cast(beanFactory, ListableBeanFactory.class);
    }

    @Nullable
    public static ConfigurableBeanFactory asConfigurableBeanFactory(Object beanFactory) {
        return cast(beanFactory, ConfigurableBeanFactory.class);
    }

    @Nullable
    public static Class<?> getBeanClass(ConfigurableListableBeanFactory beanFactory, String beanName) {
        BeanDefinition beanDefinition = getBeanDefinition(beanFactory, beanName);
        if (beanDefinition == null) {
            Object singleton = beanFactory.getSingleton(beanName);
            return singleton != null ? singleton.getClass() : null;
        }
        return resolveBeanType(beanDefinition);
    }

    @Nullable
    public static BeanDefinition getBeanDefinition(ConfigurableListableBeanFactory beanFactory, String beanName) {
        if (beanFactory.containsBeanDefinition(beanName)) {
            return beanFactory.getBeanDefinition(beanName);
        }
        return null;
    }

    @Nullable
    public static BeanDefinition getBeanDefinition(BeanDefinitionRegistry registry, String beanName) {
        if (registry.containsBeanDefinition(beanName)) {
            return registry.getBeanDefinition(beanName);
        }
        return null;
    }

    public static Set<Class<?>> getResolvableDependencyTypes(DefaultListableBeanFactory beanFactory) {
        try {
            java.lang.reflect.Field field = DefaultListableBeanFactory.class.getDeclaredField("resolvableDependencies");
            field.setAccessible(true);
            Map<Class<?>, Object> map = (Map<Class<?>, Object>) field.get(beanFactory);
            return map != null ? Collections.unmodifiableSet(map.keySet()) : Collections.emptySet();
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    public static <T> Set<Class<T>> getBeanTypes(ListableBeanFactory beanFactory, Class<T> beanType,
                                                 boolean includeNonSingletons, boolean allowEagerInit) {
        String[] beanNames = beanFactory.getBeanNamesForType(beanType, includeNonSingletons, allowEagerInit);
        Set<Class<T>> beanTypes = new LinkedHashSet<>(beanNames.length);
        for (String beanName : beanNames) {
            Class<?> type = beanFactory.getType(beanName);
            if (type != null) {
                beanTypes.add((Class<T>) type);
            }
        }
        return Collections.unmodifiableSet(beanTypes);
    }

    @Nullable
    private static <T> T cast(@Nullable Object beanFactory, Class<T> extendedType) {
        if (beanFactory == null) {
            return null;
        }
        if (beanFactory instanceof ApplicationContext) {
            beanFactory = ((ApplicationContext) beanFactory).getAutowireCapableBeanFactory();
        }
        isInstanceOf(extendedType, beanFactory,
                "The 'beanFactory' argument is not a instance of " + extendedType);
        return extendedType.cast(beanFactory);
    }
}
