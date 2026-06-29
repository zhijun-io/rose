package io.zhijun.spring.core.binder;

import io.zhijun.spring.core.binder.annotation.EnableConfigurationBeanBinding;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;

import io.zhijun.spring.core.binder.config.ConfigurationBeanBinder;
import io.zhijun.spring.core.binder.config.ConfigurationBeanCustomizer;

import static org.assertj.core.api.Assertions.assertThat;

@EnableConfigurationBeanBinding(prefix = "usr", type = User.class)
class EnableConfigurationBeanBindingTest extends AbstractEnableConfigurationBeanBindingTest {

    @Bean
    ConfigurationBeanCustomizer customizer() {
        return new ConfigurationBeanCustomizer() {
            @Override
            public int getOrder() {
                return 0;
            }

            @Override
            public void customize(String beanName, Object configurationBean) {
                if ("m".equals(beanName) && configurationBean instanceof User) {
                    ((User) configurationBean).setAge(19);
                }
            }
        };
    }

    @Bean
    ConfigurationBeanBinder configurationBeanBinder() {
        return new ConfigurationBeanBinder();
    }

    @Test
    void shouldBindSingleConfigurationBean() {
        User user = context.getBean("m", User.class);
        assertThat(user.getName()).isEqualTo("mercyblitz");
        assertThat(user.getAge()).isEqualTo(19);
    }
}
