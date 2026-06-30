package io.zhijun.spring.boot.actuator.endpoint;

import java.util.Collection;

import io.zhijun.spring.boot.props.metadata.ConfigurationMetadataRepository;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.configurationprocessor.metadata.ItemMetadata;

@Endpoint(id = "configMetadata")
public class ConfigurationMetadataEndpoint {

    private final ConfigurationMetadataRepository configurationMetadataRepository;

    public ConfigurationMetadataEndpoint(ConfigurationMetadataRepository configurationMetadataRepository) {
        this.configurationMetadataRepository = configurationMetadataRepository;
    }

    @ReadOperation
    public ConfigurationMetadataDescriptor getConfigurationMetadata() {
        ConfigurationMetadataDescriptor descriptor = new ConfigurationMetadataDescriptor();
        descriptor.groups = this.configurationMetadataRepository.getGroups();
        descriptor.properties = this.configurationMetadataRepository.getProperties();
        return descriptor;
    }

    public static class ConfigurationMetadataDescriptor {

        private Collection<ItemMetadata> groups;

        private Collection<ItemMetadata> properties;

        public Collection<ItemMetadata> getGroups() {
            return groups;
        }

        public Collection<ItemMetadata> getProperties() {
            return properties;
        }
    }
}
