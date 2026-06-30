 package io.zhijun.spring.context.event;
 
 import org.springframework.context.ApplicationEvent;
 
 /**
 * Base event raised when a bound property on a bean has changed.
 * <p>
 * Subclasses may carry additional context such as the configuration property source, property type, etc.
 * <p>
 * Inspired by {@code io.microsphere.spring.context.event.BeanPropertyChangedEvent}.
 */
 public class BeanPropertyChangedEvent extends ApplicationEvent {
 
     private final transient Object bean;
 
     private final String propertyName;
 
     private final transient Object oldValue;
 
     private final transient Object newValue;
 
     public BeanPropertyChangedEvent(Object bean, String propertyName, Object oldValue, Object newValue) {
         super(bean);
         this.bean = bean;
         this.propertyName = propertyName;
         this.oldValue = oldValue;
         this.newValue = newValue;
     }
 
     public Object getBean() {
         return bean;
     }
 
     public String getPropertyName() {
         return propertyName;
     }
 
     public Object getOldValue() {
         return oldValue;
     }
 
     public Object getNewValue() {
         return newValue;
     }
 
     @Override
     public String toString() {
         return getClass().getSimpleName() + "{" +
                 "bean=" + bean +
                 ", propertyName='" + propertyName + '\'' +
                 ", oldValue=" + oldValue +
                 ", newValue=" + newValue +
                 '}';
     }
 }
