package io.zhijun.spring.config.env.support;

import io.zhijun.spring.config.env.ImmutableMapPropertySource;
import io.zhijun.spring.config.env.config.ResourceYamlProcessor;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;

/**
 * {@link PropertySourceFactory} for loading YAML resources.
 *
 * @since 0.1.0
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        ResourceYamlProcessor processor = new ResourceYamlProcessor(resource.getResource());
        String propertySourceName = name != null ? name : resource.getResource().getDescription();
        return new ImmutableMapPropertySource(propertySourceName, processor.process());
    }
}
