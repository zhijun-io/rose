package io.zhijun.spring.core.env;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class PropertySourcesUtilsTests {

    @Test
    void shouldExtractSubPropertiesByPrefix() {
        MockEnvironment environment = new MockEnvironment();
        MutablePropertySources propertySources = environment.getPropertySources();
        propertySources.addFirst(new MapPropertySource("test", java.util.Collections.<String, Object>singletonMap(
                "app.name", "rose")));

        Map<String, Object> subProperties = PropertySourcesUtils.getSubProperties(environment, "app");

        assertThat(subProperties).containsEntry("name", "rose");
    }

    @Test
    void shouldNormalizePrefixWithTrailingDot() {
        assertThat(PropertySourcesUtils.normalizePrefix("app")).isEqualTo("app.");
        assertThat(PropertySourcesUtils.normalizePrefix("app.")).isEqualTo("app.");
    }
}
