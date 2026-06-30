package io.zhijun.spring.config.binder;

import java.util.Map;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.convert.ConversionService;
import org.springframework.validation.DataBinder;

/**
 * Default {@link ConfigurationBeanBinder} implementation backed by Spring {@link DataBinder}.
 * <p>
 * Applies a flat property map (prefix already stripped) directly onto the target bean using
 * direct field access.
 *
 * @see ConfigurationBeanBinder
 * @see DataBinder
 */
public final class DefaultConfigurationBeanBinder implements ConfigurationBeanBinder {

    private ConversionService conversionService;

    @Override
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public void bind(
            Map<String, Object> configurationProperties,
            boolean ignoreUnknownFields,
            boolean ignoreInvalidFields,
            Object configurationBean) {
        DataBinder dataBinder = new DataBinder(configurationBean);
        dataBinder.setIgnoreUnknownFields(ignoreUnknownFields);
        dataBinder.setIgnoreInvalidFields(ignoreInvalidFields);
        dataBinder.initDirectFieldAccess();
        if (conversionService != null) {
            dataBinder.setConversionService(conversionService);
        }
        dataBinder.bind(new MutablePropertyValues(configurationProperties));
    }
}
