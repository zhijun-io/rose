package io.zhijun.spring.core.binder;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;

import io.zhijun.spring.core.binder.annotation.ConfigurationBeanBindingPostProcessor;
import io.zhijun.spring.core.binder.annotation.EnableConfigurationBeanBinding;
import io.zhijun.spring.core.binder.config.ConfigurationBeanBinder;

@EnableConfigurationBeanBinding(
        prefix = "users",
        type = User.class,
        multiple = true,
        ignoreUnknownFields = false,
        ignoreInvalidFields = false)
class EnableConfigurationBeanBindingMultipleTest extends AbstractEnableConfigurationBeanBindingTest {

    private User aUser;

    private User bUser;

    @Bean
    ConfigurationBeanBindingPostProcessor configurationBeanBindingPostProcessor() {
        ConfigurationBeanBindingPostProcessor processor = new ConfigurationBeanBindingPostProcessor();
        processor.setConfigurationBeanBinder(new ConfigurationBeanBinder());
        return processor;
    }

    @BeforeEach
    void loadUsers() {
        aUser = context.getBean("a", User.class);
        bUser = context.getBean("b", User.class);
    }

    @Test
    void shouldBindMultipleConfigurationBeans() {
        Collection<User> users = context.getBeansOfType(User.class).values();
        assertThat(users).hasSize(2).contains(aUser, bUser);
        assertThat(aUser.getName()).isEqualTo("name-a");
        assertThat(aUser.getAge()).isEqualTo(1);
        assertThat(bUser.getName()).isEqualTo("name-b");
        assertThat(bUser.getAge()).isEqualTo(2);
        assertThat(context.getBean(
                                ConfigurationBeanBindingPostProcessor.BEAN_NAME,
                                ConfigurationBeanBindingPostProcessor.class)
                        .getConfigurationBeanBinder())
                .isNotNull();
    }
}
