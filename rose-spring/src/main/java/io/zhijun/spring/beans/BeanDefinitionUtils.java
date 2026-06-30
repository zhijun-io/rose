package io.zhijun.spring.beans;

import io.zhijun.core.annotation.Nullable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;

import java.util.function.Consumer;

public abstract class BeanDefinitionUtils {

    @Nullable
    public static Class<?> resolveBeanType(BeanDefinition beanDefinition) {
        return resolveBeanType(beanDefinition, null);
    }

    @Nullable
    public static Class<?> resolveBeanType(BeanDefinition beanDefinition, @Nullable ClassLoader classLoader) {
        Class<?> beanClass = resolveBeanTypeFromRootBeanDefinition(beanDefinition);
        if (beanClass != null) {
            return beanClass;
        }
        String beanClassName = beanDefinition.getBeanClassName();
        if (beanClassName != null) {
            try {
                if (classLoader != null) {
                    return Class.forName(beanClassName, false, classLoader);
                }
                return Class.forName(beanClassName);
            } catch (ClassNotFoundException | LinkageError e) {
                return null;
            }
        }
        return null;
    }

    @Nullable
    private static Class<?> resolveBeanTypeFromRootBeanDefinition(BeanDefinition beanDefinition) {
        if (beanDefinition instanceof RootBeanDefinition) {
            ResolvableType resolvableType = beanDefinition.getResolvableType();
            if (resolvableType != null) {
                return resolvableType.resolve();
            }
        }
        return null;
    }

    public static void setBeanDefinitionClass(BeanDefinition beanDefinition, String beanClassName,
                                               @Nullable Consumer<AbstractBeanDefinition> abstractBeanDefinitionConsumer) {
        beanDefinition.setBeanClassName(beanClassName);
        if (beanDefinition instanceof AbstractBeanDefinition && abstractBeanDefinitionConsumer != null) {
            abstractBeanDefinitionConsumer.accept((AbstractBeanDefinition) beanDefinition);
        }
    }

    public static BeanDefinitionBuilder createBeanDefinitionBuilder(String beanClassName) {
        return BeanDefinitionBuilder.genericBeanDefinition(beanClassName);
    }

    public static BeanDefinitionBuilder createBeanDefinitionBuilder(Class<?> beanType) {
        return BeanDefinitionBuilder.genericBeanDefinition(beanType);
    }
}
