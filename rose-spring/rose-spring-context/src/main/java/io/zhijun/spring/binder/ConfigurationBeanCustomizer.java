package io.zhijun.spring.binder;

import org.springframework.core.Ordered;

/**
 * Hook invoked after each bind (startup and env hot-reload) of an
 * {@link io.zhijun.spring.binder.EnableConfigurationBeanBinding} bean.
 * <p>
 * Implement {@link Ordered} to control callback order. Register as a Spring bean.
 */
public interface ConfigurationBeanCustomizer extends Ordered {

    void customize(String beanName, Object configurationBean);
}
