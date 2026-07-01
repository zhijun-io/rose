package io.zhijun.spring.web.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestContextStrategyTests {

    @Test
    void hasThreeStrategies() {
        assertEquals(3, RequestContextStrategy.values().length);
    }

    @Test
    void containsExpectedStrategies() {
        assertNotNull(RequestContextStrategy.valueOf("DEFAULT"));
        assertNotNull(RequestContextStrategy.valueOf("THREAD_LOCAL"));
        assertNotNull(RequestContextStrategy.valueOf("INHERITABLE_THREAD_LOCAL"));
    }
}
