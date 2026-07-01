package io.zhijun.spring.beans;

import io.zhijun.spring.beans.factory.support.GenericBeanNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GenericBeanNameGeneratorTests {

    @Test
    void shouldGenerateBeanNameFromClassType() {
        BeanDefinition definition = BeanDefinitionBuilder.genericBeanDefinition(UserService.class).getBeanDefinition();
        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();

        String beanName = new GenericBeanNameGenerator().generateBeanName(definition, registry);
        assertThat(beanName).isEqualTo("userService");
    }

    @Test
    void shouldUseSingletonInstance() {
        assertThat(GenericBeanNameGenerator.INSTANCE).isNotNull();
        assertThat(GenericBeanNameGenerator.INSTANCE).isInstanceOf(GenericBeanNameGenerator.class);
    }

    @Test
    void shouldThrowWhenBeanDefinitionHasNoType() {
        BeanDefinition definition = BeanDefinitionBuilder.genericBeanDefinition((String) null).getBeanDefinition();
        definition.setBeanClassName((String) null);
        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();

        assertThatThrownBy(() -> new GenericBeanNameGenerator().generateBeanName(definition, registry))
                .isInstanceOf(IllegalArgumentException.class);
    }

    static class UserService {
    }
}
