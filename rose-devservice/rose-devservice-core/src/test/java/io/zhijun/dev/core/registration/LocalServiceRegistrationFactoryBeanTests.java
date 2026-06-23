package io.zhijun.dev.core.registration;

import io.zhijun.dev.core.registration.LocalServiceRegistrationFactoryBean;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.testcontainers.containers.Container;

import io.zhijun.dev.api.registration.LocalServiceRegistration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LocalServiceRegistrationFactoryBean}.
 */
class LocalServiceRegistrationFactoryBeanTests {

    @Test
    void exposesFactoryBeanMetadata() {
        LocalServiceRegistrationFactoryBean factoryBean = new LocalServiceRegistrationFactoryBean();

        assertThat(factoryBean.getObjectType()).isEqualTo(LocalServiceRegistration.class);
        assertThat(factoryBean.isSingleton()).isTrue();
    }

    @Test
    void createsRegistrationFromContainerBean() {
        Container<?> container = mock(Container.class);
        when(container.getContainerId()).thenReturn("container-id");

        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("devService.container.redis", container);

        LocalServiceRegistrationFactoryBean factoryBean = new LocalServiceRegistrationFactoryBean();
        factoryBean.setBeanFactory(beanFactory);
        factoryBean.setName("redis");
        factoryBean.setDescription("Redis");
        factoryBean.setContainerBeanName("devService.container.redis");

        LocalServiceRegistration registration = factoryBean.getObject();

        assertThat(registration.getName()).isEqualTo("redis");
        assertThat(registration.getDescription()).isEqualTo("Redis");
        assertThat(registration.getContainerInfo()).isNotNull();
    }

}
