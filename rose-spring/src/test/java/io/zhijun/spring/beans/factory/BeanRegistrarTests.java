package io.zhijun.spring.beans.factory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;

import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BeanRegistrarTests {

    @Test
    void shouldRegisterBeanDefinitionWithName() {
        SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
        boolean registered = BeanRegistrar.registerBeanDefinition(registry, "myBean", new RootBeanDefinition(String.class));
        assertThat(registered).isTrue();
        assertThat(registry.containsBeanDefinition("myBean")).isTrue();
    }

    @Test
    void shouldRegisterBeanDefinitionWithConsumer() {
        SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
        BeanRegistrar.registerBeanDefinition(registry, "primaryBean", String.class,
                (org.springframework.beans.factory.support.BeanDefinitionBuilder builder) -> builder.setPrimary(true));
        BeanDefinition bd = registry.getBeanDefinition("primaryBean");
        assertThat(bd.isPrimary()).isTrue();
    }

    @Test
    void shouldRegisterInfrastructureBean() {
        SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
        BeanRegistrar.registerInfrastructureBean(registry, "infra", Integer.class);
        BeanDefinition bd = registry.getBeanDefinition("infra");
        assertThat(bd.getRole()).isEqualTo(BeanDefinition.ROLE_INFRASTRUCTURE);
    }

    @Test
    void shouldNotOverrideWhenDisallowed() {
        SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
        BeanRegistrar.registerBeanDefinition(registry, "dup", new RootBeanDefinition(String.class));
        boolean second = BeanRegistrar.registerBeanDefinition(registry, "dup",
                new RootBeanDefinition(Integer.class), false);
        assertThat(second).isFalse();
    }

    @Test
    void shouldAllowOverridingByDefault() {
        SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
        BeanRegistrar.registerBeanDefinition(registry, "dup", new RootBeanDefinition(String.class));
        boolean second = BeanRegistrar.registerBeanDefinition(registry, "dup", new RootBeanDefinition(Integer.class));
        assertThat(second).isTrue();
    }

    @Test
    void shouldRegisterGenericBeans() {
        SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
        Map<Class<?>, String> result = BeanRegistrar.registerGenericBeans(registry, Arrays.asList(String.class, Integer.class));
        assertThat(result).hasSize(2);
        assertThat(result).containsKeys(String.class, Integer.class);
        assertThat(registry.containsBeanDefinition(result.get(String.class))).isTrue();
    }

    @Test
    void shouldRegisterGenericBeansVarargs() {
        SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
        Map<Class<?>, String> result = BeanRegistrar.registerGenericBeans(registry, Double.class, Long.class);
        assertThat(result).hasSize(2);
    }

    @Test
    void shouldReturnEmptyForEmptyBeans() {
        SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
        assertThat(BeanRegistrar.registerGenericBeans(registry, Arrays.asList())).isEmpty();
        assertThat(BeanRegistrar.registerGenericBeans(registry)).isEmpty();
    }

    @Test
    void shouldRegisterBeanInstance() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        BeanRegistrar.registerBean(bf, "instance", "hello-string");
        assertThat(bf.getBean("instance")).isEqualTo("hello-string");
    }

    @Test
    void shouldRegisterSingleton() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        Object singleton = new Object();
        BeanRegistrar.registerSingleton(bf, "singleton", singleton);
        assertThat(bf.getBean("singleton")).isSameAs(singleton);
    }

    @Test
    void shouldNotOverrideExistingSingleton() {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        bf.registerSingleton("existing", "first");
        BeanRegistrar.registerSingleton(bf, "existing", "second");
        assertThat(bf.getBean("existing")).isEqualTo("first");
    }

    @Test
    void shouldRegisterGenericBeanReturnName() {
        SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
        String beanName = BeanRegistrar.registerGenericBean(registry, java.util.List.class);
        assertThat(beanName).isEqualTo("list");
        assertThat(registry.containsBeanDefinition(beanName)).isTrue();
    }

    @Test
    void shouldRegisterBeanDefinitionWithoutName() {
        SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
        boolean registered = BeanRegistrar.registerBeanDefinition(registry, new RootBeanDefinition(String.class));
        assertThat(registered).isTrue();
        assertThat(registry.getBeanDefinitionNames()).hasSize(1);
    }
}
