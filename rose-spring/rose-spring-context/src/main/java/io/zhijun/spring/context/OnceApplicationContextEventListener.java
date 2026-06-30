 package io.zhijun.spring.context;
 
 import org.jspecify.annotations.Nullable;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.context.ApplicationEvent;
 import org.springframework.context.ApplicationListener;
 import org.springframework.context.event.ApplicationContextEvent;
 
 /**
  * 一次性 {@link ApplicationListener} 基类，只处理来自原始 ApplicationContext 的事件。
  * <p>防止在父子容器层次中重复处理同一事件。</p>
  *
  * @param <E> 监听的事件类型
  */
 public abstract class OnceApplicationContextEventListener<E extends ApplicationContextEvent>
         implements ApplicationListener<E>, ApplicationContextAware {
 
     protected final Logger logger = LoggerFactory.getLogger(getClass());
 
     private @Nullable ApplicationContext applicationContext;
 
     @Override
     public final void onApplicationEvent(E event) {
         if (isOriginalEventSource(event)) {
             onApplicationContextEvent(event);
         }
     }
 
     protected abstract void onApplicationContextEvent(E event);
 
     private boolean isOriginalEventSource(ApplicationEvent event) {
         boolean original = applicationContext != null && applicationContext.equals(event.getSource());
         if (!original && logger.isTraceEnabled()) {
             logger.trace("事件源 [{}] 不是原始 ApplicationContext，跳过", event.getSource());
         }
         return original;
     }
 
     @Override
     public final void setApplicationContext(ApplicationContext applicationContext) {
         this.applicationContext = applicationContext;
     }
 
     protected final ApplicationContext getApplicationContext() {
         if (applicationContext == null) {
             throw new IllegalStateException("ApplicationContext 未注入，请确保该类是一个 Spring Bean");
         }
         return applicationContext;
     }
 }
