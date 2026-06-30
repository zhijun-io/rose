package io.zhijun.spring.beans.factory.filter;

import io.zhijun.spring.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.Set;

import static io.zhijun.spring.beans.factory.BeanFactoryUtils.asDefaultListableBeanFactory;
import static io.zhijun.spring.beans.factory.BeanFactoryUtils.getResolvableDependencyTypes;

/**
 * 可解析依赖类型过滤器。
 * 判断某个类是否属于 Spring 容器可解析的依赖类型（如 BeanFactory、ApplicationContext 等）。
 */
public class ResolvableDependencyTypeFilter {

    public static final String BEAN_NAME = "resolvableDependencyTypeFilter";

    private final Set<Class<?>> resolvableDependencyTypes;

    public ResolvableDependencyTypeFilter(ConfigurableListableBeanFactory beanFactory) {
        this(asDefaultListableBeanFactory(beanFactory));
    }

    public ResolvableDependencyTypeFilter(DefaultListableBeanFactory beanFactory) {
        this.resolvableDependencyTypes = getResolvableDependencyTypes(beanFactory);
    }

    public boolean accept(Class<?> classToFilter) {
        for (Class<?> resolvableDependencyType : resolvableDependencyTypes) {
            if (resolvableDependencyType.isAssignableFrom(classToFilter)) {
                return true;
            }
        }
        return false;
    }

    public static ResolvableDependencyTypeFilter get(BeanFactory beanFactory) {
        return get(asDefaultListableBeanFactory(beanFactory));
    }

    static ResolvableDependencyTypeFilter get(DefaultListableBeanFactory beanFactory) {
        Object singleton = beanFactory.getSingleton(BEAN_NAME);
        if (singleton instanceof ResolvableDependencyTypeFilter) {
            return (ResolvableDependencyTypeFilter) singleton;
        }
        ResolvableDependencyTypeFilter filter = new ResolvableDependencyTypeFilter(beanFactory);
        if (beanFactory instanceof SingletonBeanRegistry) {
            ((SingletonBeanRegistry) beanFactory).registerSingleton(BEAN_NAME, filter);
        }
        return filter;
    }

    @Override
    public String toString() {
        return "ResolvableDependencyTypeFilter{" +
                "resolvableDependencyTypes=" + resolvableDependencyTypes +
                '}';
    }
}
