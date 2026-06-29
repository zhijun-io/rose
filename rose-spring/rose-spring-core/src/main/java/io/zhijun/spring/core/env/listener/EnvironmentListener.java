package io.zhijun.spring.core.env.listener;

import io.zhijun.spring.core.env.event.PropertySourceChangedEvent;
import io.zhijun.spring.core.env.event.PropertySourcesChangedEvent;

/**
 * Listener for property-source change events published by
 * {@link io.zhijun.spring.core.env.ListenableMutablePropertySources}.
 */
public interface EnvironmentListener {

    default void onPropertySourceChanged(PropertySourceChangedEvent event) {}

    default void onPropertySourcesChanged(PropertySourcesChangedEvent event) {}
}
