package io.zhijun.spring.core.env;

import io.zhijun.spring.context.ConfigurableApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static io.zhijun.spring.constants.PropertyConstants.LISTENABLE_ENVIRONMENT_PROPERTY_NAME_PREFIX;

/**
 * {@link ConfigurableApplicationContextInitializer} 实现，用于初始化 {@link ListenableConfigurableEnvironment}。
 * 配置属性：{@code rose.spring.listenable-environment.enabled}（默认 false）。
 */
public class ListenableConfigurableEnvironmentInitializer extends ConfigurableApplicationContextInitializer {

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
        return LISTENABLE_ENVIRONMENT_PROPERTY_NAME_PREFIX;
    }

    @Override
    public boolean getDefaultEnabled() {
        return false;
    }
}
