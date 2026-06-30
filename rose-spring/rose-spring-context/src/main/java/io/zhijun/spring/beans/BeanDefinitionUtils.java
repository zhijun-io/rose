 package io.zhijun.spring.beans;
 
 import org.jspecify.annotations.Nullable;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.beans.factory.support.AbstractBeanDefinition;
 import org.springframework.beans.factory.support.BeanDefinitionBuilder;
 import org.springframework.core.ResolvableType;
 
 /**
  * {@link BeanDefinition} 工具类。
  */
 public abstract class BeanDefinitionUtils {
 
     private static final Logger logger = LoggerFactory.getLogger(BeanDefinitionUtils.class);
 
     private BeanDefinitionUtils() {
     }
 
     /**
      * 创建指定类型的 bean 定义。
      */
     public static AbstractBeanDefinition genericBeanDefinition(Class<?> beanType) {
         return genericBeanDefinition(beanType, BeanDefinition.ROLE_APPLICATION);
     }
 
     /**
      * 创建指定类型和角色的 bean 定义。
      */
     public static AbstractBeanDefinition genericBeanDefinition(Class<?> beanType, int role) {
         BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(beanType);
         builder.setRole(role);
         AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
         if (logger.isTraceEnabled()) {
             logger.trace("Built BeanDefinition [{}] for type [{}]", beanDefinition, beanType);
         }
         return beanDefinition;
     }
 
     /**
      * 从 {@link BeanDefinition} 解析 bean 类型。
      * <p>优先通过 {@link ResolvableType} 解析，失败时回退到 bean class name。</p>
      */
     @Nullable
     public static Class<?> resolveBeanType(BeanDefinition beanDefinition) {
         return resolveBeanType(beanDefinition, Thread.currentThread().getContextClassLoader());
     }
 
     /**
      * 从 {@link BeanDefinition} 解析 bean 类型，使用指定的类加载器。
      */
     @Nullable
     public static Class<?> resolveBeanType(BeanDefinition beanDefinition, @Nullable ClassLoader classLoader) {
         ResolvableType resolvableType = beanDefinition.getResolvableType();
         Class<?> beanClass = resolvableType.resolve();
         if (beanClass == null) {
             String beanClassName = beanDefinition.getBeanClassName();
             if (beanClassName != null && classLoader != null) {
                 try {
                     beanClass = Class.forName(beanClassName, false, classLoader);
                 } catch (ClassNotFoundException e) {
                     logger.debug("Failed to resolve bean class [{}]", beanClassName, e);
                 }
             }
         }
         return beanClass;
     }
 }
