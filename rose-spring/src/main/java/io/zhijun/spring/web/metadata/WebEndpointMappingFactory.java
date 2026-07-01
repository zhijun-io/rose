package io.zhijun.spring.web.metadata;

import java.util.Optional;

/**
 * {@link WebEndpointMapping} 工厂接口。
 *
  * @param <E> 端点类型
  */
 @FunctionalInterface
 public interface WebEndpointMappingFactory<E> {
 
     default boolean supports(E endpoint) {
         return true;
     }
 
     Optional<WebEndpointMapping> create(E endpoint);
 
     @SuppressWarnings("unchecked")
     default Class<E> getSourceType() {
         return (Class<E>) org.springframework.core.ResolvableType.forClass(getClass())
                 .as(WebEndpointMappingFactory.class).resolveGeneric(0);
     }
 }
