package io.zhijun.spring.beans.factory;

import io.zhijun.core.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static io.zhijun.spring.beans.factory.BeanFactoryUtils.asDefaultListableBeanFactory;

import static java.util.Collections.addAll;

public abstract class AbstractInjectionPointDependencyResolver implements InjectionPointDependencyResolver {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final ConfigurableListableBeanFactory beanFactory;

    private final Set<Class<?>> frameworkProvidedTypes;

    public AbstractInjectionPointDependencyResolver(ConfigurableListableBeanFactory beanFactory, Set<Class<?>> frameworkProvidedTypes) {
        this.beanFactory = beanFactory;
        this.frameworkProvidedTypes = frameworkProvidedTypes;
    }

    public AbstractInjectionPointDependencyResolver(ConfigurableListableBeanFactory beanFactory) {
        this(beanFactory, Collections.emptySet());
    }

    protected AbstractInjectionPointDependencyResolver() {
        this.beanFactory = null;
        this.frameworkProvidedTypes = Collections.emptySet();
    }

    public ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    public Set<Class<?>> getFrameworkProvidedTypes() {
        return frameworkProvidedTypes;
    }

    public void resolve(Field field, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        // default: no-op, subclasses can override
    }

    public void resolve(Method method, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        // default: no-op, subclasses can override
    }

    public void resolve(Constructor<?> constructor, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        // default: no-op, subclasses can override
    }

    public void resolve(Parameter parameter, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        // default: no-op, subclasses can override
    }

    @Nullable
    protected Class<?> resolveDependentType(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            List<Type> arguments = resolveActualTypeArguments(type);
            Type argumentType = arguments.get(arguments.size() - 1);
            return resolveDependentType(argumentType);
        }
        return null;
    }

    private static List<Type> resolveActualTypeArguments(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            List<Type> result = new ArrayList<>(actualTypeArguments.length);
            Collections.addAll(result, actualTypeArguments);
            return result;
        }
        return Collections.emptyList();
    }

    protected boolean isFrameworkProvidedType(Class<?> type) {
        if (frameworkProvidedTypes.isEmpty()) { return false; } for (Class<?> t : frameworkProvidedTypes) { if (t.isAssignableFrom(type)) { return true; } } return false;
    }

}
