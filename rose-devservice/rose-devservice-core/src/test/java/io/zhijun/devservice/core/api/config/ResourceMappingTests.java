package io.zhijun.devservice.core.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link ResourceMapping}.
 */
class ResourceMappingTests {

    @Test
    void accessors() {
        ResourceMapping mapping = new ResourceMapping();
        mapping.setSourcePath("init.sql");
        mapping.setContainerPath("/docker-entrypoint-initdb.d/init.sql");

        assertThat(mapping.getSourcePath()).isEqualTo("init.sql");
        assertThat(mapping.getContainerPath()).isEqualTo("/docker-entrypoint-initdb.d/init.sql");
    }

    @Test
    void constructor() {
        ResourceMapping mapping = new ResourceMapping("schema.sql", "/schema.sql");

        assertThat(mapping.getSourcePath()).isEqualTo("schema.sql");
        assertThat(mapping.getContainerPath()).isEqualTo("/schema.sql");
    }
}
