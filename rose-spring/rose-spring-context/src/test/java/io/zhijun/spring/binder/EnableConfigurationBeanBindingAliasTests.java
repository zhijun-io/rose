package io.zhijun.spring.binder;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.zhijun.spring.binder.EnableConfigurationBeanBinding;

@EnableConfigurationBeanBinding(
        prefix = "users",
        type = User.class,
        multiple = true,
        ignoreUnknownFields = false,
        ignoreInvalidFields = false)
class EnableConfigurationBeanBindingAliasTests extends AbstractEnableConfigurationBeanBindingTests {

    @Test
    void shouldRegisterAliasesForMultipleBeans() {
        assertAliases("a", Arrays.asList("UserA"));
        assertAliases("b", Arrays.asList("UserB"));
    }

    private void assertAliases(String beanName, List<String> aliases) {
        assertThat(context.containsBeanDefinition(beanName)).isTrue();
        assertThat(context.containsBean(beanName)).isTrue();
        User user = context.getBean(beanName, User.class);
        for (String alias : aliases) {
            assertThat(context.containsBeanDefinition(alias)).isFalse();
            assertThat(context.containsBean(alias)).isTrue();
            assertThat(context.getBean(alias, User.class)).isEqualTo(user);
        }
    }
}
