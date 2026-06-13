package io.zhijun.dev.services.mongodb;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.dev.services.api.provider.DevServiceCategories;
import io.zhijun.dev.services.api.provider.DevServiceProvider;
import io.zhijun.dev.services.core.autoconfigure.ConditionalOnDevServicesEnabled;
import io.zhijun.dev.services.core.autoconfigure.DevServicesAutoConfiguration;
import io.zhijun.dev.services.core.registration.DevServicesRegistrar;
import io.zhijun.dev.services.core.registration.DevServicesRegistry;
import io.zhijun.dev.services.mongodb.MongoDbDevServicesAutoConfiguration.MongoDbDevServicesRegistrar;

/**
 * MongoDB dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(DevServicesAutoConfiguration.class)
@org.springframework.boot.autoconfigure.AutoConfigureBefore({
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
@ConditionalOnDevServicesEnabled("mongodb")
@EnableConfigurationProperties(MongoDbDevServicesProperties.class)
@Import(MongoDbDevServicesRegistrar.class)
public final class MongoDbDevServicesAutoConfiguration {

    @Bean
    DevServiceProvider mongoDbDevServiceProvider() {
        return DevServiceProvider.of("mongodb", DevServiceCategories.MONGODB);
    }

    static class MongoDbDevServicesRegistrar extends DevServicesRegistrar {

        @Override
        protected void registerDevServices(DevServicesRegistry registry, Environment environment) {
            final MongoDbDevServicesProperties properties = bindProperties(
                    MongoDbDevServicesProperties.CONFIG_PREFIX, MongoDbDevServicesProperties.class);

            registry.registerDevService(new java.util.function.Consumer<DevServicesRegistry.ServiceSpec>() {
                @Override
                public void accept(DevServicesRegistry.ServiceSpec service) {
                    service
                            .name("mongodb")
                            .description("MongoDB Dev Service")
                            .container(new java.util.function.Consumer<DevServicesRegistry.ContainerSpec>() {
                                @Override
                                public void accept(DevServicesRegistry.ContainerSpec container) {
                                    container
                                            .type(RoseMongoDbContainer.class)
                                            .supplier(new java.util.function.Supplier<org.testcontainers.containers.Container<?>>() {
                                                @Override
                                                public org.testcontainers.containers.Container<?> get() {
                                                    return new RoseMongoDbContainer(properties);
                                                }
                                            });
                                }
                            });
                }
            });

            addDynamicProperty("spring.data.mongodb.uri", new java.util.function.Supplier<Object>() {
                @Override
                public Object get() {
                    return mongoContainer().getReplicaSetUrl();
                }
            });
        }

        private RoseMongoDbContainer mongoContainer() {
            RoseMongoDbContainer container = getBeanFactory().getBean(RoseMongoDbContainer.class);
            if (!container.isRunning()) {
                container.start();
            }
            return container;
        }
    }
}
