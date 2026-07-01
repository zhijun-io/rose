package io.zhijun.spring.config.context.annotation;

import io.zhijun.spring.context.annotation.AnnotatedBeanCapableImportSelector;
import io.zhijun.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Base import selector that loads property sources for a driving annotation.
 */
public abstract class AnnotatedPropertySourceLoader<A extends Annotation> extends AnnotatedBeanCapableImportSelector<A> {

    protected static final String NAME_ATTRIBUTE_NAME = "name";

    private String propertySourceName;

    @Override
    public final void selectImports(AnnotationMetadata metadata,
                                    ResolvablePlaceholderAnnotationAttributes<A> annotationAttributes,
                                    Set<String> imports) {
        String name = resolvePropertySourceName(annotationAttributes, metadata);
        this.propertySourceName = name;
        MutablePropertySources propertySources = getEnvironment().getPropertySources();
        try {
            loadPropertySource(annotationAttributes, metadata, name, propertySources);
        } catch (Throwable ex) {
            String message = "Failed to load PropertySource '" + name + "' for configuration class '"
                    + metadata.getClassName() + "' annotated with @" + annotationType.getName();
            throw new BeanCreationException(message, ex);
        }
    }

    protected final String resolvePropertySourceName(AnnotationAttributes attributes, AnnotationMetadata metadata) {
        String name = buildPropertySourceName(attributes, metadata);
        if (!StringUtils.hasText(name)) {
            name = buildDefaultPropertySourceName(attributes, metadata);
        }
        return name;
    }

    protected String buildPropertySourceName(AnnotationAttributes attributes, AnnotationMetadata metadata) {
        if (attributes.containsKey(NAME_ATTRIBUTE_NAME)) {
            return attributes.getString(NAME_ATTRIBUTE_NAME);
        }
        return null;
    }

    protected String buildDefaultPropertySourceName(AnnotationAttributes attributes, AnnotationMetadata metadata) {
        return metadata.getClassName() + "@" + annotationType.getName();
    }

    protected abstract void loadPropertySource(AnnotationAttributes attributes, AnnotationMetadata metadata,
                                               String propertySourceName, MutablePropertySources propertySources) throws Throwable;

    protected String getPropertySourceName() {
        return propertySourceName;
    }
}
