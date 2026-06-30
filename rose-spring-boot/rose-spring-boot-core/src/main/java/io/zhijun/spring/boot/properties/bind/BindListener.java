 package io.zhijun.spring.boot.properties.bind;

 import org.springframework.boot.context.properties.bind.BindContext;
 import org.springframework.boot.context.properties.bind.Bindable;
 import org.springframework.boot.context.properties.source.ConfigurationPropertyName;

 /**
 * Callback interface for the lifecycle of {@link org.springframework.boot.context.properties.bind.Binder#bind binding}.
 * <p>
 * Implementations can be registered as Spring beans and are automatically discovered by
 * {@link io.zhijun.spring.boot.properties.ListenableConfigurationPropertiesBindHandlerAdvisor}.
 * <p>
 * Inspired by {@code io.microsphere.spring.boot.context.properties.bind.BindListener}.
 */
 public interface BindListener {

     default void onStart(ConfigurationPropertyName name, Bindable<?> target, BindContext context) {
     }

     default Object onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
         return result;
     }

     default void onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error) {
     }

     default void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
     }
 }
