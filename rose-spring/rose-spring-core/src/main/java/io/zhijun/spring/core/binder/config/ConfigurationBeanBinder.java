package io.zhijun.spring.core.binder.config;

import java.util.Map;

import org.springframework.core.convert.ConversionService;

/**
 * Binds configuration properties to a configuration bean.
 */
public interface ConfigurationBeanBinder {

    void bind(Map<String, Object> configurationProperties, boolean ignoreUnknownFields, boolean ignoreInvalidFields,
            Object configurationBean);

    void setConversionService(ConversionService conversionService);
}
