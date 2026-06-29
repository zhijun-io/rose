package io.zhijun.spring.core.binder.config;

import java.util.Map;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.convert.ConversionService;
import org.springframework.validation.DataBinder;

/**
 * Applies a flat property map onto a configuration bean using Spring {@link DataBinder}.
 */
public final class ConfigurationBeanBinder {

    private ConversionService conversionService;

    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public void bind(Map<String, Object> configurationProperties, boolean ignoreUnknownFields,
            boolean ignoreInvalidFields, Object configurationBean) {
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
