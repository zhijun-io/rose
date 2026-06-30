package io.zhijun.spring.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class GenericAnnotationAttributesTests {

    @Test
    void createFromAnnotation() {
        @SampleAnnotation(value = "hello", count = 42)
        class Annotated {}

        SampleAnnotation annotation = Annotated.class.getAnnotation(SampleAnnotation.class);
        GenericAnnotationAttributes<SampleAnnotation> attrs = new GenericAnnotationAttributes<>(annotation);

        assertThat(attrs.annotationType()).isSameAs(SampleAnnotation.class);
        assertThat(attrs.getString("value")).isEqualTo("hello");
        assertThat((int) attrs.get("count")).isEqualTo(42);
    }

    @Test
    void createFromMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("value", "test");
        map.put("count", 99);

        GenericAnnotationAttributes<SampleAnnotation> attrs = new GenericAnnotationAttributes<>(map, SampleAnnotation.class);

        assertThat(attrs.annotationType()).isSameAs(SampleAnnotation.class);
        assertThat(attrs.getString("value")).isEqualTo("test");
        assertThat((int) attrs.get("count")).isEqualTo(99);
    }

    @Test
    void ofFactoryFromAnnotation() {
        @SampleAnnotation(value = "factory", count = 1)
        class Annotated {}

        SampleAnnotation annotation = Annotated.class.getAnnotation(SampleAnnotation.class);
        GenericAnnotationAttributes<SampleAnnotation> attrs = GenericAnnotationAttributes.of(annotation);

        assertThat(attrs.annotationType()).isSameAs(SampleAnnotation.class);
        assertThat(attrs.getString("value")).isEqualTo("factory");
    }

    @Test
    void equalsAndHashCode() {
        Map<String, Object> m1 = new HashMap<>();
        m1.put("value", "x");
        m1.put("count", 1);

        Map<String, Object> m2 = new HashMap<>();
        m2.put("value", "x");
        m2.put("count", 1);

        GenericAnnotationAttributes<SampleAnnotation> a1 = new GenericAnnotationAttributes<>(m1, SampleAnnotation.class);
        GenericAnnotationAttributes<SampleAnnotation> a2 = new GenericAnnotationAttributes<>(m2, SampleAnnotation.class);

        assertThat(a1).isEqualTo(a2);
        assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
    }

    @Test
    void differentValuesNotEqual() {
        Map<String, Object> m1 = new HashMap<>();
        m1.put("value", "x");

        Map<String, Object> m2 = new HashMap<>();
        m2.put("value", "y");

        GenericAnnotationAttributes<SampleAnnotation> a1 = new GenericAnnotationAttributes<>(m1, SampleAnnotation.class);
        GenericAnnotationAttributes<SampleAnnotation> a2 = new GenericAnnotationAttributes<>(m2, SampleAnnotation.class);

        assertThat(a1).isNotEqualTo(a2);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface SampleAnnotation {
        String value();
        int count() default 0;
    }
}
