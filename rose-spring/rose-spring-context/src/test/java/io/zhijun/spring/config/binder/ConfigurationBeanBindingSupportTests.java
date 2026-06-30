package io.zhijun.spring.config.binder;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.env.StandardEnvironment;

class ConfigurationBeanBindingSupportTests {

    @Test
    void isConfigurationBeanDefinitionReturnsTrueWhenSourceMatches() {
        RootBeanDefinition definition = new RootBeanDefinition(Object.class);
        definition.setSource(ConfigurationBeanBindingSupport.CONFIGURATION_BEAN_SOURCE);

        assertThat(ConfigurationBeanBindingSupport.isConfigurationBeanDefinition(definition)).isTrue();
    }

    @Test
    void isConfigurationBeanDefinitionReturnsFalseForNull() {
        assertThat(ConfigurationBeanBindingSupport.isConfigurationBeanDefinition(null)).isFalse();
    }

    @Test
    void isConfigurationBeanDefinitionReturnsFalseWhenSourceDiffers() {
        RootBeanDefinition definition = new RootBeanDefinition(Object.class);
        definition.setSource("other");

        assertThat(ConfigurationBeanBindingSupport.isConfigurationBeanDefinition(definition)).isFalse();
    }

    @Test
    void prefixAffectedReturnsFalseWhenInputsMissing() {
        assertThat(ConfigurationBeanBindingSupport.prefixAffected(null, Collections.singleton("app.name"))).isFalse();
        assertThat(ConfigurationBeanBindingSupport.prefixAffected("app", null)).isFalse();
        assertThat(ConfigurationBeanBindingSupport.prefixAffected("app", Collections.<String>emptySet())).isFalse();
    }

    @Test
    void prefixAffectedMatchesExactAndNestedKeysOnly() {
        Set<String> changedKeys = new java.util.LinkedHashSet<String>();
        changedKeys.add("app");
        changedKeys.add("app.name");
        changedKeys.add("application.name");

        assertThat(ConfigurationBeanBindingSupport.prefixAffected("app", changedKeys)).isTrue();
        assertThat(ConfigurationBeanBindingSupport.prefixAffected("other", changedKeys)).isFalse();
        assertThat(ConfigurationBeanBindingSupport.prefixAffected("application", changedKeys)).isTrue();
        assertThat(ConfigurationBeanBindingSupport.prefixAffected("db", changedKeys)).isFalse();
    }

    @Test
    void resolveSubPropertiesReturnsOriginalMapForSingleBeanBinding() {
        Map<String, Object> properties = new LinkedHashMap<String, Object>();
        properties.put("name", "rose");

        Map<String, Object> resolved = ConfigurationBeanBindingSupport.resolveSubProperties(
                false, "u0", properties, new StandardEnvironment());

        assertThat(resolved).isSameAs(properties);
    }

    @Test
    void resolveSubPropertiesExtractsNamedSubtreeForMultipleBinding() {
        Map<String, Object> properties = new LinkedHashMap<String, Object>();
        properties.put("u0.name", "rose");
        properties.put("u0.age", "18");
        properties.put("u1.name", "jack");

        Map<String, Object> resolved = ConfigurationBeanBindingSupport.resolveSubProperties(
                true, "u0", properties, new StandardEnvironment());

        assertThat(resolved).containsEntry("name", "rose").containsEntry("age", "18").hasSize(2);
    }

    @Test
    void resolveBindingPropertiesReadsCurrentEnvironmentValues() {
        StandardEnvironment environment = new StandardEnvironment();
        Map<String, Object> source = new LinkedHashMap<String, Object>();
        source.put("app.name", "rose");
        source.put("app.age", "18");
        source.put("users.u0.name", "jack");
        source.put("users.u0.age", "20");
        environment.getPropertySources()
                .addFirst(new org.springframework.core.env.MapPropertySource("test", source));

        Map<String, Object> single =
                ConfigurationBeanBindingSupport.resolveBindingProperties(environment, "app", false, "ignored");
        Map<String, Object> multiple =
                ConfigurationBeanBindingSupport.resolveBindingProperties(environment, "users", true, "u0");

        assertThat(single).containsEntry("name", "rose").containsEntry("age", "18");
        assertThat(multiple).containsEntry("name", "jack").containsEntry("age", "20");
    }
}
