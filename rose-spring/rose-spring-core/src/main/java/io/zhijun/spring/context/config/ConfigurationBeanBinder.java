package io.zhijun.spring.context.config;

import java.util.Map;

import io.zhijun.spring.beans.factory.annotation.EnableConfigurationBeanBinding;
import org.springframework.core.convert.ConversionService;

/**
 * Binds configuration properties to a configuration bean.
 */
public interface ConfigurationBeanBinder {

    void bind(Map<String, Object> configurationProperties, boolean ignoreUnknownFields, boolean ignoreInvalidFields,
            Object configurationBean);

    void setConversionService(ConversionService conversionService);
}
