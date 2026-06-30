package io.zhijun.spring.boot.actuate.endpoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.zhijun.spring.boot.props.metadata.ConfigurationMetadataRepository;
import io.zhijun.spring.boot.props.metadata.ConfigurationProperty;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.configurationprocessor.metadata.ItemMetadata;

@Endpoint(id = "configProperties")
public class ConfigurationPropertiesEndpoint {

    private final ConfigurationMetadataRepository configurationMetadataRepository;

    public ConfigurationPropertiesEndpoint(ConfigurationMetadataRepository configurationMetadataRepository) {
        this.configurationMetadataRepository = configurationMetadataRepository;
    }

    @ReadOperation
    public ConfigurationPropertiesDescriptor getConfigurationProperties() {
        ConfigurationPropertiesDescriptor descriptor = new ConfigurationPropertiesDescriptor();
        descriptor.configurationProperties.addAll(adaptFromConfigurationMetadataRepository());
        return descriptor;
    }

    private List<ConfigurationProperty> adaptFromConfigurationMetadataRepository() {
        Collection<ItemMetadata> properties = configurationMetadataRepository.getProperties();
        List<ConfigurationProperty> result = new ArrayList<>(properties.size());
        for (ItemMetadata property : properties) {
            result.add(adaptConfigurationProperty(property));
        }
        return result;
    }

    private static ConfigurationProperty adaptConfigurationProperty(ItemMetadata property) {
        ConfigurationProperty cp = new ConfigurationProperty(property.getName());
        cp.setType(property.getType() != null ? property.getType() : String.class.getName());
        cp.setDescription(property.getDescription());
        cp.setDefaultValue(property.getDefaultValue());
        cp.setSourceType(property.getSourceType());
        cp.setSourceMethod(property.getSourceMethod());
        return cp;
    }

    public static class ConfigurationPropertiesDescriptor {

        private final List<ConfigurationProperty> configurationProperties = new ArrayList<>();

        public List<ConfigurationProperty> getConfigurationProperties() {
            return configurationProperties;
        }
    }
}
