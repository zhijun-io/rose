package io.zhijun.spring.env;

import io.zhijun.core.annotation.Nullable;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

public abstract class EnvironmentUtils {

    public static ConfigurableEnvironment asConfigurableEnvironment(Environment environment) {
        if (environment instanceof ConfigurableEnvironment) {
            return (ConfigurableEnvironment) environment;
        }
        return null;
    }

    @Nullable
    public static ConversionService getConversionService(Environment environment) {
        ConfigurableEnvironment ce = asConfigurableEnvironment(environment);
        if (ce != null && ce.getConversionService() != null) {
            return ce.getConversionService();
        }
        return null;
    }
}
