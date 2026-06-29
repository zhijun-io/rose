package io.zhijun.spring.core.binder.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;

class ConfigurationBeanBinderTests {

    @Test
    void bindMapsPropertiesToFields() {
        ConfigurationBeanBinder binder = new ConfigurationBeanBinder();
        SampleConfiguration target = new SampleConfiguration();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("name", "rose");
        properties.put("enabled", "true");

        binder.bind(properties, true, true, target);

        assertThat(target.name).isEqualTo("rose");
        assertThat(target.enabled).isTrue();
    }

    @Test
    void bindUsesConfiguredConversionService() {
        ConfigurationBeanBinder binder = new ConfigurationBeanBinder();
        ConversionService conversionService = new DefaultFormattingConversionService();
        binder.setConversionService(conversionService);
        SampleConfiguration target = new SampleConfiguration();

        binder.bind(Collections.<String, Object>singletonMap("count", "42"), true, true, target);

        assertThat(target.count).isEqualTo(42);
    }

    @Test
    void bindIgnoresUnknownFieldsWhenConfigured() {
        ConfigurationBeanBinder binder = new ConfigurationBeanBinder();
        SampleConfiguration target = new SampleConfiguration();

        binder.bind(Collections.<String, Object>singletonMap("unknown", "value"), true, true, target);

        assertThat(target.name).isNull();
    }

    @Test
    void bindRejectsUnknownFieldsWhenNotIgnored() {
        ConfigurationBeanBinder binder = new ConfigurationBeanBinder();
        SampleConfiguration target = new SampleConfiguration();

        assertThatThrownBy(() ->
                        binder.bind(Collections.<String, Object>singletonMap("unknown", "value"), false, true, target))
                .isInstanceOf(org.springframework.beans.NotWritablePropertyException.class);
    }

    @Test
    void bindIgnoresInvalidFieldsWhenConfigured() {
        ConfigurationBeanBinder binder = new ConfigurationBeanBinder();
        SampleConfiguration target = new SampleConfiguration();

        binder.bind(Collections.<String, Object>singletonMap("count", "not-a-number"), true, true, target);

        assertThat(target.count).isZero();
    }

    @Test
    void bindStillBindsValidFieldsWhenInvalidFieldsNotIgnored() {
        ConfigurationBeanBinder binder = new ConfigurationBeanBinder();
        SampleConfiguration target = new SampleConfiguration();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("count", "42");
        properties.put("enabled", "not-a-boolean");

        binder.bind(properties, true, false, target);

        assertThat(target.count).isEqualTo(42);
        assertThat(target.enabled).isFalse();
    }

    static class SampleConfiguration {

        String name;

        boolean enabled;

        int count;
    }
}
