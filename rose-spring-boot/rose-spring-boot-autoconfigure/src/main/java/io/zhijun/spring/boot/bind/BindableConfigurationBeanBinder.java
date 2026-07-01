package io.zhijun.spring.boot.bind;

import io.zhijun.spring.context.config.ConfigurationBeanBinder;

import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver;
import org.springframework.boot.context.properties.bind.handler.IgnoreErrorsBindHandler;
import org.springframework.boot.context.properties.bind.handler.NoUnboundElementsBindHandler;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.UnboundElementsSourceFilter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Map;

/**
 * 基于 Spring Boot {@link Binder} 的 {@link ConfigurationBeanBinder} 实现，
 * 使用 {@link Bindable} 将配置属性绑定到目标 bean。
 * <p>
 * 相比 {@code DefaultConfigurationBeanBinder}（基于 {@code DataBinder}），
 * {@code Binder} 对嵌套属性、类型转换支持更好。
 * <p>
 * （借鉴 microsphere-spring-boot {@code BindableConfigurationBeanBinder}）
 *
 * @see ConfigurationBeanBinder
 * @see Binder
 */
public class BindableConfigurationBeanBinder implements ConfigurationBeanBinder {

    private ConversionService conversionService;

    @Override
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public void bind(Map<String, Object> configurationProperties,
                     boolean ignoreUnknownFields, boolean ignoreInvalidFields,
                     Object configurationBean) {

        Iterable<PropertySource<?>> propertySources =
                java.util.Collections.singletonList(
                        new MapPropertySource("internal", configurationProperties));

        Iterable<ConfigurationPropertySource> configurationPropertySources =
                org.springframework.boot.context.properties.source.ConfigurationPropertySources.from(propertySources);

        Bindable<?> bindable = Bindable.ofInstance(configurationBean);

        Binder binder = new Binder(configurationPropertySources,
                new PropertySourcesPlaceholdersResolver(propertySources),
                conversionService);

        BindHandler bindHandler = createBindHandler(ignoreUnknownFields, ignoreInvalidFields);

        binder.bind("", bindable, bindHandler);
    }

    private static BindHandler createBindHandler(boolean ignoreUnknownFields, boolean ignoreInvalidFields) {
        BindHandler handler = BindHandler.DEFAULT;
        if (ignoreInvalidFields) {
            handler = new IgnoreErrorsBindHandler(handler);
        }
        if (!ignoreUnknownFields) {
            handler = new NoUnboundElementsBindHandler(handler, new UnboundElementsSourceFilter());
        }
        return handler;
    }
}
