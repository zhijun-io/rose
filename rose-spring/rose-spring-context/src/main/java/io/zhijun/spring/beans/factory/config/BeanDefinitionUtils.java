package io.zhijun.spring.beans.factory.config;

import io.zhijun.core.annotation.Nullable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;

public abstract class BeanDefinitionUtils {

    @Nullable
    public static Class<?> resolveBeanType(BeanDefinition beanDefinition) {
        return resolveBeanType(beanDefinition, null);
    }

    @Nullable
    public static Class<?> resolveBeanType(BeanDefinition beanDefinition, @Nullable ClassLoader classLoader) {
        if (beanDefinition instanceof RootBeanDefinition) {
            ResolvableType resolvableType = beanDefinition.getResolvableType();
            Class<?> beanClass = resolvableType.resolve();
            if (beanClass != null) {
                return beanClass;
            }
        }
        String beanClassName = beanDefinition.getBeanClassName();
        if (beanClassName != null) {
            try {
                if (classLoader != null) {
                    return Class.forName(beanClassName, false, classLoader);
                }
                return Class.forName(beanClassName);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
        return null;
    }
}
