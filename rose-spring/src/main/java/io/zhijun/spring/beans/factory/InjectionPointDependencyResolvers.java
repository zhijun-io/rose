package io.zhijun.spring.beans.factory;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InjectionPointDependencyResolvers implements InjectionPointDependencyResolver {

    private final List<InjectionPointDependencyResolver> resolvers;

    public InjectionPointDependencyResolvers(ListableBeanFactory beanFactory) {
        this.resolvers = new ArrayList<>();
        this.resolvers.addAll(beanFactory.getBeansOfType(InjectionPointDependencyResolver.class).values());
    }

    @Override
    public void resolve(AnnotatedElement element, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        resolvers.forEach(r -> r.resolve(element, beanFactory, dependentBeanNames));
    }
}
