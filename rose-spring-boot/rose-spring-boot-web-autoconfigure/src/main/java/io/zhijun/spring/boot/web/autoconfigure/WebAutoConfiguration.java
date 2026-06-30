package io.zhijun.spring.boot.web.autoconfigure;

import io.zhijun.spring.web.metadata.DefaultWebEndpointMappingRegistry;
import io.zhijun.spring.web.metadata.WebEndpointMappingRegistrar;
import io.zhijun.spring.web.metadata.WebEndpointMappingRegistry;
import io.zhijun.spring.boot.web.autoconfigure.condition.ConditionalOnWebAvailable;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
