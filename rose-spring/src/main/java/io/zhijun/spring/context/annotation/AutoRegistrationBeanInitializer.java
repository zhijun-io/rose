
package io.zhijun.spring.context.annotation;

import io.zhijun.spring.beans.factory.support.BeanRegistrar;
import io.zhijun.spring.context.ConfigurableApplicationContextInitializer;
import io.zhijun.spring.context.config.AutoRegistrationBean;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static io.zhijun.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static io.zhijun.spring.constants.PropertyConstants.DEFAULT_AUTO_REGISTERED_VALUE;
import static io.zhijun.spring.context.annotation.EnableAutoRegistrationBean.BEANS_AUTO_REGISTERED_PROEPRTY_NAME;

/**
 * {@link ApplicationContextInitializer} class for {@link AutoRegistrationBean}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableAutoRegistrationBean
 * @see AutoRegistrationBean
 * @since 1.0.0
 */
public class AutoRegistrationBeanInitializer extends ConfigurableApplicationContextInitializer {

    @Override
    protected void initialize(ConfigurableApplicationContext context, ConfigurableEnvironment environment) {
        registerBeanDefinition((BeanDefinitionRegistry) context.getBeanFactory(), Config.class);
    }

    @EnableAutoRegistrationBean
    static class Config {
    }

    @Override
    public String getEnabledPropertyName() {
        return BEANS_AUTO_REGISTERED_PROEPRTY_NAME;
    }

    @Override
    public boolean getDefaultEnabled() {
        return DEFAULT_AUTO_REGISTERED_VALUE;
    }
}
