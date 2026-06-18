package io.zhijun.spring.core.binder.config;

import java.util.Map;

import org.springframework.core.convert.ConversionService;

/**
 * Strategy for applying a flat property map onto a configuration bean.
 * <p>
 * Used by {@link io.zhijun.spring.core.binder.annotation.ConfigurationBeanBindingPostProcessor}.
 * Replace by registering a custom {@code ConfigurationBeanBinder} bean in the application context.
 */
public interface ConfigurationBeanBinder {

    /**
     * Binds {@code configurationProperties} onto {@code configurationBean}.
     * Keys are typically dotted paths produced by {@link io.zhijun.spring.core.env.PropertySourcesUtils}.
     */
    void bind(Map<String, Object> configurationProperties, boolean ignoreUnknownFields, boolean ignoreInvalidFields,
            Object configurationBean);

    void setConversionService(ConversionService conversionService);
}
