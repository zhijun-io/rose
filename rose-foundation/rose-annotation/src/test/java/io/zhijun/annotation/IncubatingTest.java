package io.zhijun.annotation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IncubatingTest {

    @Incubating
    static class Parent {}

    static final class Child extends Parent {}

    @Test
    void inheritsToSubclasses() {
        assertTrue(Child.class.isAnnotationPresent(Incubating.class));
    }
}
