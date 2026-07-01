package io.zhijun.spring.config.context.annotation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;
import static org.springframework.core.env.StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME;

class ResourcePropertySourceLoaderTests {

    @Test
    void shouldLoadDefaultPropertySource() {
        withContext(DefaultConfig.class, environment -> {
            PropertySource<?> propertySource = getLast(environment);
            assertDefaultPropertySourceName(propertySource, DefaultConfig.class);
            assertCompositeProperties(propertySource);
            assertEnvironmentProperties(environment);
        });
    }

    @Test
    void shouldLoadNamedPropertySource() {
        withContext(NamedConfig.class, environment -> {
            PropertySource<?> propertySource = environment.getPropertySources().get("test-property-source");
            assertThat(propertySource).isSameAs(getLast(environment));
            assertCompositeProperties(propertySource);
            assertEnvironmentProperties(environment);
        });
    }

    @Test
    void shouldInsertFirstPropertySource() {
        withContext(FirstConfig.class, environment -> {
            PropertySource<?> propertySource = environment.getPropertySources().iterator().next();
            assertDefaultPropertySourceName(propertySource, FirstConfig.class);
            assertCompositeProperties(propertySource);
            assertEnvironmentProperties(environment);
        });
    }

    @Test
    void shouldInsertBeforeNamedSource() {
        withContext(BeforeConfig.class, environment -> {
            Iterator<PropertySource<?>> iterator = environment.getPropertySources().iterator();
            PropertySource<?> previous = null;
            while (iterator.hasNext()) {
                PropertySource<?> current = iterator.next();
                if (SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME.equals(current.getName())) {
                    break;
                }
                previous = current;
            }
            assertDefaultPropertySourceName(previous, BeforeConfig.class);
            assertCompositeProperties(previous);
        });
    }

    @Test
    void shouldInsertAfterNamedSource() {
        withContext(AfterConfig.class, environment -> {
            Iterator<PropertySource<?>> iterator = environment.getPropertySources().iterator();
            while (iterator.hasNext()) {
                PropertySource<?> current = iterator.next();
                if (SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME.equals(current.getName())) {
                    break;
                }
            }
            PropertySource<?> next = iterator.next();
            assertDefaultPropertySourceName(next, AfterConfig.class);
            assertCompositeProperties(next);
        });
    }

    @Test
    void shouldIgnoreMissingResourceWhenConfigured() {
        withContext(IgnoreResourceNotFoundConfig.class, environment -> {
            MutablePropertySources propertySources = environment.getPropertySources();
            assertThat(propertySources.contains(SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME)).isTrue();
            assertThat(propertySources.contains(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)).isTrue();
        });
    }

    @Test
    void shouldFailOnMissingResourceByDefault() {
        assertThatThrownBy(() -> withContext(NotFoundConfig.class, environment -> {
        })).isInstanceOf(BeanDefinitionStoreException.class);
    }

    private void withContext(Class<?> configClass, java.util.function.Consumer<ConfigurableEnvironment> assertion) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context, "value=classpath*:/META-INF/test/*.properties");
        context.register(configClass);
        try {
            context.refresh();
            assertion.accept(context.getEnvironment());
        } finally {
            context.close();
        }
    }

    private static PropertySource<?> getLast(ConfigurableEnvironment environment) {
        Iterator<PropertySource<?>> iterator = environment.getPropertySources().iterator();
        PropertySource<?> result = null;
        while (iterator.hasNext()) {
            result = iterator.next();
        }
        return result;
    }

    private static void assertDefaultPropertySourceName(PropertySource<?> propertySource, Class<?> configClass) {
        assertThat(propertySource.getName())
                .isEqualTo(configClass.getName() + "@" + ResourcePropertySource.class.getName());
    }

    private static void assertCompositeProperties(PropertySource<?> propertySource) {
        assertThat(propertySource).isInstanceOf(CompositePropertySource.class);
        assertThat(propertySource.getProperty("a")).isEqualTo("1");
        assertThat(propertySource.getProperty("b")).isEqualTo("3");
    }

    private static void assertEnvironmentProperties(ConfigurableEnvironment environment) {
        assertThat(environment.getProperty("a")).isEqualTo("1");
        assertThat(environment.getProperty("b")).isEqualTo("3");
    }

    @ResourcePropertySource("classpath*:/META-INF/test/*.properties")
    static class DefaultConfig {
    }

    @ResourcePropertySource(name = "test-property-source", value = "classpath*:/META-INF/test/*.properties")
    static class NamedConfig {
    }

    @ResourcePropertySource(value = "classpath*:/META-INF/test/*.properties", first = true)
    static class FirstConfig {
    }

    @ResourcePropertySource(value = "classpath*:/META-INF/test/*.properties", before = SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)
    static class BeforeConfig {
    }

    @ResourcePropertySource(value = "classpath*:/META-INF/test/*.properties", after = SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME)
    static class AfterConfig {
    }

    @ResourcePropertySource(value = "classpath*:/not-found.properties", ignoreResourceNotFound = true)
    static class IgnoreResourceNotFoundConfig {
    }

    @ResourcePropertySource("classpath*:/not-found.properties")
    static class NotFoundConfig {
    }
}
