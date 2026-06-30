package io.zhijun.spring.beans.factory;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Set;

public interface InjectionPointDependencyResolver {

    void resolve(Field field, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames);

    void resolve(Method method, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames);

    void resolve(Constructor<?> constructor, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames);

    void resolve(Parameter parameter, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames);
}
