package io.zhijun.spring.core.env;

import io.zhijun.spring.core.env.PropertySourceChangedEvent;
import io.zhijun.spring.core.env.PropertySourcesChangedEvent;

/**
 * Listener for property-source change events published by
 * {@link ListenableMutablePropertySources}.
 */
public interface EnvironmentListener {

    default void onPropertySourceChanged(PropertySourceChangedEvent event) {}

    default void onPropertySourcesChanged(PropertySourcesChangedEvent event) {}
}
