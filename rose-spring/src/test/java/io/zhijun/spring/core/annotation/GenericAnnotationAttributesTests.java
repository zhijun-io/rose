package io.zhijun.spring.core.annotation;

import org.junit.jupiter.api.Test;

import java.lang.annotation.*;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GenericAnnotationAttributesTests {

    @Test
    void shouldCreateFromAnnotationInstance() {
        SampleAnnotation annotation = AnnotatedElement.class.getAnnotation(SampleAnnotation.class);
        GenericAnnotationAttributes<SampleAnnotation> attrs = new GenericAnnotationAttributes<SampleAnnotation>(annotation);
        assertThat(attrs.annotationType()).isEqualTo(SampleAnnotation.class);
        assertThat(attrs.getString("value")).isEqualTo("hello");
    }

    @Test
    void shouldCreateFromMapAndAnnotationType() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("value", "world");
        GenericAnnotationAttributes<SampleAnnotation> attrs = new GenericAnnotationAttributes<SampleAnnotation>(map, SampleAnnotation.class);
        assertThat(attrs.annotationType()).isEqualTo(SampleAnnotation.class);
        assertThat(attrs.getString("value")).isEqualTo("world");
    }

    @Test
    void shouldThrowWhenAnnotationTypeIsNull() {
        assertThatThrownBy(() -> new GenericAnnotationAttributes<SampleAnnotation>(new HashMap<String, Object>(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("annotationType must not be null");
    }

    @Test
    void shouldImplementEqualsBasedOnAnnotationTypeAndAttributes() {
        Map<String, Object> map1 = new HashMap<String, Object>();
        map1.put("value", "hello");
        Map<String, Object> map2 = new HashMap<String, Object>();
        map2.put("value", "hello");

        GenericAnnotationAttributes<SampleAnnotation> attrs1 = new GenericAnnotationAttributes<SampleAnnotation>(map1, SampleAnnotation.class);
        GenericAnnotationAttributes<SampleAnnotation> attrs2 = new GenericAnnotationAttributes<SampleAnnotation>(map2, SampleAnnotation.class);

        assertThat(attrs1).isEqualTo(attrs2);
        assertThat(attrs1.hashCode()).isEqualTo(attrs2.hashCode());
    }

    @Test
    void shouldNotBeEqualToDifferentAnnotationType() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("value", "hello");
        GenericAnnotationAttributes<SampleAnnotation> attrs1 = new GenericAnnotationAttributes<SampleAnnotation>(map, SampleAnnotation.class);
        GenericAnnotationAttributes<SampleAnnotation> attrs2 = new GenericAnnotationAttributes<SampleAnnotation>(map, SampleAnnotation.class) {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Deprecated.class;
            }
        };

        assertThat(attrs1).isNotEqualTo(attrs2);
    }

    @Test
    void shouldNotBeEqualToDifferentValue() {
        Map<String, Object> map1 = new HashMap<String, Object>();
        map1.put("value", "hello");
        Map<String, Object> map2 = new HashMap<String, Object>();
        map2.put("value", "world");

        GenericAnnotationAttributes<SampleAnnotation> attrs1 = new GenericAnnotationAttributes<SampleAnnotation>(map1, SampleAnnotation.class);
        GenericAnnotationAttributes<SampleAnnotation> attrs2 = new GenericAnnotationAttributes<SampleAnnotation>(map2, SampleAnnotation.class);

        assertThat(attrs1).isNotEqualTo(attrs2);
    }

    @Test
    void shouldNotBeEqualToDifferentSize() {
        Map<String, Object> map1 = new HashMap<String, Object>();
        map1.put("value", "hello");
        Map<String, Object> map2 = new HashMap<String, Object>();
        map2.put("value", "hello");
        map2.put("extra", "extra");

        GenericAnnotationAttributes<SampleAnnotation> attrs1 = new GenericAnnotationAttributes<SampleAnnotation>(map1, SampleAnnotation.class);
        GenericAnnotationAttributes<SampleAnnotation> attrs2 = new GenericAnnotationAttributes<SampleAnnotation>(map2, SampleAnnotation.class);

        assertThat(attrs1).isNotEqualTo(attrs2);
    }

    @Test
    void shouldProduceConsistentToString() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("value", "hello");
        GenericAnnotationAttributes<SampleAnnotation> attrs = new GenericAnnotationAttributes<SampleAnnotation>(map, SampleAnnotation.class);

        String str = attrs.toString();
        assertThat(str).contains("SampleAnnotation");
        assertThat(str).contains("value=\"hello\"");
    }

    @Test
    void shouldSupportOfFactoryMethodFromAnnotation() {
        SampleAnnotation annotation = AnnotatedElement.class.getAnnotation(SampleAnnotation.class);
        GenericAnnotationAttributes<SampleAnnotation> attrs = GenericAnnotationAttributes.of(annotation);
        assertThat(attrs.annotationType()).isEqualTo(SampleAnnotation.class);
    }

    @Test
    void shouldSupportOfFactoryMethodFromMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("value", "test");
        GenericAnnotationAttributes<SampleAnnotation> attrs = GenericAnnotationAttributes.of(map, SampleAnnotation.class);
        assertThat(attrs.annotationType()).isEqualTo(SampleAnnotation.class);
        assertThat(attrs.getString("value")).isEqualTo("test");
    }

    @Test
    void shouldReturnSameInstanceForGenericAnnotationAttributesInOf() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("value", "test");
        GenericAnnotationAttributes<SampleAnnotation> original = new GenericAnnotationAttributes<SampleAnnotation>(map, SampleAnnotation.class);
        GenericAnnotationAttributes<SampleAnnotation> result = GenericAnnotationAttributes.of(original, SampleAnnotation.class);
        assertThat(result).isSameAs(original);
    }

    @Test
    void shouldHandleNullValuesInHashCode() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("value", null);
        GenericAnnotationAttributes<SampleAnnotation> attrs = new GenericAnnotationAttributes<SampleAnnotation>(map, SampleAnnotation.class);
        assertThat(attrs.hashCode()).isNotZero();
    }

    @SampleAnnotation("hello")
    static class AnnotatedElement {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface SampleAnnotation {
        String value();
    }
}
