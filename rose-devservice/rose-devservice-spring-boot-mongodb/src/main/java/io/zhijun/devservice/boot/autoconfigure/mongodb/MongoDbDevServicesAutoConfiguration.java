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
import io.zhijun.devservice.core.api.provider.DevServiceCategories;

/**
 * MongoDB dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServiceAutoConfiguration.class)
@org.springframework.boot.autoconfigure.AutoConfigureBefore({
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
@ConditionalOnDevServiceEnabled("mongodb")
@EnableConfigurationProperties(MongoDbDevServiceProperties.class)
@Import(MongoDbDevServicesAutoConfiguration.MongoDbDevServiceRegistrar.class)
public final class MongoDbDevServicesAutoConfiguration {

    private static final DevServiceConnectorDescriptor<MongoDbDevServiceProperties, RoseMongoDbContainer> DESCRIPTOR =
            DevServiceConnectorDescriptor.<MongoDbDevServiceProperties, RoseMongoDbContainer>builder()
                    .propertiesType(MongoDbDevServiceProperties.class)
                    .configPrefix(MongoDbDevServiceProperties.CONFIG_PREFIX)
                    .serviceName("mongodb")
                    .displayName("MongoDB Dev Service")
                    .category(DevServiceCategories.MONGODB)
                    .containerClass(RoseMongoDbContainer.class)
                    .containerFactory(RoseMongoDbContainer::new)
                    .dynamicProperties(registrar -> registrar.addDynamicProperty("spring.data.mongodb.uri",
                            () -> registrar.requireRunningContainer().getReplicaSetUrl()))
                    .build();

    static final class MongoDbDevServiceRegistrar
            extends ContainerDevServiceRegistrar<MongoDbDevServiceProperties, RoseMongoDbContainer> {

        MongoDbDevServiceRegistrar() {
            super(DESCRIPTOR);
        }
    }
}
