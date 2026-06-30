package io.zhijun.spring.web.metadata;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class WebEndpointMappingTests {

    @Test
    void shouldBuildWithMinimalConfiguration() {
        WebEndpointMapping mapping = WebEndpointMapping.builder()
                .endpoint("test")
                .build();

        assertThat(mapping.getEndpoint()).isEqualTo("test");
        assertThat(mapping.getSource()).isSameAs(WebEndpointMapping.UNKNOWN_SOURCE);
        assertThat(mapping.getPatterns()).isEmpty();
        assertThat(mapping.getMethods()).isEmpty();
    }

    @Test
    void shouldBuildWithFullConfiguration() {
        WebEndpointMapping mapping = WebEndpointMapping.builder()
                .endpoint("myEndpoint")
                .source("mySource")
                .pattern("/api/foo")
                .methods("GET", "POST")
                .params("param1=value1")
                .headers("X-Custom=val")
                .consumes("application/json")
                .produces("application/json")
                .build();

        assertThat(mapping.getEndpoint()).isEqualTo("myEndpoint");
        assertThat(mapping.getSource()).isEqualTo("mySource");
        assertThat(mapping.getPatterns()).containsExactly("/api/foo");
        assertThat(mapping.getMethods()).containsExactly("GET", "POST");
        assertThat(mapping.getParams()).containsExactly("param1=value1");
        assertThat(mapping.getHeaders()).containsExactly("X-Custom=val");
        assertThat(mapping.getConsumes()).containsExactly("application/json");
        assertThat(mapping.getProduces()).containsExactly("application/json");
    }

    @Test
    void shouldSupportCollectionMethods() {
        WebEndpointMapping mapping = WebEndpointMapping.builder()
                .endpoint("coll")
                .patterns(Arrays.asList("/a", "/b"))
                .methods(Arrays.asList("GET", "PUT"))
                .build();

        assertThat(mapping.getPatterns()).containsExactly("/a", "/b");
        assertThat(mapping.getMethods()).containsExactly("GET", "PUT");
    }

    @Test
    void shouldSupportVarargsMethods() {
        WebEndpointMapping mapping = WebEndpointMapping.builder()
                .endpoint("v")
                .patterns("/x", "/y")
                .methods("DELETE")
                .build();

        assertThat(mapping.getPatterns()).containsExactly("/x", "/y");
        assertThat(mapping.getMethods()).containsExactly("DELETE");
    }

    @Test
    void shouldImplementEqualsBasedOnAllFields() {
        WebEndpointMapping m1 = WebEndpointMapping.builder()
                .endpoint("e").source("s").pattern("/p").method("GET")
                .build();

        WebEndpointMapping m2 = WebEndpointMapping.builder()
                .endpoint("e").source("s").pattern("/p").method("GET")
                .build();

        assertThat(m1).isEqualTo(m2);
        assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
    }

    @Test
    void shouldNotBeEqualWithDifferentPatterns() {
        WebEndpointMapping m1 = WebEndpointMapping.builder()
                .endpoint("e").pattern("/a").build();
        WebEndpointMapping m2 = WebEndpointMapping.builder()
                .endpoint("e").pattern("/b").build();

        assertThat(m1).isNotEqualTo(m2);
    }

    @Test
    void shouldNotBeEqualWithDifferentEndpoint() {
        WebEndpointMapping m1 = WebEndpointMapping.builder()
                .endpoint("e1").pattern("/a").build();
        WebEndpointMapping m2 = WebEndpointMapping.builder()
                .endpoint("e2").pattern("/a").build();

        assertThat(m1).isNotEqualTo(m2);
    }

    @Test
    void shouldNotBeEqualToNull() {
        WebEndpointMapping m = WebEndpointMapping.builder()
                .endpoint("e").build();
        assertThat(m).isNotEqualTo(null);
    }

    @Test
    void shouldNotBeEqualToDifferentType() {
        WebEndpointMapping m = WebEndpointMapping.builder()
                .endpoint("e").build();
        assertThat(m).isNotEqualTo("string");
    }

    @Test
    void shouldProduceConsistentToString() {
        WebEndpointMapping m = WebEndpointMapping.builder()
                .endpoint("e1")
                .pattern("/api/test")
                .method("GET")
                .build();

        String str = m.toString();
        assertThat(str).contains("e1");
        assertThat(str).contains("/api/test");
        assertThat(str).contains("GET");
    }

    @Test
    void shouldMaintainInsertionOrderInSets() {
        WebEndpointMapping m = WebEndpointMapping.builder()
                .endpoint("ordered")
                .methods("POST", "GET", "PUT")
                .build();

        assertThat(m.getMethods()).containsExactly("POST", "GET", "PUT");
    }

    @Test
    void shouldDeduplicateValues() {
        WebEndpointMapping m = WebEndpointMapping.builder()
                .endpoint("dedup")
                .method("GET").method("GET").method("POST").method("GET")
                .build();

        assertThat(m.getMethods()).containsExactly("GET", "POST");
    }

    @Test
    void shouldReturnUnknownSourceByDefault() {
        WebEndpointMapping m = WebEndpointMapping.builder()
                .endpoint("test")
                .build();

        assertThat(m.getSource()).isSameAs(WebEndpointMapping.UNKNOWN_SOURCE);
    }
}
