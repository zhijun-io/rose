package io.zhijun.spring.core.config;

import io.zhijun.spring.core.config.annotation.ResourcePropertySource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import io.zhijun.spring.core.config.annotation.JsonPropertySource;
import io.zhijun.spring.core.config.annotation.YamlPropertySource;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;
import static org.springframework.core.env.StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME;

class ResourcePropertySourceLoaderTests {

    private static final String LOCATION_A = "classpath:/META-INF/test/a.properties";
    private static final String LOCATION_B = "classpath:/META-INF/test/b.properties";

    @Test
    void shouldLoadWildcardPropertyLocations() {
        ConfigurableEnvironment environment = start(WildcardConfig.class);

        assertThat(environment.getProperty("wildcard.a")).isEqualTo("alpha");
        assertThat(environment.getProperty("wildcard.b")).isEqualTo("beta");
    }

    @Test
    void shouldLoadMultiplePropertyLocations() {
        ConfigurableEnvironment environment = start(DefaultConfig.class);

        assertThat(environment.getProperty("a")).isEqualTo("1");
        assertThat(environment.getProperty("b")).isEqualTo("3");

        PropertySource<?> propertySource = lastPropertySource(environment);
        assertThat(propertySource.getName()).isEqualTo(DefaultConfig.class.getName() + "@"
                + ResourcePropertySource.class.getName());
        assertThat(propertySource).isInstanceOf(CompositePropertySource.class);
        assertThat(((CompositePropertySource) propertySource).getPropertySources()).hasSize(2);
    }

    @Test
    void shouldLoadNamedPropertySource() {
        ConfigurableEnvironment environment = start(NamedConfig.class);

        assertThat(environment.getProperty("a")).isEqualTo("1");
        assertThat(environment.getPropertySources().get("test-property-source")).isNotNull();
        assertThat(lastPropertySource(environment).getName()).isEqualTo("test-property-source");
    }

    @Test
    void shouldAddPropertySourceFirst() {
        ConfigurableEnvironment environment = start(FirstConfig.class);

        Iterator<PropertySource<?>> iterator = environment.getPropertySources().iterator();
        PropertySource<?> first = iterator.next();
        assertThat(first.getName()).isEqualTo(FirstConfig.class.getName() + "@"
                + ResourcePropertySource.class.getName());
        assertThat(environment.getProperty("a")).isEqualTo("1");
    }

    @Test
    void shouldAddPropertySourceBefore() {
        ConfigurableEnvironment environment = start(BeforeConfig.class);
        MutablePropertySources propertySources = environment.getPropertySources();

        assertThat(propertySources).hasSize(3);
        assertPropertySourceBefore(propertySources, SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                BeforeConfig.class.getName() + "@" + ResourcePropertySource.class.getName());
        assertThat(environment.getProperty("a")).isEqualTo("1");
    }

    @Test
    void shouldAddPropertySourceAfter() {
        ConfigurableEnvironment environment = start(AfterConfig.class);
        MutablePropertySources propertySources = environment.getPropertySources();

        assertThat(propertySources).hasSize(3);
        assertPropertySourceAfter(propertySources, SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME,
                AfterConfig.class.getName() + "@" + ResourcePropertySource.class.getName());
        assertThat(environment.getProperty("a")).isEqualTo("1");
    }

    @Test
    void shouldIgnoreMissingResourceWhenConfigured() {
        ConfigurableEnvironment environment = start(IgnoreResourceNotFoundConfig.class);

        assertThat(environment.getPropertySources()).hasSize(2);
        assertThat(environment.getPropertySources().get(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME)).isNotNull();
        assertThat(environment.getPropertySources().get(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)).isNotNull();
    }

    @Test
    void shouldFailWhenResourceMissing() {
        assertThatThrownBy(() -> start(NotFoundConfig.class))
                .isInstanceOf(BeanDefinitionStoreException.class);
    }

    @Test
    void shouldLoadYamlPropertySource() {
        ConfigurableEnvironment environment = start(YamlConfig.class);

        assertThat(environment.getProperty("app.name")).isEqualTo("rose-yaml");
    }

    @Test
    void shouldLoadJsonPropertySource() {
        ConfigurableEnvironment environment = start(JsonConfig.class);

        assertThat(environment.getProperty("app.name")).isEqualTo("rose-json");
    }

    @Test
    void shouldLoadRepeatablePropertySources() {
        ConfigurableEnvironment environment = start(RepeatableConfig.class);

        assertThat(environment.getProperty("a")).isEqualTo("1");
        assertThat(environment.getProperty("repeatable.key")).isEqualTo("value");
    }

    private static ConfigurableEnvironment start(Class<?> configClass) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(configClass);
        return context.getEnvironment();
    }

    private static PropertySource<?> lastPropertySource(ConfigurableEnvironment environment) {
        PropertySource<?> last = null;
        for (PropertySource<?> propertySource : environment.getPropertySources()) {
            last = propertySource;
        }
        return last;
    }

    private static void assertPropertySourceBefore(MutablePropertySources propertySources, String anchorName,
            String expectedName) {
        PropertySource<?> previous = null;
        for (PropertySource<?> propertySource : propertySources) {
            if (anchorName.equals(propertySource.getName())) {
                assertThat(previous).isNotNull();
                assertThat(previous.getName()).isEqualTo(expectedName);
                return;
            }
            previous = propertySource;
        }
        throw new AssertionError("Anchor property source not found: " + anchorName);
    }

    private static void assertPropertySourceAfter(MutablePropertySources propertySources, String anchorName,
            String expectedName) {
        boolean foundAnchor = false;
        for (PropertySource<?> propertySource : propertySources) {
            if (foundAnchor) {
                assertThat(propertySource.getName()).isEqualTo(expectedName);
                return;
            }
            if (anchorName.equals(propertySource.getName())) {
                foundAnchor = true;
            }
        }
        throw new AssertionError("Anchor property source not found: " + anchorName);
    }

    @Configuration
    @ResourcePropertySource("classpath*:/META-INF/wildcard/*.properties")
    static class WildcardConfig {
    }

    @Configuration
    @ResourcePropertySource(value = {LOCATION_A, LOCATION_B})
    static class DefaultConfig {
    }

    @Configuration
    @ResourcePropertySource(name = "test-property-source", value = {LOCATION_A, LOCATION_B})
    static class NamedConfig {
    }

    @Configuration
    @ResourcePropertySource(value = {LOCATION_A, LOCATION_B}, first = true)
    static class FirstConfig {
    }

    @Configuration
    @ResourcePropertySource(value = {LOCATION_A, LOCATION_B}, before = SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)
    static class BeforeConfig {
    }

    @Configuration
    @ResourcePropertySource(value = {LOCATION_A, LOCATION_B}, after = SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME)
    static class AfterConfig {
    }

    @Configuration
    @ResourcePropertySource(value = "classpath*:/not-found.properties", ignoreResourceNotFound = true)
    static class IgnoreResourceNotFoundConfig {
    }

    @Configuration
    @ResourcePropertySource("classpath*:/not-found.properties")
    static class NotFoundConfig {
    }

    @Configuration
    @YamlPropertySource("classpath:/META-INF/test/app.yml")
    static class YamlConfig {
    }

    @Configuration
    @JsonPropertySource("classpath:/META-INF/test/app.json")
    static class JsonConfig {
    }

    @Configuration
    @ResourcePropertySource("classpath:/META-INF/test/a.properties")
    @ResourcePropertySource(name = "repeatable", value = "classpath:/META-INF/test/repeatable.properties")
    static class RepeatableConfig {
    }
}
