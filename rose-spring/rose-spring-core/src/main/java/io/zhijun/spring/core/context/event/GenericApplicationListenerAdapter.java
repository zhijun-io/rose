 package io.zhijun.spring.core.context.event;
 
 import org.jspecify.annotations.Nullable;
 import org.springframework.context.ApplicationEvent;
 import org.springframework.context.event.GenericApplicationListener;
 import org.springframework.context.event.SmartApplicationListener;
 
 import static org.springframework.core.ResolvableType.forClass;
 
 /**
  * 结合 {@link GenericApplicationListener} 和 {@link SmartApplicationListener} 的便捷接口。
  * <p>子类只需实现 {@link #onApplicationEvent(ApplicationEvent)} 和
  * {@link #supportsEventType(Class)} 即可。</p>
  */
 public interface GenericApplicationListenerAdapter extends GenericApplicationListener, SmartApplicationListener {
 
     @Override
     default boolean supportsSourceType(@Nullable Class<?> sourceType) {
         return true;
     }
 
     @Override
     default boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
         return supportsEventType(forClass(eventType));
     }
 
     @Override
     default int getOrder() {
         return LOWEST_PRECEDENCE;
     }
 
     @Override
     default String getListenerId() {
         return "";
     }
 }
