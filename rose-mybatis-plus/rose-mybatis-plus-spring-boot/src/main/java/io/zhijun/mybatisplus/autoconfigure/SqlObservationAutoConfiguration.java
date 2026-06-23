package io.zhijun.mybatisplus.autoconfigure;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.MeterRegistry;
import io.zhijun.mybatisplus.observation.SqlObservationInterceptor;

/**
 * Auto-configuration for {@link SqlObservationInterceptor}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnMybatisPlusEnabled
@ConditionalOnClass(SqlObservationInterceptor.class)
@ConditionalOnProperty(prefix = SqlObservationProperties.CONFIG_PREFIX, name = "enabled", matchIfMissing = true)
@Conditional(SqlObservationAutoConfiguration.OnTracerOrMeterRegistry.class)
@EnableConfigurationProperties(SqlObservationProperties.class)
public final class SqlObservationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SqlObservationInterceptor.class)
    SqlObservationInterceptor sqlObservationInterceptor(ObjectProvider<MeterRegistry> meterRegistryProvider,
            ObjectProvider<io.opentelemetry.api.trace.Tracer> tracerProvider) {
        return new SqlObservationInterceptor(tracerProvider.getIfAvailable(), meterRegistryProvider.getIfAvailable());
    }

    static final class OnTracerOrMeterRegistry extends AnyNestedCondition {

        OnTracerOrMeterRegistry() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnBean(MeterRegistry.class)
        static final class OnMeterRegistry {
        }

        @ConditionalOnClass(name = "io.opentelemetry.api.trace.Tracer")
        @ConditionalOnBean(type = "io.opentelemetry.api.trace.Tracer")
        static final class OnTracer {
        }
    }

}
