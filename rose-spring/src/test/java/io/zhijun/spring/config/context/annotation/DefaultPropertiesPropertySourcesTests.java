package io.zhijun.spring.config.context.annotation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.zhijun.spring.core.env.PropertySourcesUtils.DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DefaultPropertiesPropertySourcesTests.class)
@DefaultPropertiesPropertySource(locations = {"classpath*:/META-INF/test/a.properties"})
@DefaultPropertiesPropertySource(value = {"classpath*:/META-INF/test/b.properties"})
@DefaultPropertiesPropertySource(properties = {"a=0", "b=1", "c=2"})
class DefaultPropertiesPropertySourcesTests {

    @Autowired
    private ConfigurableEnvironment environment;

    @Test
    void shouldMergeRepeatableDefaultProperties() {
        assertThat(environment.getPropertySources().contains(DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME)).isTrue();
        assertThat(environment.getProperty("a")).isEqualTo("0");
        assertThat(environment.getProperty("b")).isEqualTo("1");
        assertThat(environment.getProperty("c")).isEqualTo("2");
    }
}
