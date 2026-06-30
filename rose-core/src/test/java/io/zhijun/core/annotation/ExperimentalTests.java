package io.zhijun.core.annotation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ExperimentalTests {

    @Experimental(since = "0.0.1")
    static class Parent {
    }

    static final class Child extends Parent {
    }

    @Test
    void inheritsToSubclasses() {
        assertTrue(Child.class.isAnnotationPresent(Experimental.class));
    }
}
