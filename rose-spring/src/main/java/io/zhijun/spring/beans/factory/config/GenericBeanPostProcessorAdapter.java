 package io.zhijun.spring.beans.factory.config;

 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.config.BeanPostProcessor;

 import static org.springframework.aop.framework.AopProxyUtils.ultimateTargetClass;
 import static org.springframework.core.GenericTypeResolver.resolveTypeArgument;

 /**
  * 泛型化的 {@link BeanPostProcessor} 适配器。
  * <p>子类通过泛型参数指定要处理的 Bean 类型，无需手动类型判断。</p>
  *
  * @param <T> 要处理的 Bean 类型
  */
 @SuppressWarnings("unchecked")
 public abstract class GenericBeanPostProcessorAdapter<T> implements BeanPostProcessor {

     private final Class<T> beanType;

     @SuppressWarnings("unchecked")
     public GenericBeanPostProcessorAdapter() {
         this.beanType = (Class<T>) resolveTypeArgument(getClass(), GenericBeanPostProcessorAdapter.class);
     }

     @Override
     public final Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
         Class<?> beanClass = ultimateTargetClass(bean);
         if (beanType.isAssignableFrom(beanClass)) {
             return doPostProcessBeforeInitialization((T) bean, beanName);
         }
         return bean;
     }

     @Override
     public final Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
         Class<?> beanClass = ultimateTargetClass(bean);
         if (beanType.isAssignableFrom(beanClass)) {
             return doPostProcessAfterInitialization((T) bean, beanName);
         }
         return bean;
     }

     public final Class<T> getBeanType() {
         return beanType;
     }

     /**
      * 前置处理，可返回修改后的 Bean 实例。
      */
     protected T doPostProcessBeforeInitialization(T bean, String beanName) throws BeansException {
         processBeforeInitialization(bean, beanName);
         return bean;
     }

     /**
      * 后置处理，可返回修改后的 Bean 实例。
      */
     protected T doPostProcessAfterInitialization(T bean, String beanName) throws BeansException {
         processAfterInitialization(bean, beanName);
         return bean;
     }

     /**
      * 前置回调，不返回 bean（由 doPostProcessBeforeInitialization 调用）。
      */
     protected void processBeforeInitialization(T bean, String beanName) throws BeansException {
     }

     /**
      * 后置回调，不返回 bean（由 doPostProcessAfterInitialization 调用）。
      */
     protected void processAfterInitialization(T bean, String beanName) throws BeansException {
     }
 }
