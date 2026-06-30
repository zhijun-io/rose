 package io.zhijun.spring.boot.properties.bind;
 
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
import io.zhijun.spring.boot.properties.bind.ConfigurationPropertiesBeanPropertyChangedEvent;

 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.SmartInitializingSingleton;
 import org.springframework.boot.context.properties.ConfigurationProperties;
 import org.springframework.boot.context.properties.bind.BindContext;
 import org.springframework.boot.context.properties.bind.Bindable;
 import org.springframework.boot.context.properties.source.ConfigurationProperty;
 import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.context.ConfigurableApplicationContext;
 import org.springframework.core.annotation.AnnotatedElementUtils;
 
 /**
 * A {@link BindListener} that publishes {@link ConfigurationPropertiesBeanPropertyChangedEvent}
 * when a bound property changes on a {@link ConfigurationProperties @ConfigurationProperties} bean.
 * <p>
 * During initial binding it tracks the bound values; on subsequent bindings (re-binding) it detects
 * differences and publishes events via the {@link ApplicationContext}.
 * <p>
 * Inspired by {@code io.microsphere.spring.boot.context.properties.bind.EventPublishingConfigurationPropertiesBeanPropertyChangedListener}.
 */
 public class EventPublishingConfigurationPropertiesBeanPropertyChangedListener
         implements BindListener, ApplicationContextAware, InitializingBean, SmartInitializingSingleton {
 
     private static final Logger logger = LoggerFactory.getLogger(
             EventPublishingConfigurationPropertiesBeanPropertyChangedListener.class);
 
     private ConfigurableApplicationContext applicationContext;
 
     private boolean initialBindingComplete;
 
     public EventPublishingConfigurationPropertiesBeanPropertyChangedListener() {
     }
 
     @Override
     public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
         this.applicationContext = (ConfigurableApplicationContext) applicationContext;
     }
 
     @Override
     public void afterPropertiesSet() {
     }
 
     @Override
     public void afterSingletonsInstantiated() {
         this.initialBindingComplete = true;
         if (logger.isDebugEnabled()) {
             logger.debug("Initial binding complete, property change detection now active");
         }
     }
 
     @Override
     public Object onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
         if (!initialBindingComplete) {
             return result;
         }
         ConfigurationProperty property = context.getConfigurationProperty();
         if (property == null) {
             return result;
         }
         Object bean = resolveBean(target);
         if (bean == null) {
             return result;
         }
         if (!isConfigurationPropertiesBean(bean)) {
             return result;
         }
         String propertyName = property.getName().toString();
         Object oldValue = context.getConfigurationProperty().getValue();
         if (oldValue != null ? oldValue.equals(result) : result == null) {
             return result;
         }
         ConfigurationPropertiesBeanPropertyChangedEvent event =
                 new ConfigurationPropertiesBeanPropertyChangedEvent(
                         bean, propertyName, target.getType(), oldValue, result, property);
         applicationContext.publishEvent(event);
         if (logger.isTraceEnabled()) {
             logger.trace("Published ConfigurationPropertiesBeanPropertyChangedEvent [{}]", event);
         }
         return result;
     }
 
     private static boolean isConfigurationPropertiesBean(Object bean) {
         return AnnotatedElementUtils.hasAnnotation(bean.getClass(), ConfigurationProperties.class);
     }
 
     private static Object resolveBean(Bindable<?> target) {
         java.util.function.Supplier<?> value = target.getValue();
         return value != null ? value.get() : null;
     }
 }
