package io.zhijun.spring.core.beans.factory.filter;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 判断一个类型是否为 Spring 可解析依赖类型的过滤器。
 *
 * <p>可解析依赖类型包括 {@code BeanFactory}、{@code ApplicationContext}、{@code Environment}
 * 等通过 {@link DefaultListableBeanFactory#registerResolvableDependency(Class, Object)} 注册的类型。</p>
 */
public class ResolvableDependencyTypeFilter implements Predicate<Class<?>> {

    private final Set<Class<?>> resolvableDependencyTypes;

    /**
     * 从 {@link DefaultListableBeanFactory} 构造，通过反射读取内部的可解析依赖类型集合。
     */
    public ResolvableDependencyTypeFilter(ConfigurableListableBeanFactory beanFactory) {
        this.resolvableDependencyTypes = resolveResolvableDependencyTypes(beanFactory);
    }

    /**
     * 使用给定的可解析依赖类型集合构造。
     */
    public ResolvableDependencyTypeFilter(Set<Class<?>> resolvableDependencyTypes) {
        this.resolvableDependencyTypes = resolvableDependencyTypes;
    }

    @Override
    public boolean test(Class<?> type) {
        for (Class<?> resolvableType : resolvableDependencyTypes) {
            if (resolvableType.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static Set<Class<?>> resolveResolvableDependencyTypes(ConfigurableListableBeanFactory beanFactory) {
        if (!(beanFactory instanceof DefaultListableBeanFactory)) {
            return Collections.emptySet();
        }
        try {
            Field field = DefaultListableBeanFactory.class.getDeclaredField("resolvableDependencies");
            field.setAccessible(true);
            Map<Class<?>, Object> map = (Map<Class<?>, Object>) field.get(beanFactory);
            return map.keySet();
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }
}
