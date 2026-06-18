package io.zhijun.spring.core.binder.config;

import org.springframework.core.Ordered;

/**
 * Customizes a configuration bean after binding.
 */
public interface ConfigurationBeanCustomizer extends Ordered {

    void customize(String beanName, Object configurationBean);
}
