package io.zhijun.spring.core.binder;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DefaultConfigurationBeanAliasGeneratorTests {

    @Test
    void defaultGeneratorUsesSimpleClassNameAndCapitalizedBeanName() {
        DefaultConfigurationBeanAliasGenerator generator = new DefaultConfigurationBeanAliasGenerator();

        assertThat(generator.generateAlias("users", "u0", UserProperties.class)).isEqualTo("UserPropertiesU0");
    }

    static class UserProperties {}
}
