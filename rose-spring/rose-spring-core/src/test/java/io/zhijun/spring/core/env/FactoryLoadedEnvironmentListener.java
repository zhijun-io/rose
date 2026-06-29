package io.zhijun.spring.core.env;

import java.util.ArrayList;
import java.util.List;

import io.zhijun.spring.core.env.event.PropertySourcesChangedEvent;
public class FactoryLoadedEnvironmentListener implements EnvironmentListener {

    private static final List<String> CALLBACKS = new ArrayList<String>();

    public static void reset() {
        CALLBACKS.clear();
    }

    public static List<String> callbacks() {
        return new ArrayList<String>(CALLBACKS);
    }

    @Override
    public void onPropertySourcesChanged(PropertySourcesChangedEvent event) {
        CALLBACKS.add("onPropertySourcesChanged");
    }
}
