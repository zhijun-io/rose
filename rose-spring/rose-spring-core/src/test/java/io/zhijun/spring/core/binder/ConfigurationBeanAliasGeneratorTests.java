package io.zhijun.spring.core.binder;

import static org.assertj.core.api.Assertions.assertThat;

import io.zhijun.spring.core.binder.support.DefaultConfigurationBeanAliasGenerator;
import io.zhijun.spring.core.binder.support.HyphenDelimitedAliasGenerator;
import io.zhijun.spring.core.binder.support.UnderscoreDelimitedAliasGenerator;
import org.junit.jupiter.api.Test;

class ConfigurationBeanAliasGeneratorTests {

    @Test
    void defaultGeneratorUsesSimpleClassNameAndCapitalizedBeanName() {
        DefaultConfigurationBeanAliasGenerator generator = new DefaultConfigurationBeanAliasGenerator();

        assertThat(generator.generateAlias("users", "u0", UserProperties.class)).isEqualTo("UserPropertiesU0");
    }

    @Test
    void hyphenGeneratorJoinsPrefixSegmentsAndBeanName() {
        HyphenDelimitedAliasGenerator generator = new HyphenDelimitedAliasGenerator();

        assertThat(generator.generateAlias("rose.app", "primary", UserProperties.class))
                .isEqualTo("roseApp-primary");
    }

    @Test
    void underscoreGeneratorUsesUnderscoreDelimiter() {
        UnderscoreDelimitedAliasGenerator generator = new UnderscoreDelimitedAliasGenerator();

        assertThat(generator.generateAlias("rose.app", "primary", UserProperties.class))
                .isEqualTo("roseApp_primary");
    }

    static class UserProperties {}
}
