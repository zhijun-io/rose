package io.zhijun.spring.core.annotation;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.PropertyResolver;

import java.lang.annotation.*;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResolvablePlaceholderAnnotationAttributesTests {

    @Test
    void shouldCreateFromAnnotation() {
        Greeting annotation = AnnotatedClass.class.getAnnotation(Greeting.class);
        ResolvablePlaceholderAnnotationAttributes<Greeting> attrs =
                new ResolvablePlaceholderAnnotationAttributes<Greeting>(annotation, null);
        assertThat(attrs.annotationType()).isEqualTo(Greeting.class);
        assertThat(attrs.getString("message")).isEqualTo("${greeting.message}");
    }

    @Test
    void shouldResolvePlaceholdersInString() {
        PropertyResolver resolver = mock(PropertyResolver.class);
        when(resolver.resolvePlaceholders("${greeting.message}")).thenReturn("Hello");

        Greeting annotation = AnnotatedClass.class.getAnnotation(Greeting.class);
        ResolvablePlaceholderAnnotationAttributes<Greeting> attrs =
                new ResolvablePlaceholderAnnotationAttributes<Greeting>(annotation, resolver);
        assertThat(attrs.getString("message")).isEqualTo("Hello");
    }

    @Test
    void shouldResolvePlaceholdersInStringArray() {
        PropertyResolver resolver = mock(PropertyResolver.class);
        when(resolver.resolvePlaceholders("${tag.a}")).thenReturn("TagA");
        when(resolver.resolvePlaceholders("${tag.b}")).thenReturn("TagB");

        Tags annotation = AnnotatedTags.class.getAnnotation(Tags.class);
        ResolvablePlaceholderAnnotationAttributes<Tags> attrs =
                new ResolvablePlaceholderAnnotationAttributes<Tags>(annotation, resolver);
        assertThat(attrs.getStringArray("value")).containsExactly("TagA", "TagB");
    }

    @Test
    void shouldCopyFromGenericAnnotationAttributes() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("message", "plain");
        GenericAnnotationAttributes<Greeting> source = new GenericAnnotationAttributes<Greeting>(map, Greeting.class);

        ResolvablePlaceholderAnnotationAttributes<Greeting> attrs =
                ResolvablePlaceholderAnnotationAttributes.of(source, (PropertyResolver) null);
        assertThat(attrs.annotationType()).isEqualTo(Greeting.class);
        assertThat(attrs.getString("message")).isEqualTo("plain");
    }

    @Test
    void shouldReturnSameInstanceForResolvablePlaceholderInOf() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("message", "plain");
        ResolvablePlaceholderAnnotationAttributes<Greeting> source =
                new ResolvablePlaceholderAnnotationAttributes<Greeting>(map, Greeting.class, null);

        ResolvablePlaceholderAnnotationAttributes<Greeting> result =
                ResolvablePlaceholderAnnotationAttributes.of(source, (PropertyResolver) null);
        assertThat(result).isSameAs(source);
    }

    @Test
    void shouldCreateFromMapWithResolver() {
        PropertyResolver resolver = mock(PropertyResolver.class);
        when(resolver.resolvePlaceholders("${env.msg}")).thenReturn("Resolved");

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("message", "${env.msg}");

        ResolvablePlaceholderAnnotationAttributes<Greeting> attrs =
                new ResolvablePlaceholderAnnotationAttributes<Greeting>(map, Greeting.class, resolver);
        assertThat(attrs.getString("message")).isEqualTo("Resolved");
    }

    @Test
    void shouldReturnUnresolvedValuesWhenResolverIsNull() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("message", "${placeholder}");

        ResolvablePlaceholderAnnotationAttributes<Greeting> attrs =
                new ResolvablePlaceholderAnnotationAttributes<Greeting>(map, Greeting.class, null);
        assertThat(attrs.getString("message")).isEqualTo("${placeholder}");
    }

    @Test
    void shouldHandleEmptyAttributes() {
        Map<String, Object> map = new HashMap<String, Object>();
        ResolvablePlaceholderAnnotationAttributes<Greeting> attrs =
                new ResolvablePlaceholderAnnotationAttributes<Greeting>(map, Greeting.class, null);
        assertThat(attrs).isEmpty();
    }

    @Test
    void shouldUseOfFactoryMethodFromAnnotation() {
        Greeting annotation = AnnotatedClass.class.getAnnotation(Greeting.class);
        ResolvablePlaceholderAnnotationAttributes<Greeting> attrs =
                ResolvablePlaceholderAnnotationAttributes.of(annotation, null);
        assertThat(attrs.annotationType()).isEqualTo(Greeting.class);
    }

    @Greeting(message = "${greeting.message}")
    static class AnnotatedClass {
    }

    @Tags({"${tag.a}", "${tag.b}"})
    static class AnnotatedTags {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Greeting {
        String message();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Tags {
        String[] value();
    }
}
