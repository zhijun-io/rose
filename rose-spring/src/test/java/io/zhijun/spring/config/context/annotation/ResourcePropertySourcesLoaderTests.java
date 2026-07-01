package io.zhijun.spring.config.context.annotation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ResourcePropertySourcesLoaderTests.class)
@TestPropertySource(properties = "value=classpath*:/META-INF/test/*.properties")
@ResourcePropertySources({
        @ResourcePropertySource(value = {"${value}"}),
        @ResourcePropertySource(value = {"${value}"})
})
class ResourcePropertySourcesLoaderTests {

    @Autowired
    private Environment environment;

    @Test
    void shouldLoadRepeatableResourcePropertySources() {
        assertThat(environment.getProperty("a")).isEqualTo("1");
        assertThat(environment.getProperty("b")).isEqualTo("3");
    }
}
