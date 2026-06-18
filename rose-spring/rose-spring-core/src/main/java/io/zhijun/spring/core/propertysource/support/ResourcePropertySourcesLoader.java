package io.zhijun.spring.core.propertysource.support;

import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import io.zhijun.spring.core.propertysource.annotation.ResourcePropertySources;

/**
 * Loader for {@link ResourcePropertySources} container annotation.
 */
public class ResourcePropertySourcesLoader extends AnnotatedPropertySourceImportSelector<ResourcePropertySources> {

    ResourcePropertySourcesLoader() {
        super(ResourcePropertySources.class);
    }

    @Override
    protected void loadPropertySource(AnnotationAttributes attributes, AnnotationMetadata metadata) {
        Class<?> importingClass = resolveImportingClass(metadata);
        ResourcePropertySourceLoader delegate = new ResourcePropertySourceLoader();
        delegate.copyContextFrom(this);
        for (AnnotationAttributes element : attributes.getAnnotationArray("value")) {
            delegate.loadPropertySource(importingClass, element);
        }
    }
}
