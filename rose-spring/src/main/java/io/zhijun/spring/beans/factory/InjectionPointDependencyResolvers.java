package io.zhijun.spring.beans.factory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InjectionPointDependencyResolvers implements InjectionPointDependencyResolver {

    private final List<InjectionPointDependencyResolver> resolvers;

    public InjectionPointDependencyResolvers(BeanFactory beanFactory) {
        this.resolvers = new ArrayList<>();
        if (beanFactory instanceof ListableBeanFactory) {
            this.resolvers.addAll(((ListableBeanFactory) beanFactory).getBeansOfType(InjectionPointDependencyResolver.class).values());
        }
    }

    @Override
    public void resolve(Field field, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        resolvers.forEach(r -> r.resolve(field, beanFactory, dependentBeanNames));
    }

    @Override
    public void resolve(Method method, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        resolvers.forEach(r -> r.resolve(method, beanFactory, dependentBeanNames));
    }

    @Override
    public void resolve(Constructor<?> constructor, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        resolvers.forEach(r -> r.resolve(constructor, beanFactory, dependentBeanNames));
    }

    @Override
    public void resolve(Parameter parameter, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        resolvers.forEach(r -> r.resolve(parameter, beanFactory, dependentBeanNames));
    }
}
