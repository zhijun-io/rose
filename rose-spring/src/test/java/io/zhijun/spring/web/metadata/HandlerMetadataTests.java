package io.zhijun.spring.web.metadata;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HandlerMetadataTests {

    @Test
    void constructorSetsHandlerAndMetadata() {
        String handler = "handler";
        Integer metadata = 42;
        HandlerMetadata<String, Integer> hm = new HandlerMetadata<>(handler, metadata);
        assertThat(hm.getHandler()).isSameAs(handler);
        assertThat(hm.getMetadata()).isSameAs(metadata);
    }

    @Test
    void constructorRejectsNullHandler() {
        assertThatThrownBy(() -> new HandlerMetadata<>(null, "meta"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructorRejectsNullMetadata() {
        assertThatThrownBy(() -> new HandlerMetadata<>("handler", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void equalsBasedOnHandlerAndMetadata() {
        HandlerMetadata<String, Integer> a = new HandlerMetadata<>("h", 1);
        HandlerMetadata<String, Integer> b = new HandlerMetadata<>("h", 1);
        HandlerMetadata<String, Integer> c = new HandlerMetadata<>("h", 2);
        assertThat(b).isEqualTo(a);
        assertThat(b.hashCode()).isEqualTo(a.hashCode());
        assertThat(c).isNotEqualTo(a);
    }

    @Test
    void toStringContainsClassName() {
        HandlerMetadata<String, Integer> hm = new HandlerMetadata<>("h", 1);
        assertThat(hm.toString()).contains("HandlerMetadata");
    }
}
