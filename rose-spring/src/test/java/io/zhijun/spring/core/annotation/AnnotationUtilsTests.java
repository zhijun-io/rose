package io.zhijun.spring.core.annotation;

import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotationAttributes;

import java.lang.annotation.*;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnnotationUtilsTests {

    @TestAnnotation(value = "test", count = 42)
    static class AnnotatedElement {
    }

    @Test
    void shouldFindAnnotationTypeByClassName() {
        assertThat(AnnotationUtils.findAnnotationType("java.lang.Override"))
                .isEqualTo(Override.class);
    }

    @Test
    void shouldReturnNullForUnknownAnnotationType() {
        assertThat(AnnotationUtils.findAnnotationType("com.example.NonExistent"))
                .isNull();
    }

    @Test
    void shouldGetAnnotationAttributes() {
        TestAnnotation annotation = AnnotatedElement.class.getAnnotation(TestAnnotation.class);
        AnnotationAttributes attrs = AnnotationUtils.getAnnotationAttributes(annotation);
        assertThat(attrs).isNotNull();
        assertThat(attrs.getString("value")).isEqualTo("test");
        Object count = attrs.getNumber("count");
        assertThat(count).isEqualTo(42);
    }

    @Test
    void shouldGetAttributeFromAnnotationAttributes() {
        AnnotationAttributes attrs = new AnnotationAttributes();
        attrs.put("name", "test");
        String value = AnnotationUtils.getAttribute(attrs, "name");
        assertThat(value).isEqualTo("test");
    }

    @Test
    void shouldGetAttributeFromMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", 42);
        Integer value = AnnotationUtils.getAttribute(map, "key");
        assertThat(value).isEqualTo(42);
    }

    @Test
    void shouldGetAttributeWithDefaultFromMap() {
        Map<String, Object> map = new HashMap<>();
        Object value = AnnotationUtils.getAttribute(map, "missing", true);
        assertThat(value).isEqualTo(true);
    }

    @Test
    void shouldGetRequiredAttribute() {
        AnnotationAttributes attrs = new AnnotationAttributes();
        attrs.put("required", "val");
        String val = AnnotationUtils.getRequiredAttribute(attrs, "required");
        assertThat(val).isEqualTo("val");
    }

    @Test
    void shouldThrowWhenRequiredAttributeMissing() {
        AnnotationAttributes attrs = new AnnotationAttributes();
        assertThatThrownBy(() -> AnnotationUtils.getRequiredAttribute(attrs, "missing"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void shouldConvertMapToAnnotationAttributes() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        AnnotationAttributes result = AnnotationUtils.ofAnnotationAttributes(map);
        assertThat(result).isInstanceOf(AnnotationAttributes.class);
        assertThat(result.getString("key")).isEqualTo("value");
    }

    @Test
    void shouldReturnSameInstanceForAnnotationAttributes() {
        AnnotationAttributes attrs = new AnnotationAttributes();
        assertThat(AnnotationUtils.ofAnnotationAttributes(attrs)).isSameAs(attrs);
    }

    @Test
    void shouldFindAnnotationTypeFromAttributes() {
        AnnotationAttributes attrs = new AnnotationAttributes();
        attrs.put("annotationType", "java.lang.Deprecated");
        assertThat(AnnotationUtils.findAnnotationType(attrs)).isEqualTo(Deprecated.class);
    }

    @Test
    void shouldReturnNullForMissingAnnotationType() {
        AnnotationAttributes attrs = new AnnotationAttributes();
        assertThat(AnnotationUtils.findAnnotationType(attrs)).isNull();
    }

    @Test
    void shouldTryGetMergedAnnotationWhenPresent() {
        AnnotationAttributes attrs = AnnotationUtils.tryGetMergedAnnotation(AnnotatedElement.class, TestAnnotation.class);
        assertThat(attrs).isNotNull();
        assertThat(attrs.getString("value")).isEqualTo("test");
    }

    @Test
    void shouldReturnNullForMissingMergedAnnotation() {
        AnnotationAttributes attrs = AnnotationUtils.tryGetMergedAnnotation(AnnotatedElement.class, SuppressWarnings.class);
        assertThat(attrs).isNull();
    }

    @Test
    void shouldThrowWhenRequiredAttributeMissingFromMap() {
        Map<String, Object> map = new HashMap<>();
        assertThatThrownBy(() -> AnnotationUtils.getRequiredAttribute(map, "missing"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface TestAnnotation {
        String value();
        int count() default 0;
    }
}
