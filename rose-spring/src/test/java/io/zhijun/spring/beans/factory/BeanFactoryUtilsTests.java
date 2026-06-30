package io.zhijun.spring.beans.factory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BeanFactoryUtilsTests {

    @Test
    void shouldExtractBeanDefinitionRegistry() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        BeanDefinitionRegistry registry = BeanFactoryUtils.asBeanDefinitionRegistry(bf);
        assertThat(registry).isSameAs(bf);
    }

    @Test
    void shouldReturnNullForNullInput() {
        assertThat(BeanFactoryUtils.asBeanDefinitionRegistry(null)).isNull();
        assertThat(BeanFactoryUtils.asConfigurableListableBeanFactory(null)).isNull();
        assertThat(BeanFactoryUtils.asDefaultListableBeanFactory(null)).isNull();
        assertThat(BeanFactoryUtils.asListableBeanFactory(null)).isNull();
        assertThat(BeanFactoryUtils.asConfigurableBeanFactory(null)).isNull();
    }

    @Test
    void shouldThrowForNonRegistry() {
        assertThatThrownBy(() -> BeanFactoryUtils.asBeanDefinitionRegistry(new Object()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldExtractConfigurableListableBeanFactory() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        ConfigurableListableBeanFactory clbf = BeanFactoryUtils.asConfigurableListableBeanFactory(bf);
        assertThat(clbf).isSameAs(bf);
    }

    @Test
    void shouldThrowForNonConfigurableListable() {
        assertThatThrownBy(() -> BeanFactoryUtils.asConfigurableListableBeanFactory(new Object()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowForNonDefaultListable() {
        assertThatThrownBy(() -> BeanFactoryUtils.asDefaultListableBeanFactory(new Object()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldGetBeanClassFromSingleton() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        bf.registerSingleton("myBean", "hello");
        Class<?> type = BeanFactoryUtils.getBeanClass(bf, "myBean");
        assertThat(type).isEqualTo(String.class);
    }

    @Test
    void shouldGetBeanClassFromDefinition() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        bf.registerBeanDefinition("num", new RootBeanDefinition(Integer.class));
        Class<?> type = BeanFactoryUtils.getBeanClass(bf, "num");
        assertThat(type).isEqualTo(Integer.class);
    }

    @Test
    void shouldReturnNullForNonExistentBean() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        assertThat(BeanFactoryUtils.getBeanClass(bf, "nonexistent")).isNull();
    }

    @Test
    void shouldGetBeanDefinition() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        bf.registerBeanDefinition("test", new RootBeanDefinition(String.class));

        ConfigurableListableBeanFactory clbf = bf;
        assertThat(BeanFactoryUtils.getBeanDefinition(clbf, "test")).isNotNull();
        assertThat(BeanFactoryUtils.getBeanDefinition(clbf, "missing")).isNull();
    }

    @Test
    void shouldGetBeanDefinitionFromRegistry() {
        SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
        registry.registerBeanDefinition("foo", new RootBeanDefinition(Integer.class));

        assertThat(BeanFactoryUtils.getBeanDefinition(registry, "foo")).isNotNull();
        assertThat(BeanFactoryUtils.getBeanDefinition(registry, "bar")).isNull();
    }

    @Test
    void shouldGetResolvableDependencyTypes() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        assertThat(BeanFactoryUtils.getResolvableDependencyTypes(bf)).isEmpty();
    }

    @Test
    void shouldGetBeanTypes() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        bf.registerBeanDefinition("runnable", new RootBeanDefinition(Runnable.class));
        Set<Class<Runnable>> types = BeanFactoryUtils.getBeanTypes(bf, Runnable.class, true, false);
        assertThat(types).isNotEmpty();
    }

    @Test
    void shouldExtractDefaultListableBeanFactory() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        assertThat(BeanFactoryUtils.asDefaultListableBeanFactory(bf)).isSameAs(bf);
    }

    @Test
    void shouldReturnNullForNullCast() {
        assertThat(BeanFactoryUtils.asBeanDefinitionRegistry(null)).isNull();
    }
}
