 package io.zhijun.spring.boot.properties;
 
import java.util.stream.Collectors;
import java.util.List;

import io.zhijun.spring.boot.properties.bind.BindListener;
 import io.zhijun.spring.boot.properties.bind.ListenableBindHandlerAdapter;
 
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.SmartInitializingSingleton;
 import org.springframework.boot.context.properties.ConfigurationPropertiesBindHandlerAdvisor;
 import org.springframework.boot.context.properties.bind.BindHandler;
 import org.springframework.core.annotation.AnnotationAwareOrderComparator;
 
 /**
 * A {@link ConfigurationPropertiesBindHandlerAdvisor} that automatically chains all
 * {@link BindListener} beans discovered in the {@link BeanFactory}.
 * <p>
 * Registered as a Spring Boot {@link ConfigurationPropertiesBindHandlerAdvisor} so it is picked
 * up by Boot's {@code ConfigurationPropertiesBindingPostProcessor} during
 * {@link org.springframework.boot.context.properties.EnableConfigurationProperties @EnableConfigurationProperties}
 * binding.
 * <p>
 * Inspired by {@code io.microsphere.spring.boot.context.properties.ListenableConfigurationPropertiesBindHandlerAdvisor}.
 */
 public class ListenableConfigurationPropertiesBindHandlerAdvisor
         implements ConfigurationPropertiesBindHandlerAdvisor, BeanFactoryAware, SmartInitializingSingleton {
 
     private BeanFactory beanFactory;
 
     private List<BindListener> listeners;
 
     @Override
     public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
         this.beanFactory = beanFactory;
     }
 
    @Override
    public void afterSingletonsInstantiated() {
        // Collect all BindListener beans, ordered
        List<BindListener> found = beanFactory.getBeanProvider(BindListener.class)
                .orderedStream()
                .collect(Collectors.toList());
        AnnotationAwareOrderComparator.sort(found);
        this.listeners = found;
    }
 
     @Override
     public BindHandler apply(BindHandler handler) {
         if (listeners == null || listeners.isEmpty()) {
             return handler;
         }
         return new ListenableBindHandlerAdapter(handler, listeners);
     }
 }
