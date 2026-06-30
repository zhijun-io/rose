package io.zhijun.spring.web.metadata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DefaultWebEndpointMappingRegistry} 单元测试。
 */
class DefaultWebEndpointMappingRegistryTests {

    private DefaultWebEndpointMappingRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new DefaultWebEndpointMappingRegistry();
    }

    @Test
    void shouldReturnEmptyCollectionInitially() {
        assertThat(registry.getAll()).isEmpty();
    }

    @Test
    void shouldRegisterNewMapping() {
        WebEndpointMapping mapping = WebEndpointMapping.builder()
                .endpoint("test")
                .pattern("/api/test")
                .build();

        boolean result = registry.register(mapping);

        assertThat(result).isTrue();
        assertThat(registry.getAll()).hasSize(1).containsExactly(mapping);
    }

    @Test
    void shouldRejectDuplicateMappings() {
        WebEndpointMapping mapping = WebEndpointMapping.builder()
                .endpoint("dup")
                .pattern("/dup")
                .build();

        registry.register(mapping);
        boolean result = registry.register(mapping);

        assertThat(result).isFalse();
        assertThat(registry.getAll()).hasSize(1);
    }

    @Test
    void shouldRegisterMultipleMappings() {
        WebEndpointMapping m1 = WebEndpointMapping.builder()
                .endpoint("e1").pattern("/a").build();
        WebEndpointMapping m2 = WebEndpointMapping.builder()
                .endpoint("e2").pattern("/b").build();
        WebEndpointMapping m3 = WebEndpointMapping.builder()
                .endpoint("e3").pattern("/c").build();

        registry.register(m1);
        registry.register(m2);
        registry.register(m3);

        assertThat(registry.getAll()).hasSize(3).containsExactly(m1, m2, m3);
    }

    @Test
    void shouldReturnUnmodifiableCollection() {
        registry.register(WebEndpointMapping.builder().endpoint("e").build());
        assertThat(registry.getAll()).isUnmodifiable();
    }

    @Test
    void shouldIgnoreNullMapping() {
        boolean result = registry.register(null);
        assertThat(result).isFalse();
        assertThat(registry.getAll()).isEmpty();
    }

    @Test
    void shouldRegisterSameContentButDifferentInstance() {
        WebEndpointMapping m1 = WebEndpointMapping.builder()
                .endpoint("same").pattern("/x").build();
        WebEndpointMapping m2 = WebEndpointMapping.builder()
                .endpoint("same").pattern("/x").build();

        registry.register(m1);
        // Equals-based mapping, so m2 equals m1
        boolean result = registry.register(m2);

        assertThat(result).isFalse();
        assertThat(registry.getAll()).hasSize(1);
    }
}
