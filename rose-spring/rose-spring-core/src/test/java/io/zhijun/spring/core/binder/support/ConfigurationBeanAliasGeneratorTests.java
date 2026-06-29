package io.zhijun.spring.core.binder.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationBeanAliasGeneratorTests {

    @Test
    void defaultGeneratorUsesSimpleClassNameAndCapitalizedBeanName() {
        DefaultConfigurationBeanAliasGenerator generator = new DefaultConfigurationBeanAliasGenerator();

        assertThat(generator.generateAlias("users", "u0", UserProperties.class)).isEqualTo("UserPropertiesU0");
    }

    @Test
    void hyphenGeneratorJoinsPrefixSegmentsAndBeanName() {
        DelimitedAliasGenerator.Hyphen generator = new DelimitedAliasGenerator.Hyphen();

        assertThat(generator.generateAlias("rose.app", "primary", UserProperties.class)).isEqualTo("roseApp-primary");
    }

    @Test
    void underscoreGeneratorUsesUnderscoreDelimiter() {
        DelimitedAliasGenerator.Underscore generator = new DelimitedAliasGenerator.Underscore();

        assertThat(generator.generateAlias("rose.app", "primary", UserProperties.class)).isEqualTo("roseApp_primary");
    }

    static class UserProperties {
    }
}
