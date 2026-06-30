package io.zhijun.spring.boot.actuate;

import io.zhijun.spring.boot.actuate.endpoint.ArtifactsEndpoint;
import io.zhijun.spring.boot.actuate.endpoint.ConfigurationMetadataEndpoint;
import io.zhijun.spring.boot.actuate.endpoint.ConfigurationPropertiesEndpoint;
import io.zhijun.spring.boot.actuate.endpoint.WebEndpoints;
import io.zhijun.spring.boot.props.metadata.ConfigurationMetadataReader;
import io.zhijun.spring.boot.props.metadata.ConfigurationMetadataRepository;
import io.zhijun.spring.boot.autoconfigure.condition.ConditionalOnConfigurationProcessorPresent;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.annotation.Endpoint")
@Import(value = { ActuatorEndpointsAutoConfiguration.ConfigurationProcessorConfiguration.class })
public class ActuatorEndpointsAutoConfiguration implements BeanClassLoaderAware {

    private ClassLoader classLoader;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnAvailableEndpoint
    public ArtifactsEndpoint artifactsEndpoint() {
        return new ArtifactsEndpoint(classLoader);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication
    @ConditionalOnAvailableEndpoint
    public WebEndpoints webEndpoints(WebEndpointsSupplier webEndpointsSupplier) {
        return new WebEndpoints(webEndpointsSupplier);
    }

    @ConditionalOnConfigurationProcessorPresent
    static class ConfigurationProcessorConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ConfigurationMetadataReader configurationMetadataReader() {
            return new ConfigurationMetadataReader();
        }

        @Bean
        @ConditionalOnMissingBean
        public ConfigurationMetadataRepository configurationMetadataRepository(
                ConfigurationMetadataReader configurationMetadataReader) {
            return new ConfigurationMetadataRepository(configurationMetadataReader);
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnAvailableEndpoint
        public ConfigurationMetadataEndpoint configurationMetadataEndpoint(
                ConfigurationMetadataRepository configurationMetadataRepository) {
            return new ConfigurationMetadataEndpoint(configurationMetadataRepository);
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnAvailableEndpoint
        public ConfigurationPropertiesEndpoint configurationPropertiesEndpoint(
                ConfigurationMetadataRepository configurationMetadataRepository) {
            return new ConfigurationPropertiesEndpoint(configurationMetadataRepository);
        }
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
