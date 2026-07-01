package io.zhijun.spring.config.context.annotation;

import io.zhijun.spring.context.annotation.AnnotatedBeanCapableImportSelector;
import io.zhijun.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import io.zhijun.spring.core.env.PropertySourcesUtils;
import io.zhijun.spring.core.io.support.PropertiesUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.type.AnnotationMetadata;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static io.zhijun.spring.core.env.PropertySourcesUtils.DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME;

/**
 * Loader for {@link DefaultPropertiesPropertySource}.
 */
class DefaultPropertiesPropertySourceLoader extends AnnotatedBeanCapableImportSelector<DefaultPropertiesPropertySource> {

    @Override
    protected void selectImports(AnnotationMetadata metadata,
                                 ResolvablePlaceholderAnnotationAttributes<DefaultPropertiesPropertySource> annotationAttributes,
                                 Set<String> imports) {
        loadPropertySource(annotationAttributes);
    }

    protected void loadPropertySource(AnnotationAttributes attributes) {
        Map<String, Object> defaultProperties = PropertySourcesUtils.getDefaultProperties(environment);
        if (defaultProperties == null) {
            defaultProperties = new LinkedHashMap<String, Object>();
            environment.getPropertySources().addLast(new MapPropertySource(DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME, defaultProperties));
        }
        try {
            loadPropertySourceFromLocations(attributes, defaultProperties);
            loadPropertySourceFromProperties(attributes, defaultProperties);
        } catch (Throwable ex) {
            throw new IllegalStateException("Failed to load default properties", ex);
        }
    }

    void loadPropertySourceFromLocations(AnnotationAttributes attributes, Map<String, Object> defaultProperties) throws Throwable {
        if (attributes == null) {
            return;
        }
        String[] locations = attributes.getStringArray("locations");
        if (locations.length == 0) {
            return;
        }
        String propertySourceName = DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME + "@" + hashCode();
        ResourcePropertySourceLoader delegate = getDelegate();
        PropertySourceExtensionAttributes<ResourcePropertySource> extensionAttributes = buildExtensionAttributes(attributes);
        PropertySource<?> propertySource = delegate.loadPropertySource(extensionAttributes, propertySourceName);
        loadPropertySource(propertySource, defaultProperties);
    }

    void loadPropertySource(PropertySource<?> propertySource, Map<String, Object> defaultProperties) {
        if (propertySource instanceof EnumerablePropertySource) {
            EnumerablePropertySource<?> enumerable = (EnumerablePropertySource<?>) propertySource;
            for (String propertyName : enumerable.getPropertyNames()) {
                defaultProperties.put(propertyName, enumerable.getProperty(propertyName));
            }
        }
    }

    void loadPropertySourceFromProperties(AnnotationAttributes attributes, Map<String, Object> defaultProperties) throws IOException {
        if (attributes == null) {
            return;
        }
        String[] propertiesValue = attributes.getStringArray("properties");
        if (propertiesValue.length == 0) {
            return;
        }
        Properties properties = PropertiesUtils.loadProperties(propertiesValue, environment);
        for (String propertyName : properties.stringPropertyNames()) {
            defaultProperties.put(propertyName, properties.getProperty(propertyName));
        }
    }

    private PropertySourceExtensionAttributes<ResourcePropertySource> buildExtensionAttributes(AnnotationAttributes attributes) {
        AnnotationAttributes resourceAttributes = new AnnotationAttributes();
        resourceAttributes.put("name", "");
        resourceAttributes.put("autoRefreshed", false);
        resourceAttributes.put("first", false);
        resourceAttributes.put("before", "");
        resourceAttributes.put("after", "");
        resourceAttributes.put("value", attributes.getStringArray("locations"));
        resourceAttributes.put("resourceComparator", attributes.getClass("resourceComparator"));
        resourceAttributes.put("ignoreResourceNotFound", attributes.getBoolean("ignoreResourceNotFound"));
        resourceAttributes.put("encoding", attributes.getString("encoding"));
        resourceAttributes.put("factory", attributes.getClass("factory"));
        return new PropertySourceExtensionAttributes<ResourcePropertySource>(resourceAttributes,
                ResourcePropertySource.class, environment);
    }

    private ResourcePropertySourceLoader getDelegate() {
        ResourcePropertySourceLoader delegate = new ResourcePropertySourceLoader();
        delegate.setEnvironment(getEnvironment());
        delegate.setBeanFactory(getBeanFactory());
        delegate.setResourceLoader(getResourceLoader());
        delegate.setBeanClassLoader(getClassLoader());
        try {
            delegate.afterPropertiesSet();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return delegate;
    }
}
