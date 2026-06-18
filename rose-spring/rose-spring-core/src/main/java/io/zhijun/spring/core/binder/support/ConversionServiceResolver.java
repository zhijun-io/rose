package io.zhijun.spring.core.binder.support;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.format.support.DefaultFormattingConversionService;

/**
 * Resolves a {@link ConversionService} from the bean factory or creates a default instance.
 */
public class ConversionServiceResolver {

    private final ConfigurableListableBeanFactory beanFactory;

    public ConversionServiceResolver(ConfigurableListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public ConversionService resolve() {
        ConversionService conversionService = beanFactory.getConversionService();
        if (conversionService != null) {
            return conversionService;
        }
        if (beanFactory.containsBean(ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME)) {
            return beanFactory.getBean(ConversionService.class);
        }
        if (beanFactory.containsBean(ConfigurableApplicationContext.ENVIRONMENT_BEAN_NAME)) {
            ConfigurableEnvironment environment = beanFactory.getBean(ConfigurableEnvironment.class);
            if (environment.getConversionService() != null) {
                return environment.getConversionService();
            }
        }
        return new DefaultFormattingConversionService();
    }
}
