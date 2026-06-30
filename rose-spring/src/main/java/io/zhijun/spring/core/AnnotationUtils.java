package io.zhijun.spring.core;

import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.MergedAnnotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Map;

import static org.springframework.core.annotation.AnnotationUtils.getAnnotationAttributes;

public abstract class AnnotationUtils {

    public static Class<? extends Annotation> findAnnotationType(String annotationTypeName) {
        try {
            Class<?> type = Class.forName(annotationTypeName);
            return type.asSubclass(Annotation.class);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static AnnotationAttributes getAnnotationAttributes(Annotation annotation) {
        return getAnnotationAttributes(annotation, false);
    }

    public static AnnotationAttributes getAnnotationAttributes(Annotation annotation, boolean nestedAnnotationsAsMap) {
        return ofAnnotationAttributes(
                org.springframework.core.annotation.AnnotationUtils.getAnnotationAttributes(annotation, nestedAnnotationsAsMap));
    }

    public static <T> T getAttribute(AnnotationAttributes attributes, String attributeName) {
        return (T) attributes.get(attributeName);
    }

    public static <T> T getAttribute(Map<String, Object> attributes, String attributeName) {
        return (T) attributes.get(attributeName);
    }

    public static <T> T getAttribute(Map<String, Object> attributes, String attributeName, Boolean defaultValue) {
        return (T) attributes.getOrDefault(attributeName, defaultValue);
    }

    public static <T> T getRequiredAttribute(AnnotationAttributes attributes, String attributeName) {
        if (!attributes.containsKey(attributeName)) {
            throw new IllegalArgumentException("Attribute '" + attributeName + "' not found in " + attributes);
        }
        return (T) attributes.get(attributeName);
    }

    public static <T> T getRequiredAttribute(Map<String, Object> attributes, String attributeName) {
        if (!attributes.containsKey(attributeName)) {
            throw new IllegalArgumentException("Attribute '" + attributeName + "' not found in " + attributes);
        }
        return (T) attributes.get(attributeName);
    }

    public static AnnotationAttributes ofAnnotationAttributes(Map<String, Object> map) {
        if (map instanceof AnnotationAttributes) {
            return (AnnotationAttributes) map;
        }
        return new AnnotationAttributes(map);
    }

    public static Class<? extends Annotation> findAnnotationType(AnnotationAttributes attributes) {
        String className = (String) attributes.get("annotationType");
        if (className != null) {
            return findAnnotationType(className);
        }
        return null;
    }

    public static AnnotationAttributes tryGetMergedAnnotation(AnnotatedElement element,
                                                               Class<? extends Annotation> annotationType) {
        MergedAnnotations annotations = MergedAnnotations.from(element);
        if (!annotations.isPresent(annotationType)) {
            return null;
        }
        return new AnnotationAttributes(annotations.get(annotationType).asMap());
    }
}
