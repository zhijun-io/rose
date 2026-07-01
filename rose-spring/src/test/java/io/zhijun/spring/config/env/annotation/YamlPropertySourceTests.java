package io.zhijun.spring.config.env.annotation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = YamlPropertySourceTests.Config.class)
class YamlPropertySourceTests {

    @Value("${app.name}")
    private String appName;

    @Value("${spring.datasource.url}")
    private String dataSourceUrl;

    @Test
    void shouldLoadYamlPropertySourceAnnotation() {
        assertThat(appName).isEqualTo("test-app");
        assertThat(dataSourceUrl).isEqualTo("jdbc:h2:mem:test");
    }

    @Configuration
    @YamlPropertySource({
            "classpath:test-yaml/simple.yml",
            "classpath:test-yaml/nested-with-list.yml"
    })
    static class Config {
    }
}
