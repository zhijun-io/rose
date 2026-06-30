package io.zhijun.spring.context.config;
import io.zhijun.spring.config.binder.ConfigurationBeanBinder;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.convert.ConversionService;
import org.springframework.validation.DataBinder;

import java.util.Map;

/**
 * The default {@link ConfigurationBeanBinder} implementation
 *
 * @see ConfigurationBeanBinder
 * @since 1.0.0
 */
public class DefaultConfigurationBeanBinder implements ConfigurationBeanBinder {

    private ConversionService conversionService;

    @Override
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public void bind(Map<String, Object> configurationProperties, boolean ignoreUnknownFields,
                     boolean ignoreInvalidFields, Object configurationBean) {
        DataBinder dataBinder = new DataBinder(configurationBean);
        // Set ignored*
        dataBinder.setIgnoreInvalidFields(ignoreUnknownFields);
        dataBinder.setIgnoreUnknownFields(ignoreInvalidFields);
        // Get properties under specified prefix from PropertySources
        // Convert Map to MutablePropertyValues
        MutablePropertyValues propertyValues = new MutablePropertyValues(configurationProperties);
        dataBinder.initDirectFieldAccess();
        dataBinder.setConversionService(conversionService);
        // Bind
        dataBinder.bind(propertyValues);
    }
}
