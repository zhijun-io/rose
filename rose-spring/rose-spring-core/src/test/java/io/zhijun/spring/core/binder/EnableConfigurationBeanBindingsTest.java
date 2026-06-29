package io.zhijun.spring.core.binder;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;

import io.zhijun.spring.core.binder.annotation.EnableConfigurationBeanBinding;
import io.zhijun.spring.core.binder.annotation.EnableConfigurationBeanBindings;

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
