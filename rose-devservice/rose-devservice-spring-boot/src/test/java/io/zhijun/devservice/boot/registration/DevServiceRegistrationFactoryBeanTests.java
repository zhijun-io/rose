package io.zhijun.devservice.boot.registration;

import io.zhijun.devservice.core.api.registration.DevServiceRegistration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.testcontainers.containers.Container;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link DevServiceRegistrationFactoryBean}.
 */
class DevServiceRegistrationFactoryBeanTests {

    @Test
    void exposesFactoryBeanMetadata() {
        DevServiceRegistrationFactoryBean factoryBean = new DevServiceRegistrationFactoryBean();

        assertThat(factoryBean.getObjectType()).isEqualTo(DevServiceRegistration.class);
        assertThat(factoryBean.isSingleton()).isTrue();
    }

    @Test
    void createsRegistrationFromContainerBean() {
        Container<?> container = mock(Container.class);
        when(container.getContainerId()).thenReturn("container-id");

        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("devService.container.redis", container);

        DevServiceRegistrationFactoryBean factoryBean = new DevServiceRegistrationFactoryBean();
        factoryBean.setBeanFactory(beanFactory);
        factoryBean.setName("redis");
        factoryBean.setDescription("Redis");
        factoryBean.setContainerBeanName("devService.container.redis");

        DevServiceRegistration registration = factoryBean.getObject();

        assertThat(registration.getName()).isEqualTo("redis");
        assertThat(registration.getDescription()).isEqualTo("Redis");
        assertThat(registration.getContainerInfo()).isNotNull();
    }
}
