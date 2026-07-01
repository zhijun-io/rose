package io.zhijun.spring.web.metadata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HandlerMetadataTests {

    @Test
    void constructorSetsHandlerAndMetadata() {
        String handler = "handler";
        Integer metadata = 42;
        HandlerMetadata<String, Integer> hm = new HandlerMetadata<>(handler, metadata);
        assertSame(handler, hm.getHandler());
        assertSame(metadata, hm.getMetadata());
    }

    @Test
    void constructorRejectsNullHandler() {
        assertThrows(NullPointerException.class, () -> new HandlerMetadata<>(null, "meta"));
    }

    @Test
    void constructorRejectsNullMetadata() {
        assertThrows(NullPointerException.class, () -> new HandlerMetadata<>("handler", null));
    }

    @Test
    void equalsBasedOnHandlerAndMetadata() {
        HandlerMetadata<String, Integer> a = new HandlerMetadata<>("h", 1);
        HandlerMetadata<String, Integer> b = new HandlerMetadata<>("h", 1);
        HandlerMetadata<String, Integer> c = new HandlerMetadata<>("h", 2);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    void toStringContainsClassName() {
        HandlerMetadata<String, Integer> hm = new HandlerMetadata<>("h", 1);
        assertTrue(hm.toString().contains("HandlerMetadata"));
    }
}
