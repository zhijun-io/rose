package io.zhijun.spring.config.context.annotation;

import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Loader for repeatable {@link DefaultPropertiesPropertySource}.
 */
class DefaultPropertiesPropertySourcesLoader extends AnnotatedPropertySourceLoader<DefaultPropertiesPropertySources> {

    @Override
    protected void loadPropertySource(AnnotationAttributes attributes, AnnotationMetadata metadata,
                                      String propertySourceName, MutablePropertySources propertySources) {
        AnnotationAttributes[] elements = attributes.getAnnotationArray("value");
        DefaultPropertiesPropertySourceLoader delegate = getDelegate();
        for (AnnotationAttributes element : elements) {
            delegate.loadPropertySource(element);
        }
    }

    private DefaultPropertiesPropertySourceLoader getDelegate() {
        DefaultPropertiesPropertySourceLoader delegate = new DefaultPropertiesPropertySourceLoader();
        delegate.setEnvironment(getEnvironment());
        delegate.setBeanFactory(getBeanFactory());
        delegate.setResourceLoader(getResourceLoader());
        delegate.setBeanClassLoader(getClassLoader());
        return delegate;
    }
}
