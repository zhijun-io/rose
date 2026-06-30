package io.zhijun.spring.core.binder;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;

import io.zhijun.spring.core.binder.EnableConfigurationBeanBinding;
import io.zhijun.spring.core.binder.EnableConfigurationBeanBindings;

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
