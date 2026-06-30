package io.zhijun.spring.core.propertysource;

import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import io.zhijun.spring.core.propertysource.ResourcePropertySource;

/**
 * Loader for enhanced resource property sources.
 */
public class ResourcePropertySourceLoader extends AnnotatedPropertySourceImportSelector<ResourcePropertySource> {

    public ResourcePropertySourceLoader() {
        super(ResourcePropertySource.class);
    }

    @Override
    protected void loadPropertySource(AnnotationAttributes attributes, AnnotationMetadata metadata) {
        Class<?> importingClass = resolveImportingClass(metadata);
        loadPropertySource(importingClass, attributes);
    }

    void loadPropertySource(Class<?> importingClass, AnnotationAttributes attributes) {
        PropertySourceLoading.loadPropertySource(this, importingClass, attributes, ResourcePropertySource.class);
    }

    void copyContextFrom(AnnotatedPropertySourceImportSelector<?> source) {
        PropertySourceLoading.copyContextFrom(this, source);
    }
}
