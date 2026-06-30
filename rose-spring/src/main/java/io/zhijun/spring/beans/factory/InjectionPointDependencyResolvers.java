package io.zhijun.spring.beans.factory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.lang.reflect.AnnotatedElement;
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
    public void resolve(AnnotatedElement element, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        resolvers.forEach(r -> r.resolve(element, beanFactory, dependentBeanNames));
    }
}
