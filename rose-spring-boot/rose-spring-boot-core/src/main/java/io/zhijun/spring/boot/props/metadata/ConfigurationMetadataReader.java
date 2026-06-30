package io.zhijun.spring.boot.props.metadata;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.metadata.ConfigurationMetadata;
import org.springframework.boot.configurationprocessor.metadata.JsonMarshaller;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

public class ConfigurationMetadataReader implements ResourceLoaderAware {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationMetadataReader.class);

    public static final String METADATA_PATH = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
            + "/META-INF/spring-configuration-metadata.json";

    public static final String ADDITIONAL_METADATA_PATH = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
            + "/META-INF/additional-spring-configuration-metadata.json";

    private ResourcePatternResolver resourcePatternResolver;

    public ConfigurationMetadata read() {
        ConfigurationMetadata metadata = new ConfigurationMetadata();
        readMetadata(metadata, METADATA_PATH);
        readMetadata(metadata, ADDITIONAL_METADATA_PATH);
        return metadata;
    }

    private void readMetadata(ConfigurationMetadata metadata, String locationPattern) {
        ResourcePatternResolver resolver = getResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources(locationPattern);
            for (Resource resource : resources) {
                readMetadata(metadata, resource);
            }
        }
        catch (Exception e) {
            logger.error("Failed to read configuration metadata from pattern[{}]", locationPattern, e);
        }
    }

    private void readMetadata(ConfigurationMetadata metadata, Resource resource) throws Exception {
        JsonMarshaller jsonMarshaller = new JsonMarshaller();
        try (InputStream inputStream = resource.getInputStream()) {
            ConfigurationMetadata resourceMetadata = jsonMarshaller.read(inputStream);
            metadata.merge(resourceMetadata);
        }
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        if (resourceLoader instanceof ResourcePatternResolver) {
            this.resourcePatternResolver = (ResourcePatternResolver) resourceLoader;
        }
        else {
            this.resourcePatternResolver = new PathMatchingResourcePatternResolver(resourceLoader);
        }
    }

    public ResourcePatternResolver getResourcePatternResolver() {
        ResourcePatternResolver resolver = this.resourcePatternResolver;
        if (resolver == null) {
            resolver = new PathMatchingResourcePatternResolver();
        }
        return resolver;
    }
}
