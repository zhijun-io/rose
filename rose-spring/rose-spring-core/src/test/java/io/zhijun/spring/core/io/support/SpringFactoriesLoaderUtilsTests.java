package io.zhijun.spring.core.io.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.SpringFactoriesLoader;

import io.zhijun.spring.core.env.listener.EnvironmentListener;
import io.zhijun.spring.core.env.listener.LoggingEnvironmentListener;
import io.zhijun.spring.core.env.refresh.PropertySourcesRefreshEnvironmentListener;

class SpringFactoriesLoaderUtilsTests {

    @Test
    void shouldLoadFactoryNamesFromSpringFactories() {
        List<String> names = SpringFactoriesLoader.loadFactoryNames(
                EnvironmentListener.class, getClass().getClassLoader());

        assertThat(names).contains("io.zhijun.spring.core.env.FactoryLoadedEnvironmentListener");
    }

    @Test
    void shouldLoadFactoriesFromSpringFactories() {
        List<EnvironmentListener> listeners = SpringFactoriesLoaderUtils.loadFactories(
                EnvironmentListener.class, getClass().getClassLoader());

        assertThat(listeners).anyMatch(listener -> listener instanceof LoggingEnvironmentListener);
        assertThat(listeners)
                .anyMatch(listener -> listener instanceof io.zhijun.spring.core.env.FactoryLoadedEnvironmentListener);
        assertThat(listeners).anyMatch(listener -> listener instanceof PropertySourcesRefreshEnvironmentListener);
    }

    @Test
    void shouldRegisterPropertySourcesRefreshEnvironmentListenerInMainSpringFactories() {
        List<String> names = SpringFactoriesLoader.loadFactoryNames(
                EnvironmentListener.class, LoggingEnvironmentListener.class.getClassLoader());

        assertThat(names).contains(PropertySourcesRefreshEnvironmentListener.class.getName());
    }

    @Test
    void shouldReturnEmptyWhenFactoryNotFound() {
        List<String> names = SpringFactoriesLoader.loadFactoryNames(
                UnknownFactory.class, getClass().getClassLoader());
        List<UnknownFactory> factories = SpringFactoriesLoaderUtils.loadFactories(
                UnknownFactory.class, getClass().getClassLoader());

        assertThat(names).isEmpty();
        assertThat(factories).isEmpty();
    }

    interface UnknownFactory {}
}
