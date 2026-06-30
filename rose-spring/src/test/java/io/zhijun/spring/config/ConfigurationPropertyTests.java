package io.zhijun.spring.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.PropertySource;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationPropertyTests {

    @Test
    void shouldCreateWithNameOnly() {
        ConfigurationProperty prop = new ConfigurationProperty("my.property");
        assertThat(prop.getName()).isEqualTo("my.property");
        assertThat(prop.getValue()).isNull();
        assertThat(prop.getPropertySource()).isNull();
    }

    @Test
    void shouldCreateWithNameValueAndSource() {
        PropertySource<?> source = PropertySource.named("mock");
        ConfigurationProperty prop = new ConfigurationProperty("app.foo", "bar", source);
        assertThat(prop.getName()).isEqualTo("app.foo");
        assertThat(prop.getValue()).isEqualTo("bar");
        assertThat(prop.getPropertySource()).isSameAs(source);
    }

    @Test
    void shouldSetValue() {
        ConfigurationProperty prop = new ConfigurationProperty("key");
        prop.setValue("newValue");
        assertThat(prop.getValue()).isEqualTo("newValue");
    }

    @Test
    void shouldSetDefaultValue() {
        ConfigurationProperty prop = new ConfigurationProperty("key");
        prop.setDefaultValue(42);
        assertThat(prop.getDefaultValue()).isEqualTo(42);
    }

    @Test
    void shouldImplementEquals() {
        ConfigurationProperty p1 = new ConfigurationProperty("prop", "val", null);
        ConfigurationProperty p2 = new ConfigurationProperty("prop", "val", null);
        assertThat(p1).isEqualTo(p2);
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
    }

    @Test
    void shouldNotBeEqualToDifferentName() {
        ConfigurationProperty p1 = new ConfigurationProperty("a", "val", null);
        ConfigurationProperty p2 = new ConfigurationProperty("b", "val", null);
        assertThat(p1).isNotEqualTo(p2);
    }

    @Test
    void shouldNotBeEqualToDifferentValue() {
        ConfigurationProperty p1 = new ConfigurationProperty("prop", "a", null);
        ConfigurationProperty p2 = new ConfigurationProperty("prop", "b", null);
        assertThat(p1).isNotEqualTo(p2);
    }

    @Test
    void shouldNotBeEqualWithDifferentRequired() {
        ConfigurationProperty p1 = new ConfigurationProperty("prop");
        p1.setRequired(true);
        ConfigurationProperty p2 = new ConfigurationProperty("prop");
        assertThat(p1).isNotEqualTo(p2);
    }

    @Test
    void shouldProduceToString() {
        ConfigurationProperty prop = new ConfigurationProperty("test.key");
        String str = prop.toString();
        assertThat(str).contains("test.key");
    }
}
