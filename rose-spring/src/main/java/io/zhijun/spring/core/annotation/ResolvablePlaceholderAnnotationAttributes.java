package io.zhijun.spring.core.annotation;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.PropertyResolver;
import io.zhijun.core.annotation.Nullable;

/**
 * 支持占位符解析的注解属性包装。
 * <p>
 * 将注解属性值中的 {@code ${...}} 占位符通过 {@link PropertyResolver} 解析为实际值。
 *
 * @param <A> 注解类型
 */
public class ResolvablePlaceholderAnnotationAttributes<A extends Annotation> extends GenericAnnotationAttributes<A> {

    private static final long serialVersionUID = 1L;

    /**
     * 从注解实例创建，解析其中的占位符。
     */
    public ResolvablePlaceholderAnnotationAttributes(A annotation, @Nullable PropertyResolver propertyResolver) {
        super(resolvePlaceholders(
                AnnotationUtils.getAnnotationAttributes(annotation),
                propertyResolver),
            (Class<A>) annotation.annotationType());
    }

    /**
     * 从 GenericAnnotationAttributes 复制并解析占位符。
     */
    public ResolvablePlaceholderAnnotationAttributes(GenericAnnotationAttributes<A> attributes, @Nullable PropertyResolver propertyResolver) {
        super(resolvePlaceholders(attributes, propertyResolver), (Class<A>) attributes.annotationType());
    }

    /**
     * 从属性 Map 和注解类型创建，解析其中的占位符。
     */
    public ResolvablePlaceholderAnnotationAttributes(Map<String, Object> map, Class<A> annotationType,
                                                     @Nullable PropertyResolver propertyResolver) {
        super(resolvePlaceholders(map, propertyResolver), annotationType);
    }

    // ---- 静态工厂 ----

    public static <A extends Annotation> ResolvablePlaceholderAnnotationAttributes<A> of(A annotation,
                                                                                         @Nullable PropertyResolver propertyResolver) {
        return new ResolvablePlaceholderAnnotationAttributes<>(annotation, propertyResolver);
    }

    public static <A extends Annotation> ResolvablePlaceholderAnnotationAttributes<A> of(GenericAnnotationAttributes<A> attributes,
                                                                                         @Nullable PropertyResolver propertyResolver) {
        if (attributes instanceof ResolvablePlaceholderAnnotationAttributes) {
            return (ResolvablePlaceholderAnnotationAttributes<A>) attributes;
        }
        return new ResolvablePlaceholderAnnotationAttributes<>(attributes, propertyResolver);
    }

    public static <A extends Annotation> ResolvablePlaceholderAnnotationAttributes<A> of(Map<String, Object> map,
                                                                                         Class<A> annotationType,
                                                                                         @Nullable PropertyResolver propertyResolver) {
        return new ResolvablePlaceholderAnnotationAttributes<>(map, annotationType, propertyResolver);
    }

    // ---- 占位符解析 ----

    @Nullable
    private static Map<String, Object> resolvePlaceholders(@Nullable Map<String, Object> source,
                                                           @Nullable PropertyResolver propertyResolver) {
        if (source == null || source.isEmpty() || propertyResolver == null) {
            return source;
        }
        Map<String, Object> copy = new LinkedHashMap<>(source);
        for (Map.Entry<String, Object> entry : copy.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                entry.setValue(propertyResolver.resolvePlaceholders((String) value));
            } else if (value instanceof String[]) {
                String[] values = (String[]) value;
                String[] resolved = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    resolved[i] = propertyResolver.resolvePlaceholders(values[i]);
                }
                entry.setValue(resolved);
            }
        }
        return copy;
    }
}
