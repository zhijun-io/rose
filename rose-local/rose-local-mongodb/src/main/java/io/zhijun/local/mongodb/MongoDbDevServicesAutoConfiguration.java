package io.zhijun.local.mongodb;

import io.zhijun.local.core.autoconfigure.LocalServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.zhijun.local.api.provider.LocalServiceCategories;
import io.zhijun.local.api.provider.LocalServiceProvider;
import io.zhijun.local.core.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.local.core.registration.LocalServiceRegistrar;
import io.zhijun.local.core.registration.LocalServiceRegistry;
import io.zhijun.local.mongodb.MongoDbDevServicesAutoConfiguration.MongoDbLocalServiceRegistrar;

/**
 * MongoDB dev services auto-configuration.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(LocalServiceAutoConfiguration.class)
@org.springframework.boot.autoconfigure.AutoConfigureBefore({
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
@ConditionalOnDevServiceEnabled("mongodb")
@EnableConfigurationProperties(MongoDbLocalServiceProperties.class)
@Import(MongoDbLocalServiceRegistrar.class)
public final class MongoDbDevServicesAutoConfiguration {

    @Bean
    LocalServiceProvider mongoDbDevServiceProvider() {
        return LocalServiceProvider.of("mongodb", LocalServiceCategories.MONGODB);
    }

    static class MongoDbLocalServiceRegistrar extends LocalServiceRegistrar {

        @Override
        protected void registerDevServices(LocalServiceRegistry registry, Environment environment) {
            final MongoDbLocalServiceProperties properties = bindProperties(
                    MongoDbLocalServiceProperties.CONFIG_PREFIX, MongoDbLocalServiceProperties.class);

            registry.registerDevService(new java.util.function.Consumer<LocalServiceRegistry.ServiceSpec>() {
                @Override
                public void accept(LocalServiceRegistry.ServiceSpec service) {
                    service
                            .name("mongodb")
                            .description("MongoDB Dev Service")
                            .container(new java.util.function.Consumer<LocalServiceRegistry.ContainerSpec>() {
                                @Override
                                public void accept(LocalServiceRegistry.ContainerSpec container) {
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
