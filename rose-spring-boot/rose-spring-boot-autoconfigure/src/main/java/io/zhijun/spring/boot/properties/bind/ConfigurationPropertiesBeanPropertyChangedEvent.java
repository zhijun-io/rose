 package io.zhijun.spring.boot.properties.bind;
 
 import io.zhijun.spring.context.event.BeanPropertyChangedEvent;
 
 import org.springframework.boot.context.properties.source.ConfigurationProperty;
 import org.springframework.core.ResolvableType;
 
 /**
 * Event raised when a property of a bean annotated with
 * {@link org.springframework.boot.context.properties.ConfigurationProperties @ConfigurationProperties}
 * was changed during re-binding.
 * <p>
 * Inspired by {@code io.microsphere.spring.boot.context.properties.bind.ConfigurationPropertiesBeanPropertyChangedEvent}.
 */
 public class ConfigurationPropertiesBeanPropertyChangedEvent extends BeanPropertyChangedEvent {
 
     private final ResolvableType propertyType;
 
     private final ConfigurationProperty configurationProperty;
 
     public ConfigurationPropertiesBeanPropertyChangedEvent(
             Object bean, String propertyName, ResolvableType propertyType,
             Object oldValue, Object newValue, ConfigurationProperty configurationProperty) {
         super(bean, propertyName, oldValue, newValue);
         this.propertyType = propertyType;
         this.configurationProperty = configurationProperty;
     }
 
     public ResolvableType getPropertyType() {
         return propertyType;
     }
 
     public ConfigurationProperty getConfigurationProperty() {
         return configurationProperty;
     }
 
     @Override
     public String toString() {
         return getClass().getSimpleName() + "{" +
                 "bean=" + getBean() +
                 ", propertyName='" + getPropertyName() + '\'' +
                 ", propertyType=" + propertyType +
                 ", configurationProperty=" + configurationProperty +
                 ", oldValue=" + getOldValue() +
                 ", newValue=" + getNewValue() +
                 '}';
     }
 }
