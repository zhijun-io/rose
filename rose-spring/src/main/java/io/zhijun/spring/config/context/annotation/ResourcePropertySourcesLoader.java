package io.zhijun.spring.config.context.annotation;

import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Loader for {@link ResourcePropertySources}.
 */
class ResourcePropertySourcesLoader extends AnnotatedPropertySourceLoader<ResourcePropertySources> {

    @Override
    protected void loadPropertySource(AnnotationAttributes attributes, AnnotationMetadata metadata,
                                      String propertySourceName, MutablePropertySources propertySources) throws Throwable {
        AnnotationAttributes[] elements = attributes.getAnnotationArray("value");
        ResourcePropertySourceLoader delegate = getDelegate();
        for (AnnotationAttributes element : elements) {
            String name = resolvePropertySourceName(element, metadata);
            delegate.loadPropertySource(element, metadata, name, propertySources);
        }
    }

    private ResourcePropertySourceLoader getDelegate() {
        ResourcePropertySourceLoader delegate = new ResourcePropertySourceLoader();
        delegate.setEnvironment(getEnvironment());
        delegate.setBeanFactory(getBeanFactory());
        delegate.setResourceLoader(getResourceLoader());
        delegate.setBeanClassLoader(getClassLoader());
        return delegate;
    }
}
