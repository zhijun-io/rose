package io.zhijun.devservice.boot.autoconfigure.mongodb;

import io.zhijun.devservice.boot.autoconfigure.DevServiceAutoConfiguration;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.zhijun.devservice.core.api.provider.DevServiceCategories;
import io.zhijun.devservice.core.api.provider.DevServiceProvider;
import io.zhijun.devservice.boot.autoconfigure.ConditionalOnDevServiceEnabled;
import io.zhijun.devservice.boot.registration.ContainerDevServiceRegistrar;
import io.zhijun.devservice.boot.autoconfigure.mongodb.MongoDbDevServicesAutoConfiguration.MongoDbDevServiceRegistrar;

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

    static class MongoDbDevServiceRegistrar
            extends ContainerDevServiceRegistrar<MongoDbDevServiceProperties, RoseMongoDbContainer> {

        @Override
        protected Class<MongoDbDevServiceProperties> getPropertiesType() {
            return MongoDbDevServiceProperties.class;
        }

        @Override
        protected String getConfigPrefix() {
            return MongoDbDevServiceProperties.CONFIG_PREFIX;
        }

        @Override
        protected String getServiceName() {
            return "mongodb";
        }

        @Override
        protected String getDisplayName() {
            return "MongoDB Dev Service";
        }

        @Override
        protected Class<RoseMongoDbContainer> getContainerClass() {
            return RoseMongoDbContainer.class;
        }

        @Override
        protected RoseMongoDbContainer createContainer(MongoDbDevServiceProperties properties) {
            return new RoseMongoDbContainer(properties);
        }

        @Override
        protected void registerDynamicProperties() {
            addDynamicProperty("spring.data.mongodb.uri", () -> requireRunningContainer().getReplicaSetUrl());
        }
    }
}
