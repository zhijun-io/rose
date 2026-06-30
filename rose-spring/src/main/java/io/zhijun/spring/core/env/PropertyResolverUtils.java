package io.zhijun.spring.core.env;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;

public abstract class PropertyResolverUtils {

    public static PropertyResolver asPropertyResolver(Environment environment) {
        return environment;
    }
}
