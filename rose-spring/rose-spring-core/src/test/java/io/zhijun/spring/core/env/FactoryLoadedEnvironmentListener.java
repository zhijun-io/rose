package io.zhijun.spring.core.env;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

public class FactoryLoadedEnvironmentListener implements EnvironmentListener {

    private static final List<String> CALLBACKS = new ArrayList<String>();

    public static void reset() {
        CALLBACKS.clear();
    }

    public static List<String> callbacks() {
        return new ArrayList<String>(CALLBACKS);
    }

    @Override
    public void beforeGetPropertySources(ConfigurableEnvironment environment) {
        CALLBACKS.add("beforeGetPropertySources");
    }

    @Override
    public void afterGetPropertySources(ConfigurableEnvironment environment, MutablePropertySources propertySources) {
        CALLBACKS.add("afterGetPropertySources");
    }
}
