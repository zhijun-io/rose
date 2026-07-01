package io.zhijun.spring.config.env;

import io.zhijun.spring.config.env.config.ResourceYamlProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceYamlProcessorTests {

    @Test
    void shouldProcessSingleYamlResource() {
        Resource resource = new ClassPathResource("test-yaml/simple.yml");
        ResourceYamlProcessor processor = new ResourceYamlProcessor(resource);
        Map<String, Object> result = processor.process();

        assertThat(result)
                .containsEntry("app.name", "test-app")
                .containsEntry("app.version", "1.0.0")
                .containsEntry("server.port", 8080)
                .containsEntry("server.host", "localhost");
    }

    @Test
    void shouldProcessMultipleYamlResources() {
        Resource resource1 = new ClassPathResource("test-yaml/simple.yml");
        Resource resource2 = new ClassPathResource("test-yaml/nested-with-list.yml");
        ResourceYamlProcessor processor = new ResourceYamlProcessor(resource1, resource2);
        Map<String, Object> result = processor.process();

        assertThat(result)
                .containsEntry("app.name", "test-app")
                .containsEntry("spring.datasource.url", "jdbc:h2:mem:test");
    }

    @Test
    void shouldHandleListValues() {
        Resource resource = new ClassPathResource("test-yaml/nested-with-list.yml");
        ResourceYamlProcessor processor = new ResourceYamlProcessor(resource);
        Map<String, Object> result = processor.process();

        assertThat(result)
                .containsEntry("spring.datasource.url", "jdbc:h2:mem:test")
                .containsEntry("spring.datasource.username", "sa");
    }

    @Test
    void shouldReturnUnmodifiableMap() {
        Resource resource = new ClassPathResource("test-yaml/simple.yml");
        ResourceYamlProcessor processor = new ResourceYamlProcessor(resource);
        Map<String, Object> result = processor.process();

        assertThat(result).isNotEmpty();
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> result.put("x", "y"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
