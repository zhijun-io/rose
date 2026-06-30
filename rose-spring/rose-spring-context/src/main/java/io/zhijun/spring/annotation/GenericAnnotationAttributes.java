package io.zhijun.spring.annotation;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Arrays;

import org.jspecify.annotations.Nullable;
import org.springframework.core.annotation.AnnotationAttributes;

/**
 * 泛型化的 {@link AnnotationAttributes}，携带准确的注解类型信息。
 *
 * @param <A> 注解类型
 */
public class GenericAnnotationAttributes<A extends Annotation> extends AnnotationAttributes {

    private static final long serialVersionUID = 1L;

    private final Class<A> annotationType;

    /**
     * 从注解实例创建。
     */
    public GenericAnnotationAttributes(A annotation) {
        super(org.springframework.core.annotation.AnnotationUtils.getAnnotationAttributes(annotation));
        this.annotationType = (Class<A>) annotation.annotationType();
    }

    /**
     * 从属性 Map 和注解类型创建。
     */
    public GenericAnnotationAttributes(Map<String, Object> map, Class<A> annotationType) {
        super(map);
        if (annotationType == null) {
            throw new IllegalArgumentException("annotationType must not be null");
        }
        this.annotationType = annotationType;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return annotationType;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof AnnotationAttributes)) return false;
        AnnotationAttributes that = (AnnotationAttributes) o;
        if (!annotationType().equals(that.annotationType())) return false;
        if (size() != that.size()) return false;
        for (Entry<String, Object> entry : entrySet()) {
            if (!Objects.deepEquals(entry.getValue(), that.get(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int h = annotationType.hashCode();
        for (Entry<String, Object> entry : entrySet()) {
            h = 31 * h + entry.getKey().hashCode();
            Object v = entry.getValue();
            h = 31 * h + (v != null ? deepHash(v) : 0);
        }
        return h;
    }

    private static int deepHash(Object value) {
        if (value.getClass().isArray()) {
            return Arrays.deepHashCode(new Object[]{value});
        }
        return value.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("@").append(annotationType.getName()).append("(");
        boolean first = true;
        for (Entry<String, Object> entry : entrySet()) {
            if (!first) sb.append(", ");
            first = false;
            sb.append(entry.getKey()).append("=");
            Object v = entry.getValue();
            if (v instanceof String) {
                sb.append('"').append(v).append('"');
            } else if (v != null && v.getClass().isArray()) {
                sb.append(java.util.Arrays.deepToString(new Object[]{v}));
            } else {
                sb.append(v);
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public static <A extends Annotation> GenericAnnotationAttributes<A> of(A annotation) {
        return new GenericAnnotationAttributes<>(annotation);
    }

    public static <A extends Annotation> GenericAnnotationAttributes<A> of(Map<String, Object> map, Class<A> annotationType) {
        if (map instanceof GenericAnnotationAttributes) {
            return (GenericAnnotationAttributes<A>) map;
        }
        return new GenericAnnotationAttributes<>(map, annotationType);
    }
}
