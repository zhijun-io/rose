package io.zhijun.spring.context.config;


import io.zhijun.core.annotation.Nullable;
import io.zhijun.spring.config.binder.ConfigurationBeanBindingPostProcessor;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;

import java.util.Map;

/**
 * Binds configuration properties from an {@link Environment} (or a flat property map)
 * onto a configuration bean.
 *
 * @see DefaultConfigurationBeanBinder
 * @see ConfigurationBeanBindingPostProcessor
 */
public interface ConfigurationBeanBinder {

    /**
     * Bind the given flat property map onto {@code configurationBean}.
     *
     * @param configurationProperties flat property map (prefix already stripped)
     * @param ignoreUnknownFields     whether to skip fields not present on the target bean
     * @param ignoreInvalidFields     whether to skip type-mismatched fields
     * @param configurationBean       the target bean to populate
     */
    void bind(Map<String, Object> configurationProperties,
              boolean ignoreUnknownFields,
              boolean ignoreInvalidFields,
              Object configurationBean);

    /**
     * Optional: set the {@link ConversionService} used during binding.
     */
    default void setConversionService(@Nullable ConversionService conversionService) {
    }

}
