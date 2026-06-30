package io.zhijun.spring.context.annotation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;

import static org.assertj.core.api.Assertions.assertThat;

class AnnotatedBeanDefinitionRegistryUtilsTests {

    @Test
    void shouldResolveAnnotationBeanNameGenerator() {
        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
        assertThat(AnnotatedBeanDefinitionRegistryUtils.resolveAnnotatedBeanNameGenerator(registry))
                .isNotNull();
    }
}
