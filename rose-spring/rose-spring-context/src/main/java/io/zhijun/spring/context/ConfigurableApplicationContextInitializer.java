package io.zhijun.spring.context;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Abstract base class for {@link ApplicationContextInitializer} implementations targeting
 * {@link ConfigurableApplicationContext}. Provides built-in enable/disable support via
 * configuration properties and automatic self-registration as a Spring bean.
 * <p>
 * Subclasses implement {@link #initialize(ConfigurableApplicationContext, ConfigurableEnvironment)}.
 * <p>
 * (借鉴 microsphere-spring {@code ConfigurableApplicationContextInitializer})
 *
 * @see ApplicationContextInitializer
 */
public abstract class ConfigurableApplicationContextInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public final void initialize(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();
        if (!isEnabled(context, environment)) {
            return;
        }
        initialize(context, environment);
        registerSelf(context);
    }

    /**
     * Perform custom initialization logic.
     */
    protected abstract void initialize(ConfigurableApplicationContext context, ConfigurableEnvironment environment);

    /**
     * Whether this initializer is enabled. Default is {@code true}.
     */
    protected boolean isEnabled(ConfigurableApplicationContext context, ConfigurableEnvironment environment) {
        return true;
    }

    /**
     * Register this instance as a singleton bean in the context.
     */
    protected void registerSelf(ConfigurableApplicationContext context) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        String beanName = getClass().getName();
        if (!beanFactory.containsSingleton(beanName)) {
            beanFactory.registerSingleton(beanName, this);
        }
    }
}
