package io.zhijun.spring.core.env;

import io.zhijun.spring.context.ConfigurableApplicationContextInitializer;
import io.zhijun.spring.core.PropertyConstants;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * {@link ConfigurableApplicationContextInitializer} 实现，用于初始化 {@link ListenableConfigurableEnvironment}。
 * 配置属性：{@code rose.spring.listenable-environment.enabled}（默认 false）。
 */
public class ListenableConfigurableEnvironmentInitializer extends ConfigurableApplicationContextInitializer {

    public static final String PROPERTY_NAME_PREFIX = "rose.spring.listenable-environment.";

    public static final String ENABLED_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "enabled";

    public static final boolean DEFAULT_ENABLED_PROPERTY_VALUE = false;

    @Override
    protected void initialize(ConfigurableApplicationContext context, ConfigurableEnvironment environment) {
        ClassLoader classLoader = context.getClassLoader();
        if (classLoader == null) {
            classLoader = ListenableConfigurableEnvironmentInitializer.class.getClassLoader();
        }
        context.setEnvironment(new ListenableConfigurableEnvironment(classLoader, environment));
    }

    @Override
    public String getEnabledPropertyName() {
        return ENABLED_PROPERTY_NAME;
    }

    @Override
    public boolean getDefaultEnabled() {
        return DEFAULT_ENABLED_PROPERTY_VALUE;
    }
}
