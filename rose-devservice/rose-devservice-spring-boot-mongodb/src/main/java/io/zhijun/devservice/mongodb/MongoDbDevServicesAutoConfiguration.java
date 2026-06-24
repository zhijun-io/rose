package io.zhijun.devservice.mongodb;

import io.zhijun.devservice.core.autoconfigure.DevServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.devservice.core.api.provider.DevServiceCategories;
import io.zhijun.devservice.core.api.provider.DevServiceProvider;
import io.zhijun.devservice.core.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.core.registration.DevServiceRegistrar;
import io.zhijun.devservice.core.registration.DevServiceRegistry;
import io.zhijun.devservice.mongodb.MongoDbDevServicesAutoConfiguration.MongoDbDevServiceRegistrar;

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
@Import(MongoDbDevServiceRegistrar.class)
public final class MongoDbDevServicesAutoConfiguration {

    @Bean
    DevServiceProvider mongoDbDevServiceProvider() {
        return DevServiceProvider.of("mongodb", DevServiceCategories.MONGODB);
    }

    static class MongoDbDevServiceRegistrar extends DevServiceRegistrar {

        @Override
        protected void registerDevServices(DevServiceRegistry registry, Environment environment) {
            final MongoDbDevServiceProperties properties = bindProperties(
                    MongoDbDevServiceProperties.CONFIG_PREFIX, MongoDbDevServiceProperties.class);

            registry.registerDevService(new java.util.function.Consumer<DevServiceRegistry.ServiceSpec>() {
                @Override
                public void accept(DevServiceRegistry.ServiceSpec service) {
                    service
                            .name("mongodb")
                            .description("MongoDB Dev Service")
                            .container(new java.util.function.Consumer<DevServiceRegistry.ContainerSpec>() {
                                @Override
                                public void accept(DevServiceRegistry.ContainerSpec container) {
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
