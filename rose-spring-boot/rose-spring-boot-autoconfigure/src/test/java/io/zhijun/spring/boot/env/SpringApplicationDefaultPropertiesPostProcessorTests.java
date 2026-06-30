package io.zhijun.spring.boot.env;

import org.junit.jupiter.api.Test;
import java.util.LinkedHashSet;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

class SpringApplicationDefaultPropertiesPostProcessorTests {

    @Test
    void shouldInitializeResourcesWithDefaultPattern() {
        SpringApplicationDefaultPropertiesPostProcessor processor =
                new SpringApplicationDefaultPropertiesPostProcessor();
        Set<String> resources = new LinkedHashSet<>();
        processor.initializeResources(resources);
        assertThat(resources).containsExactly(
                SpringApplicationDefaultPropertiesPostProcessor.DEFAULT_PROPERTIES_RESOURCES_PATTERN);
    }

    @Test
    void shouldAccumulateWithExistingResources() {
        SpringApplicationDefaultPropertiesPostProcessor processor =
                new SpringApplicationDefaultPropertiesPostProcessor();
        Set<String> resources = new LinkedHashSet<>();
        resources.add("classpath:custom.properties");
        processor.initializeResources(resources);
        assertThat(resources).hasSize(2);
    }
}
