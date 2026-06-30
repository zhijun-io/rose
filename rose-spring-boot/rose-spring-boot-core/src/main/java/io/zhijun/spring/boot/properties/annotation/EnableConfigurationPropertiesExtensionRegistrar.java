 package io.zhijun.spring.boot.properties.annotation;
 
 import java.util.Map;
 
 import io.zhijun.spring.boot.properties.ListenableConfigurationPropertiesBindHandlerAdvisor;
 import io.zhijun.spring.boot.properties.bind.EventPublishingConfigurationPropertiesBeanPropertyChangedListener;
 
 import org.springframework.beans.factory.support.BeanDefinitionBuilder;
 import org.springframework.beans.factory.support.BeanDefinitionRegistry;
 import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
 import org.springframework.core.type.AnnotationMetadata;
 
 /**
 * Registers beans for {@link EnableConfigurationPropertiesExtension}:
 * <ul>
 *     <li>{@link ListenableConfigurationPropertiesBindHandlerAdvisor}</li>
 *     <li>{@link EventPublishingConfigurationPropertiesBeanPropertyChangedListener} (if events enabled)</li>
 * </ul>
 */
 public class EnableConfigurationPropertiesExtensionRegistrar implements ImportBeanDefinitionRegistrar {
 
     private static final String ADVISOR_BEAN_NAME =
             "listenableConfigurationPropertiesBindHandlerAdvisor";
 
     private static final String EVENT_LISTENER_BEAN_NAME =
             "eventPublishingConfigurationPropertiesBeanPropertyChangedListener";
 
     @Override
     public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
         if (!registry.containsBeanDefinition(ADVISOR_BEAN_NAME)) {
             registry.registerBeanDefinition(ADVISOR_BEAN_NAME,
                     BeanDefinitionBuilder.genericBeanDefinition(
                             ListenableConfigurationPropertiesBindHandlerAdvisor.class)
                             .getBeanDefinition());
         }
         Map<String, Object> attributes = metadata.getAnnotationAttributes(
                 EnableConfigurationPropertiesExtension.class.getName());
         boolean publishEvents = attributes == null || (Boolean) attributes.get("publishEvents");
         if (publishEvents && !registry.containsBeanDefinition(EVENT_LISTENER_BEAN_NAME)) {
             registry.registerBeanDefinition(EVENT_LISTENER_BEAN_NAME,
                     BeanDefinitionBuilder.genericBeanDefinition(
                             EventPublishingConfigurationPropertiesBeanPropertyChangedListener.class)
                             .getBeanDefinition());
         }
     }
 }
