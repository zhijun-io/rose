package io.zhijun.spring.core.io.support;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpringFactoriesLoaderUtilsTests {

    @Test
    void shouldLoadFactoryNamesFromSpringFactories() {
        List<String> names = SpringFactoriesLoaderUtils.loadFactoryNames(io.zhijun.spring.core.env.EnvironmentListener.class,
                getClass().getClassLoader());

        assertThat(names).contains("io.zhijun.spring.core.env.FactoryLoadedEnvironmentListener");
    }

    @Test
    void shouldLoadFactoriesFromSpringFactories() {
        List<io.zhijun.spring.core.env.EnvironmentListener> listeners = SpringFactoriesLoaderUtils
                .loadFactories(io.zhijun.spring.core.env.EnvironmentListener.class, getClass().getClassLoader());

        assertThat(listeners).anyMatch(listener -> listener instanceof io.zhijun.spring.core.env.LoggingEnvironmentListener);
        assertThat(listeners).anyMatch(listener -> listener instanceof io.zhijun.spring.core.env.FactoryLoadedEnvironmentListener);
    }

    @Test
    void shouldReturnEmptyWhenFactoryNotFound() {
        List<String> names = SpringFactoriesLoaderUtils.loadFactoryNames(UnknownFactory.class, getClass().getClassLoader());
        List<UnknownFactory> factories = SpringFactoriesLoaderUtils.loadFactories(UnknownFactory.class,
                getClass().getClassLoader());

        assertThat(names).isEmpty();
        assertThat(factories).isEmpty();
    }

    interface UnknownFactory {
    }
}
