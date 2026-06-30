package io.zhijun.spring.beans;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class BeanDefinitionUtilsTests {

    @Test
    void shouldResolveBeanTypeFromRootBeanDefinition() {
        RootBeanDefinition bd = new RootBeanDefinition(String.class);
        assertThat(BeanDefinitionUtils.resolveBeanType(bd)).isEqualTo(String.class);
    }

    @Test
    void shouldResolveBeanTypeFromClassName() {
        BeanDefinition bd = BeanDefinitionBuilder.genericBeanDefinition("java.lang.Integer").getBeanDefinition();
        assertThat(BeanDefinitionUtils.resolveBeanType(bd)).isEqualTo(Integer.class);
    }

    @Test
    void shouldReturnNullForUnknownClassName() {
        BeanDefinition bd = BeanDefinitionBuilder.genericBeanDefinition("com.example.Unknown").getBeanDefinition();
        assertThat(BeanDefinitionUtils.resolveBeanType(bd)).isNull();
    }

    @Test
    void shouldResolveBeanTypeWithClassLoader() {
        RootBeanDefinition bd = new RootBeanDefinition(Long.class);
        Class<?> type = BeanDefinitionUtils.resolveBeanType(bd, getClass().getClassLoader());
        assertThat(type).isEqualTo(Long.class);
    }

    @Test
    void shouldReturnNullForNonClassType() {
        BeanDefinition bd = BeanDefinitionBuilder.genericBeanDefinition((String) null).getBeanDefinition();
        assertThat(BeanDefinitionUtils.resolveBeanType(bd)).isNull();
    }

    @Test
    void shouldSetBeanDefinitionClassWithConsumer() {
        BeanDefinition bd = BeanDefinitionBuilder.genericBeanDefinition((String) null).getBeanDefinition();
        BeanDefinitionUtils.setBeanDefinitionClass(bd, Integer.class.getName(),
                abd -> abd.setPrimary(true));
        assertThat(bd.getBeanClassName()).isEqualTo(Integer.class.getName());
        assertThat(bd instanceof AbstractBeanDefinition).isTrue();
    }

    @Test
    void shouldSetBeanDefinitionClassWithoutConsumer() {
        BeanDefinition bd = BeanDefinitionBuilder.genericBeanDefinition((String) null).getBeanDefinition();
        BeanDefinitionUtils.setBeanDefinitionClass(bd, Double.class.getName(), null);
        assertThat(bd.getBeanClassName()).isEqualTo(Double.class.getName());
    }

    @Test
    void shouldCreateBuilderFromClassName() {
        BeanDefinitionBuilder builder = BeanDefinitionUtils.createBeanDefinitionBuilder("java.util.List");
        assertThat(builder).isNotNull();
    }

    @Test
    void shouldCreateBuilderFromClassType() {
        BeanDefinitionBuilder builder = BeanDefinitionUtils.createBeanDefinitionBuilder(java.util.Map.class);
        assertThat(builder).isNotNull();
    }
}
