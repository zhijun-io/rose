package io.zhijun.spring.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.PropertyResolver;

class ResolvablePlaceholderAnnotationAttributesTests {

    @Test
    void resolvesPlaceholders() {
        @PlaceholderAnnotation(value = "${my.prop}", tags = {"${tag.a}", "${tag.b}"})
        class Annotated {}

        PlaceholderAnnotation annotation = Annotated.class.getAnnotation(PlaceholderAnnotation.class);
        PropertyResolver resolver = new MapPropertyResolver("my.prop", "resolved",
                "tag.a", "alpha", "tag.b", "beta");

        ResolvablePlaceholderAnnotationAttributes<PlaceholderAnnotation> attrs =
                new ResolvablePlaceholderAnnotationAttributes<>(annotation, resolver);

        assertThat(attrs.getString("value")).isEqualTo("resolved");
        assertThat(attrs.getStringArray("tags")).containsExactly("alpha", "beta");
    }

    @Test
    void nullResolverReturnsOriginalValues() {
        @PlaceholderAnnotation(value = "${my.prop}")
        class Annotated {}

        PlaceholderAnnotation annotation = Annotated.class.getAnnotation(PlaceholderAnnotation.class);
        ResolvablePlaceholderAnnotationAttributes<PlaceholderAnnotation> attrs =
                new ResolvablePlaceholderAnnotationAttributes<>(annotation, null);

        assertThat(attrs.getString("value")).isEqualTo("${my.prop}");
    }

    @Test
    void ofFactoryFromAnnotationWithResolver() {
        @PlaceholderAnnotation(value = "${x}")
        class Annotated {}

        PlaceholderAnnotation annotation = Annotated.class.getAnnotation(PlaceholderAnnotation.class);
        PropertyResolver resolver = new MapPropertyResolver("x", "y");
        ResolvablePlaceholderAnnotationAttributes<PlaceholderAnnotation> attrs =
                ResolvablePlaceholderAnnotationAttributes.of(annotation, resolver);

        assertThat(attrs.getString("value")).isEqualTo("y");
    }

    @Test
    void ofFactoryFromGenericAttributes() {
        Map<String, Object> map = new HashMap<>();
        map.put("value", "${p}");
        GenericAnnotationAttributes<PlaceholderAnnotation> generic =
                new GenericAnnotationAttributes<>(map, PlaceholderAnnotation.class);

        PropertyResolver resolver = new MapPropertyResolver("p", "v");
        ResolvablePlaceholderAnnotationAttributes<PlaceholderAnnotation> attrs =
                ResolvablePlaceholderAnnotationAttributes.of(generic, resolver);

        assertThat(attrs.getString("value")).isEqualTo("v");
    }

    @Test
    void placeholderInWholeArray() {
        @PlaceholderAnnotation(value = "x", tags = {"${a}", "${b}"})
        class Annotated {}

        PlaceholderAnnotation annotation = Annotated.class.getAnnotation(PlaceholderAnnotation.class);
        PropertyResolver resolver = new MapPropertyResolver("a", "1", "b", "2");
        ResolvablePlaceholderAnnotationAttributes<PlaceholderAnnotation> attrs =
                new ResolvablePlaceholderAnnotationAttributes<>(annotation, resolver);

        assertThat(attrs.getStringArray("tags")).containsExactly("1", "2");
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface PlaceholderAnnotation {
        String value();
        String[] tags() default {};
    }

    /** 简单的 Map 实现 PropertyResolver，用于测试。 */
    static class MapPropertyResolver implements PropertyResolver {

        private final Map<String, String> map = new HashMap<>();

        MapPropertyResolver(String... kvs) {
            for (int i = 0; i < kvs.length; i += 2) {
                map.put(kvs[i], kvs[i + 1]);
            }
        }

        @Override
        public boolean containsProperty(String key) {
            return map.containsKey(key);
        }

        @Override
        public String getProperty(String key) {
            return map.get(key);
        }

        @Override
        public String getProperty(String key, String defaultValue) {
            return map.getOrDefault(key, defaultValue);
        }

        @Override
        public <T> T getProperty(String key, Class<T> targetType) {
            return null;
        }

        @Override
        public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
            return null;
        }

        @Override
        public String getRequiredProperty(String key) throws IllegalStateException {
            return map.get(key);
        }

        @Override
        public <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
            return null;
        }

        @Override
        public String resolvePlaceholders(String text) {
            for (Map.Entry<String, String> e : map.entrySet()) {
                text = text.replace("${" + e.getKey() + "}", e.getValue());
            }
            return text;
        }

        @Override
        public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
            return resolvePlaceholders(text);
        }
    }
}
