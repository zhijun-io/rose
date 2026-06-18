package io.zhijun.spring.beans.factory.annotation;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

@EnableConfigurationBeanBindings(@EnableConfigurationBeanBinding(prefix = "usr", type = User.class))
@Configuration
class EnableConfigurationBeanBindingsTest extends AbstractEnableConfigurationBeanBindingTest {

    @Test
    void shouldBindViaContainerAnnotation() {
        User user = context.getBean("m", User.class);
        assertThat(user.getName()).isEqualTo("mercyblitz");
        assertThat(user.getAge()).isEqualTo(34);
    }
}
