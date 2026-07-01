package io.zhijun.spring.config.context.annotation;

import io.zhijun.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.jspecify.annotations.Nullable;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.Map;

/**
 * Attribute adapter for annotations meta-annotated with {@link PropertySourceExtension}.
 */
public class PropertySourceExtensionAttributes<A extends Annotation> extends ResolvablePlaceholderAnnotationAttributes<A> {

    private static final Class<PropertySourceExtension> EXTENSION_TYPE = PropertySourceExtension.class;

    public PropertySourceExtensionAttributes(Map<String, Object> source, Class<A> annotationType,
                                             @Nullable PropertyResolver propertyResolver) {
        super(source, validateAnnotationType(annotationType), propertyResolver);
    }

    static <A> Class<A> validateAnnotationType(Class<A> annotationType) {
        if (!annotationType.isAnnotationPresent(EXTENSION_TYPE)) {
            throw new IllegalArgumentException("The annotation type '" + annotationType.getName()
                    + "' must be meta-annotated by '" + EXTENSION_TYPE.getName() + "'");
        }
        return annotationType;
    }

    public final String getName() {
        return getString("name");
    }

    public final boolean isAutoRefreshed() {
        return getBoolean("autoRefreshed");
    }

    public final boolean isFirstPropertySource() {
        return getBoolean("first");
    }

    public final String getBeforePropertySourceName() {
        return getString("before");
    }

    public final String getAfterPropertySourceName() {
        return getString("after");
    }

    public final Class<A> getAnnotationType() {
        return (Class<A>) annotationType();
    }

    public final String[] getValue() {
        return getStringArray("value");
    }

    public final Class<? extends Comparator<Resource>> getResourceComparatorClass() {
        return getClass("resourceComparator");
    }

    public final boolean isIgnoreResourceNotFound() {
        return getBoolean("ignoreResourceNotFound");
    }

    public final String getEncoding() {
        return getString("encoding");
    }

    public final Class<? extends PropertySourceFactory> getPropertySourceFactoryClass() {
        return getClass("factory");
    }
}
