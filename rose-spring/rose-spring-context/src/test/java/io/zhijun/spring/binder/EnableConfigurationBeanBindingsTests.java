package io.zhijun.spring.binder;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;

import io.zhijun.spring.binder.EnableConfigurationBeanBinding;
import io.zhijun.spring.binder.EnableConfigurationBeanBindings;

@EnableConfigurationBeanBindings(@EnableConfigurationBeanBinding(prefix = "usr", type = User.class))
@Configuration
class EnableConfigurationBeanBindingsTests extends AbstractEnableConfigurationBeanBindingTests {

    @Test
    void shouldBindViaContainerAnnotation() {
        User user = context.getBean("m", User.class);
        assertThat(user.getName()).isEqualTo("mercyblitz");
        assertThat(user.getAge()).isEqualTo(34);
    }
}
