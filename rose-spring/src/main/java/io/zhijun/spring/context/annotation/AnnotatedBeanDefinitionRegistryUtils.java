package io.zhijun.spring.context.annotation;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;

public abstract class AnnotatedBeanDefinitionRegistryUtils {

    public static BeanNameGenerator resolveAnnotatedBeanNameGenerator(BeanDefinitionRegistry registry) {
        if (registry instanceof BeanDefinitionRegistry) {
            try {
                java.lang.reflect.Field field = ConfigurationClassPostProcessor.class.getDeclaredField("beanNameGenerator");
                field.setAccessible(true);
                // Try to get from the post processor if available as a bean
            } catch (Exception e) {
                // fall through
            }
        }
        return new AnnotationBeanNameGenerator();
    }
}
