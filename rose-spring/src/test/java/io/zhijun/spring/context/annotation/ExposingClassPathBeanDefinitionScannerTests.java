package io.zhijun.spring.context.annotation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ExposingClassPathBeanDefinitionScannerTests {

    @Test
    void shouldCreateScannerWithDefaults() {
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        ExposingClassPathBeanDefinitionScanner scanner = new ExposingClassPathBeanDefinitionScanner(
                registry, true,
                new org.springframework.mock.env.MockEnvironment(),
                new DefaultResourceLoader());
        assertThat(scanner).isNotNull();
    }

    @Test
    void shouldCheckCandidate() {
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        ExposingClassPathBeanDefinitionScanner scanner = new ExposingClassPathBeanDefinitionScanner(
                registry, true,
                new org.springframework.mock.env.MockEnvironment(),
                new DefaultResourceLoader());
        boolean result = scanner.checkCandidate("nonExistent",
                new org.springframework.beans.factory.support.RootBeanDefinition(String.class));
        assertThat(result).isTrue();
    }

    @Test
    void shouldProvideSingletonBeanRegistry() {
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        ExposingClassPathBeanDefinitionScanner scanner = new ExposingClassPathBeanDefinitionScanner(
                registry, true,
                new org.springframework.mock.env.MockEnvironment(),
                new DefaultResourceLoader());
        assertThat(scanner.getSingletonBeanRegistry()).isNotNull();
    }

    @Test
    void shouldRegisterSingleton() {
        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();
        ExposingClassPathBeanDefinitionScanner scanner = new ExposingClassPathBeanDefinitionScanner(
                registry, true,
                new org.springframework.mock.env.MockEnvironment(),
                new DefaultResourceLoader());
        scanner.registerSingleton("testSingleton", "singletonValue");
        assertThat(registry.containsSingleton("testSingleton")).isTrue();
    }
}
