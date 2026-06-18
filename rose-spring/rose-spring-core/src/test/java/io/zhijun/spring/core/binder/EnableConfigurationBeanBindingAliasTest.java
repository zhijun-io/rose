package io.zhijun.spring.core.binder;

import java.util.Arrays;
import java.util.List;

import io.zhijun.spring.core.binder.annotation.EnableConfigurationBeanBinding;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@EnableConfigurationBeanBinding(prefix = "users", type = User.class, multiple = true, ignoreUnknownFields = false,
        ignoreInvalidFields = false)
class EnableConfigurationBeanBindingAliasTest extends AbstractEnableConfigurationBeanBindingTest {

    @Test
    void shouldRegisterAliasesForMultipleBeans() {
        assertAliases("a", Arrays.asList("UserA", "users-a", "users_a"));
        assertAliases("b", Arrays.asList("UserB", "users-b", "users_b"));
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
