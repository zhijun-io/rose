package io.zhijun.spring.config.binder;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.env.MockEnvironment;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationBeanBindingSupportTests {

    @Test
    void shouldIdentifyConfigurationBeanDefinition() {
        RootBeanDefinition bd = new RootBeanDefinition();
        bd.setSource(ConfigurationBeanBindingSupport.CONFIGURATION_BEAN_SOURCE);
        assertThat(ConfigurationBeanBindingSupport.isConfigurationBeanDefinition(bd)).isTrue();
    }

    @Test
    void shouldReturnFalseForNonConfigurationBeanDefinition() {
        RootBeanDefinition bd = new RootBeanDefinition();
        assertThat(ConfigurationBeanBindingSupport.isConfigurationBeanDefinition(bd)).isFalse();
    }

    @Test
    void shouldReturnFalseForNullBeanDefinition() {
        assertThat(ConfigurationBeanBindingSupport.isConfigurationBeanDefinition(null)).isFalse();
    }

    @Test
    void prefixAffectedShouldReturnTrueWhenPrefixMatches() {
        Set<String> keys = new LinkedHashSet<>();
        keys.add("spring.datasource.url");
        assertThat(ConfigurationBeanBindingSupport.prefixAffected("spring.datasource", keys)).isTrue();
    }

    @Test
    void prefixAffectedShouldReturnTrueWhenExactMatch() {
        Set<String> keys = new LinkedHashSet<>();
        keys.add("spring.datasource");
        assertThat(ConfigurationBeanBindingSupport.prefixAffected("spring.datasource", keys)).isTrue();
    }

    @Test
    void prefixAffectedShouldReturnFalseForDifferentPrefix() {
        Set<String> keys = new LinkedHashSet<>();
        keys.add("other.key");
        assertThat(ConfigurationBeanBindingSupport.prefixAffected("spring.datasource", keys)).isFalse();
    }

    @Test
    void prefixAffectedShouldReturnFalseForNullInputs() {
        assertThat(ConfigurationBeanBindingSupport.prefixAffected(null, new LinkedHashSet<>())).isFalse();
        assertThat(ConfigurationBeanBindingSupport.prefixAffected("prefix", null)).isFalse();
        assertThat(ConfigurationBeanBindingSupport.prefixAffected("prefix", new LinkedHashSet<>())).isFalse();
    }

    @Test
    void resolveSubPropertiesShouldReturnFullMapWhenNotMultiple() {
        Map<String, Object> props = new HashMap<>();
        props.put("key1", "val1");
        props.put("key2", "val2");
        ConfigurableEnvironment env = new MockEnvironment();
        assertThat(ConfigurationBeanBindingSupport.resolveSubProperties(false, "bean", props, env))
                .isSameAs(props);
    }

    @Test
    void resolveBindingPropertiesShouldReturnMatchingProperties() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.datasource.url", "jdbc:test");
        env.setProperty("spring.datasource.driver", "com.TestDriver");
        env.setProperty("other.key", "other");
        Map<String, Object> result = ConfigurationBeanBindingSupport.resolveBindingProperties(
                env, "spring.datasource.", false, "bean");
        assertThat(result).containsOnlyKeys("url", "driver");
        assertThat(result).containsEntry("url", "jdbc:test");
    }
}
