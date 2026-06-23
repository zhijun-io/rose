package io.zhijun.dev.core.registration;

import io.zhijun.dev.api.registration.ContainerInfo;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.lang.Nullable;
import org.testcontainers.containers.Container;

import io.zhijun.dev.api.registration.LocalServiceRegistration;

/**
 * Creates {@link LocalServiceRegistration} after the container bean is available (Boot 2.7 compatible).
 */
final class LocalServiceRegistrationFactoryBean
        implements FactoryBean<LocalServiceRegistration>, BeanFactoryAware {

    private BeanFactory beanFactory;
    private String name;
    @Nullable
    private String description;
    private String containerBeanName;

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public void setContainerBeanName(String containerBeanName) {
        this.containerBeanName = containerBeanName;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public LocalServiceRegistration getObject() {
        Container<?> container = beanFactory.getBean(containerBeanName, Container.class);
        final String containerId = container.getContainerId();
        return new LocalServiceRegistration(name, description, new java.util.function.Supplier<ContainerInfo>() {
            @Override
            public ContainerInfo get() {
                return LocalServiceRegistry.extractContainerInfoById(containerId);
            }
        });
    }

    @Override
    public Class<?> getObjectType() {
        return LocalServiceRegistration.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
