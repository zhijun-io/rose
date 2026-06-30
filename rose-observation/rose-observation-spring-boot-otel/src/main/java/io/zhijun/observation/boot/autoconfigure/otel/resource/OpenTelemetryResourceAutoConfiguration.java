package io.zhijun.observation.boot.autoconfigure.otel.resource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import io.opentelemetry.sdk.resources.Resource;
import io.zhijun.observation.boot.autoconfigure.otel.ConditionalOnOpenTelemetry;
import io.zhijun.observation.boot.autoconfigure.otel.resource.BuildResourceContributor;
import io.zhijun.observation.boot.autoconfigure.otel.resource.EnvironmentResourceContributor;
import io.zhijun.observation.boot.autoconfigure.otel.resource.HostResourceContributor;
import io.zhijun.observation.boot.autoconfigure.otel.resource.JavaResourceContributor;
import io.zhijun.observation.boot.autoconfigure.otel.resource.OsResourceContributor;
import io.zhijun.observation.boot.autoconfigure.otel.resource.ProcessResourceContributor;
import io.zhijun.observation.boot.autoconfigure.otel.resource.ResourceContributor;

/**
 * Auto-configuration for OpenTelemetry {@link Resource}.
 */
@AutoConfiguration(after = org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration.class)
@ConditionalOnOpenTelemetry
@EnableConfigurationProperties(OpenTelemetryResourceProperties.class)
public final class OpenTelemetryResourceAutoConfiguration {

    public static final int DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 10;

    @Bean
    @ConditionalOnMissingBean
    Resource resource(
            ObjectProvider<ResourceContributor> resourceContributors,
            ObjectProvider<OpenTelemetryResourceBuilderCustomizer> customizers,
            OpenTelemetryResourceProperties properties) {
        return new OpenTelemetryResourceTemplate(properties).build(resourceContributors, customizers);
    }

    @Bean
    @ConditionalOnOpenTelemetryResourceContributor(value = "build", matchIfMissing = true)
    @ConditionalOnBean(BuildProperties.class)
    @Order(DEFAULT_ORDER)
    BuildResourceContributor buildResourceContributor(BuildProperties properties) {
        return new BuildResourceContributor(properties);
    }

    @Bean
    @ConditionalOnOpenTelemetryResourceContributor(value = "environment", matchIfMissing = true)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    EnvironmentResourceContributor environmentResourceContributor(
            Environment environment, OpenTelemetryResourceProperties properties) {
        return new EnvironmentResourceContributor(environment, properties);
    }

    @Bean
    @ConditionalOnOpenTelemetryResourceContributor(value = "host")
    @Order(DEFAULT_ORDER)
    HostResourceContributor hostResourceContributor() {
        return new HostResourceContributor();
    }

    @Bean
    @ConditionalOnOpenTelemetryResourceContributor(value = "java")
    @Order(DEFAULT_ORDER)
    JavaResourceContributor javaRuntimeResourceContributor() {
        return new JavaResourceContributor();
    }

    @Bean
    @ConditionalOnOpenTelemetryResourceContributor(value = "os")
    @Order(DEFAULT_ORDER)
    OsResourceContributor osResourceContributor() {
        return new OsResourceContributor();
    }

    @Bean
    @ConditionalOnOpenTelemetryResourceContributor(value = "process")
    @Order(DEFAULT_ORDER)
    ProcessResourceContributor processRuntimeResourceContributor() {
        return new ProcessResourceContributor();
    }

    @Bean
    OpenTelemetryResourceBuilderCustomizer filterAttributes(OpenTelemetryResourceProperties properties) {
        return new OpenTelemetryResourceTemplate(properties).filterAttributes();
    }
}
