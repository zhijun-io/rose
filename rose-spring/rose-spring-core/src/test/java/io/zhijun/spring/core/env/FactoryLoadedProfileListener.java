package io.zhijun.spring.core.env;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

public class FactoryLoadedProfileListener implements ProfileListener {

    private static final List<String> CALLBACKS = new ArrayList<String>();

    public static void reset() {
        CALLBACKS.clear();
    }

    public static List<String> callbacks() {
        return new ArrayList<String>(CALLBACKS);
    }

    @Override
    public void beforeSetActiveProfiles(ConfigurableEnvironment environment, String[] profiles) {
        CALLBACKS.add("beforeSetActiveProfiles");
    }

    @Override
    public void afterGetActiveProfiles(Environment environment, String[] activeProfiles) {
        CALLBACKS.add("afterGetActiveProfiles");
    }
}
