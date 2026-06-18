package io.zhijun.spring.context.config;

import io.zhijun.spring.beans.factory.annotation.ConfigurationBeanBindingPostProcessor;
import org.springframework.core.Ordered;

/**
 * Customizes a configuration bean after binding.
 */
public interface ConfigurationBeanCustomizer extends Ordered {

    void customize(String beanName, Object configurationBean);
}
