package io.zhijun.spring.beans;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContextInitializer;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BeanSourceTests {

    @Test
    void beanFactorySourceShouldGetBeanTypesFromFactory() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        bf.registerSingleton("str", "hello");
        Set<Class<String>> types = BeanSource.BEAN_FACTORY.getBeanTypes(bf, String.class);
        assertThat(types).contains(String.class);
    }

    @Test
    void springFactoriesSourceShouldLoadFactoryClasses() {
        ConfigurableListableBeanFactory bf = mock(ConfigurableListableBeanFactory.class);
        when(bf.getBeanClassLoader()).thenReturn(getClass().getClassLoader());
        Set<Class<ApplicationContextInitializer>> types = BeanSource.SPRING_FACTORIES.getBeanTypes(bf, ApplicationContextInitializer.class);
        assertThat(types).isNotEmpty();
    }

    @Test
    void javaServiceProviderSourceShouldLoadSpiClasses() {
        ConfigurableListableBeanFactory bf = mock(ConfigurableListableBeanFactory.class);
        when(bf.getBeanClassLoader()).thenReturn(getClass().getClassLoader());
        Set<Class<ServiceLoader>> types = BeanSource.JAVA_SERVICE_PROVIDER.getBeanTypes(bf, ServiceLoader.class);
        assertThat(types).isNotNull();
    }

    @Test
    void registerBeansWithEmptyVarargsShouldReturnEmpty() {
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        assertThat(BeanSource.BEAN_FACTORY.registerBeans((BeanDefinitionRegistry) registry)).isEmpty();
    }

    @Test
    void registerBeansWithMultipleSourcesShouldAggregate() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        bf.registerSingleton("str", "hello");
        Map<Class<?>, String> result = BeanSource.registerBeans(
                (ConfigurableListableBeanFactory) bf,
                new BeanSource[]{BeanSource.BEAN_FACTORY}, String.class);
        assertThat(result).isNotEmpty();
    }

    @Test
    void registerBeansWithEmptyBeanTypesShouldReturnEmpty() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        Map<Class<?>, String> result = BeanSource.BEAN_FACTORY.registerBeans(
                (BeanDefinitionRegistry) bf);
        assertThat(result).isEmpty();
    }
}
