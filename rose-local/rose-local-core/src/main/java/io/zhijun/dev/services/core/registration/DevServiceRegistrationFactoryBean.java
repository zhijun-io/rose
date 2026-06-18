package io.zhijun.dev.services.core.registration;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.lang.Nullable;
import org.testcontainers.containers.Container;

import io.zhijun.dev.services.api.registration.DevServiceRegistration;

/**
 * Creates {@link DevServiceRegistration} after the container bean is available (Boot 2.7 compatible).
 */
final class DevServiceRegistrationFactoryBean
        implements FactoryBean<DevServiceRegistration>, BeanFactoryAware {

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
    public DevServiceRegistration getObject() {
        Container<?> container = beanFactory.getBean(containerBeanName, Container.class);
        final String containerId = container.getContainerId();
        return new DevServiceRegistration(name, description, new java.util.function.Supplier<io.zhijun.dev.services.api.registration.ContainerInfo>() {
            @Override
            public io.zhijun.dev.services.api.registration.ContainerInfo get() {
                return DevServicesRegistry.extractContainerInfoById(containerId);
            }
        });
    }

    @Override
    public Class<?> getObjectType() {
        return DevServiceRegistration.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
