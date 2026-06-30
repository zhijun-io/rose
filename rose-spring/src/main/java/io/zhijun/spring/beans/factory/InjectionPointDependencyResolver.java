package io.zhijun.spring.beans.factory;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.lang.reflect.AnnotatedElement;
import java.util.Set;

/**
 * Resolves dependent bean names from an annotated injection point.
 */
public interface InjectionPointDependencyResolver {

    void resolve(AnnotatedElement element, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames);
}
