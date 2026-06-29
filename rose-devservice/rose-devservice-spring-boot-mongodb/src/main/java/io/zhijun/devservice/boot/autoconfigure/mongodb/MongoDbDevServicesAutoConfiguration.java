package io.zhijun.devservice.boot.autoconfigure.mongodb;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;
import io.zhijun.devservice.boot.registration.ContainerDevServiceRegistrar;
import io.zhijun.devservice.boot.registration.DevServiceConnectorDescriptor;
import io.zhijun.devservice.core.api.provider.DevServiceCategory;

/**
 * MongoDB dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@org.springframework.boot.autoconfigure.AutoConfigureBefore({
    MongoAutoConfiguration.class,
    MongoDataAutoConfiguration.class
})
@ConditionalOnDevServiceEnabled(MongoDbDevServiceProperties.SERVICE_NAME)
@EnableConfigurationProperties(MongoDbDevServiceProperties.class)
@Import(MongoDbDevServicesAutoConfiguration.MongoDbDevServiceRegistrar.class)
public final class MongoDbDevServicesAutoConfiguration {

    private static final DevServiceConnectorDescriptor<MongoDbDevServiceProperties, MongoDbContainer> DESCRIPTOR =
            DevServiceConnectorDescriptor.<MongoDbDevServiceProperties, MongoDbContainer>builder()
                    .propertiesType(MongoDbDevServiceProperties.class)
                    .configPrefix(MongoDbDevServiceProperties.CONFIG_PREFIX)
                    .serviceName(MongoDbDevServiceProperties.SERVICE_NAME)
                    .displayName("MongoDB Dev Service")
                    .category(DevServiceCategory.MONGODB)
                    .containerClass(MongoDbContainer.class)
                    .containerFactory(MongoDbContainer::new)
                    .dynamicProperties(registrar -> registrar.addDynamicProperty(
                            "spring.data.mongodb.uri",
                            () -> registrar.requireRunningContainer().getReplicaSetUrl()))
                    .build();

    static final class MongoDbDevServiceRegistrar
            extends ContainerDevServiceRegistrar<MongoDbDevServiceProperties, MongoDbContainer> {

        MongoDbDevServiceRegistrar() {
            super(DESCRIPTOR);
        }
    }
}
