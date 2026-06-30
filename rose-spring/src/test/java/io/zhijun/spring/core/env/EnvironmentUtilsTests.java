package io.zhijun.spring.core.env;

import org.junit.jupiter.api.Test;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.env.StandardEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentUtilsTests {

    @Test
    void shouldConvertToConfigurableEnvironment() {
        StandardEnvironment env = new StandardEnvironment();
        ConfigurableEnvironment result = EnvironmentUtils.asConfigurableEnvironment(env);
        assertThat(result).isSameAs(env);
    }

    @Test
    void shouldReturnNullForNonConfigurableEnvironment() {
        assertThat(EnvironmentUtils.asConfigurableEnvironment(plainEnvironment())).isNull();
    }

    @Test
    void shouldReturnNullForNonConfigurableEnvironmentInGetConversionService() {
        assertThat(EnvironmentUtils.getConversionService(plainEnvironment())).isNull();
    }

    private static Environment plainEnvironment() {
        return new Environment() {
            @Override public String[] getActiveProfiles() { return new String[0]; }
            @Override public String[] getDefaultProfiles() { return new String[0]; }
            @Override public boolean acceptsProfiles(Profiles profiles) { return false; }
            @Override public boolean acceptsProfiles(String... strings) { return false; }
            @Override public boolean containsProperty(String key) { return false; }
            @Override public String getProperty(String key) { return null; }
            @Override public String getProperty(String key, String defaultValue) { return defaultValue; }
            @Override public <T> T getProperty(String key, Class<T> targetType) { return null; }
            @Override public <T> T getProperty(String key, Class<T> targetType, T defaultValue) { return defaultValue; }
            @Override public String getRequiredProperty(String key) { return null; }
            @Override public <T> T getRequiredProperty(String key, Class<T> targetType) { return null; }
            @Override public String resolvePlaceholders(String text) { return text; }
            @Override public String resolveRequiredPlaceholders(String text) { return text; }
        };
    }
}
