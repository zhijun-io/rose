 package io.zhijun.spring.web.metadata;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Optional;
 
 /**
  * {@link WebEndpointMappingFactory} 抽象实现。
  *
  * @param <E> 端点类型
  */
 public abstract class AbstractWebEndpointMappingFactory<E> implements WebEndpointMappingFactory<E> {
 
     protected final Logger logger = LoggerFactory.getLogger(getClass());
 
     @Override
     public final Optional<WebEndpointMapping> create(E endpoint) {
         try {
             WebEndpointMapping mapping = doCreate(endpoint);
             return Optional.ofNullable(mapping);
         } catch (Throwable e) {
             logger.error("WebEndpointMapping 无法从来源 {} 创建", endpoint, e);
             return Optional.empty();
         }
     }
 
     protected abstract WebEndpointMapping doCreate(E endpoint) throws Throwable;
 }
