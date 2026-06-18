package io.zhijun.boot.context.properties.bind;

import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;

/**
 * Listener for Spring Boot configuration property binding event.
 */
public interface RoseBindListener {

    default void onStart(ConfigurationPropertyName name, Bindable<?> target, BindContext context) {
    }

    default void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
    }

    default void onCreate(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
    }

    default void onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error) {
    }

    default void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
    }
}
