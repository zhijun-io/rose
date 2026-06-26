package io.zhijun.spring.core.binder.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationBeanBindingSupportTests {

    @Test
    void isConfigurationBeanDefinitionReturnsTrueWhenSourceMatches() {
        GenericBeanDefinition definition = new GenericBeanDefinition();
        definition.setSource(ConfigurationBeanBindingSupport.CONFIGURATION_BEAN_SOURCE);

        assertThat(ConfigurationBeanBindingSupport.isConfigurationBeanDefinition(definition)).isTrue();
    }

    @Test
    void isConfigurationBeanDefinitionReturnsFalseForNullOrOtherSource() {
        assertThat(ConfigurationBeanBindingSupport.isConfigurationBeanDefinition(null)).isFalse();

        GenericBeanDefinition definition = new GenericBeanDefinition();
        definition.setSource(String.class);
        assertThat(ConfigurationBeanBindingSupport.isConfigurationBeanDefinition(definition)).isFalse();
    }

    @Test
    void prefixAffectedReturnsFalseForNullOrEmptyInputs() {
        assertThat(ConfigurationBeanBindingSupport.prefixAffected(null,
                Collections.singleton("app.name"))).isFalse();
        assertThat(ConfigurationBeanBindingSupport.prefixAffected("app", null)).isFalse();
        assertThat(ConfigurationBeanBindingSupport.prefixAffected("app", Collections.<String>emptySet())).isFalse();
    }

    @Test
    void prefixAffectedMatchesExactPrefixAndNestedKeys() {
        Set<String> changedKeys = new HashSet<String>(Arrays.asList("app", "app.name", "app.server.port", "other.key"));

        assertThat(ConfigurationBeanBindingSupport.prefixAffected("app", changedKeys)).isTrue();
        assertThat(ConfigurationBeanBindingSupport.prefixAffected("other", changedKeys)).isTrue();
        assertThat(ConfigurationBeanBindingSupport.prefixAffected("application", changedKeys)).isFalse();
    }

    @Test
    void resolveSubPropertiesReturnsFullMapWhenMultipleIsFalse() {
        Map<String, Object> configurationProperties = new HashMap<String, Object>();
        configurationProperties.put("name", "rose");
        configurationProperties.put("age", 18);

        Map<String, Object> resolved = ConfigurationBeanBindingSupport.resolveSubProperties(false, "ignored",
                configurationProperties, new MockEnvironment());

        assertThat(resolved).isSameAs(configurationProperties);
    }

    @Test
    void resolveSubPropertiesExtractsSubtreeWhenMultipleIsTrue() {
        Map<String, Object> configurationProperties = new LinkedHashMap<>();
        configurationProperties.put("u0.name", "Mercy");
        configurationProperties.put("u0.age", 18);
        configurationProperties.put("u1.name", "Ma");

        Map<String, Object> resolved = ConfigurationBeanBindingSupport.resolveSubProperties(true, "u0",
                configurationProperties, new MockEnvironment());

        assertThat(resolved).containsEntry("name", "Mercy").containsEntry("age", 18);
        assertThat(resolved).doesNotContainKey("u1.name");
    }

    @Test
    void resolveBindingPropertiesReadsPrefixFromEnvironment() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("app.name", "rose");
        environment.setProperty("app.enabled", "true");

        Map<String, Object> resolved = ConfigurationBeanBindingSupport.resolveBindingProperties(environment, "app",
                false, "ignored");

        assertThat(resolved).containsEntry("name", "rose").containsEntry("enabled", "true");
    }
}
