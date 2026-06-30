package io.zhijun.spring.boot.web.autoconfigure;

import io.zhijun.spring.boot.condition.ConditionalOnWebAvailable;
import io.zhijun.spring.web.metadata.DefaultWebEndpointMappingRegistry;
import io.zhijun.spring.web.metadata.WebEndpointMappingRegistrar;
import io.zhijun.spring.web.metadata.WebEndpointMappingRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Rose Spring Boot Web 自动配置基础
 */
@ConditionalOnWebAvailable
@AutoConfiguration
public class WebAutoConfiguration {

    @Bean
    public WebEndpointMappingRegistry webEndpointMappingRegistry() {
        return new DefaultWebEndpointMappingRegistry();
    }

    @Bean
    public WebEndpointMappingRegistrar webEndpointMappingRegistrar(WebEndpointMappingRegistry registry) {
        return new WebEndpointMappingRegistrar(registry);
    }
}
