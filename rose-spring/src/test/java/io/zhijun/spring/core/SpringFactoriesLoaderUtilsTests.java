package io.zhijun.spring.core;

import io.zhijun.spring.core.io.SpringFactoriesLoaderUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContextInitializer;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpringFactoriesLoaderUtilsTests {

    @Test
    void shouldLoadFactoriesByType() {
        List<ApplicationContextInitializer> factories =
                SpringFactoriesLoaderUtils.loadFactories(ApplicationContextInitializer.class, getClass().getClassLoader());
        assertThat(factories).isNotEmpty();
    }

    @Test
    void shouldLoadFactoriesByBeanFactory() {
        ConfigurableListableBeanFactory beanFactory = mock(ConfigurableListableBeanFactory.class);
        when(beanFactory.getBeanClassLoader()).thenReturn(getClass().getClassLoader());

        List<ApplicationContextInitializer> factories =
                SpringFactoriesLoaderUtils.loadFactories(beanFactory, ApplicationContextInitializer.class);
        assertThat(factories).isNotEmpty();
    }

    @Test
    void shouldLoadFactoryClasses() {
        Set<Class<ApplicationContextInitializer>> classes =
                SpringFactoriesLoaderUtils.loadFactoryClasses(ApplicationContextInitializer.class, getClass().getClassLoader());
        assertThat(classes).isNotEmpty();
    }

    @Test
    void shouldReturnEmptyListForUnknownType() {
        List<String> factories = SpringFactoriesLoaderUtils.loadFactories(String.class, getClass().getClassLoader());
        assertThat(factories).isEmpty();
    }
}
