package io.zhijun.spring.web.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

 import java.util.Collection;
 import java.util.Map;
 
import io.zhijun.spring.web.HandlerMethodAdvice;
import io.zhijun.spring.web.metadata.WebEndpointMapping;
import io.zhijun.spring.web.metadata.WebEndpointMappingRegistry;
 
 /**
  * Web 事件发布器，发布 HandlerMethodArgumentsResolvedEvent 和 WebEndpointMappingsReadyEvent。
  */
public class WebEventPublisher implements SmartLifecycle, HandlerMethodAdvice {
 
     private static final Logger logger = LoggerFactory.getLogger(WebEventPublisher.class);
 
     public static final int DEFAULT_PHASE = Integer.MIN_VALUE + 100;
 
     private final ApplicationContext context;
 
     private volatile boolean running;
 
     public WebEventPublisher(ApplicationContext context) {
         this.context = context;
     }
 
     @Override
    public void beforeExecuteMethod(HandlerMethod handlerMethod, Object[] args, NativeWebRequest request) {
         context.publishEvent(new HandlerMethodArgumentsResolvedEvent(request, handlerMethod, args));
     }
 
     @Override
     public void start() {
         Map<String, WebEndpointMappingRegistry> registryMap =
                 context.getBeansOfType(WebEndpointMappingRegistry.class);
         if (registryMap.isEmpty()) {
             logger.warn("No WebEndpointMappingRegistry Bean was registered");
             this.running = true;
             return;
         }
         WebEndpointMappingRegistry registry = registryMap.values().iterator().next();
         Collection<WebEndpointMapping> webEndpointMappings = registry.getAll();
         context.publishEvent(new WebEndpointMappingsReadyEvent(context, webEndpointMappings));
         this.running = true;
     }
 
     @Override
     public void stop() {
         this.running = false;
     }
 
     @Override
     public boolean isRunning() {
         return this.running;
     }
 
     @Override
     public int getPhase() {
         return DEFAULT_PHASE;
     }
 
     @Override
     public boolean isAutoStartup() {
         return true;
     }
 
     @Override
     public void stop(Runnable callback) {
         this.running = false;
         callback.run();
     }
 }
