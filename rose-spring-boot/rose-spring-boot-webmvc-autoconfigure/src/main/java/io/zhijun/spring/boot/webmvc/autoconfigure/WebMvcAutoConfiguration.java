package io.zhijun.spring.boot.webmvc.autoconfigure;

import io.zhijun.spring.webmvc.metadata.HandlerMappingWebEndpointMappingResolver;
import io.zhijun.spring.boot.webmvc.autoconfigure.condition.ConditionalOnWebMvcAvailable;
import io.zhijun.spring.webmvc.ContentCachingFilter;
import io.zhijun.spring.webmvc.annotation.EnableWebMvcExtension;
import io.zhijun.spring.webmvc.ConfigurableContentNegotiationManagerWebMvcConfigurer;
import io.zhijun.spring.webmvc.ExclusiveViewResolverApplicationListener;
import io.zhijun.spring.webmvc.interceptor.LoggingMethodHandlerInterceptor;
import io.zhijun.spring.webmvc.interceptor.LoggingPageRenderContextHandlerInterceptor;
import io.zhijun.spring.webmvc.method.LoggingHandlerMethodArgumentResolverAdvice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static io.zhijun.spring.boot.webmvc.constants.PropertyConstants.ROSE_SPRING_BOOT_WEBMVC_CONTENT_NEGOTIATION_ENABLED_PROPERTY_NAME;
import static io.zhijun.spring.boot.webmvc.constants.PropertyConstants.ROSE_SPRING_BOOT_WEBMVC_FILTER_ENABLED_PROPERTY_NAME;
import static io.zhijun.spring.boot.webmvc.constants.PropertyConstants.ROSE_SPRING_BOOT_WEBMVC_LOGGING_ENABLED_PROPERTY_NAME;
import static io.zhijun.spring.boot.webmvc.constants.PropertyConstants.ROSE_SPRING_BOOT_WEBMVC_VIEW_RESOLVER_ENABLED_PROPERTY_NAME;

/**
 * Rose Spring Boot WebMVC 自动配置
 */
@EnableWebMvcExtension(
        registerHandlerInterceptors = true,
        reversedProxyHandlerMapping = true
)
@ConditionalOnWebMvcAvailable
@AutoConfiguration(afterName = {
        "org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration"
})
public class WebMvcAutoConfiguration {

    @Bean
    public HandlerMappingWebEndpointMappingResolver handlerMappingWebEndpointMappingResolver() {
        return new HandlerMappingWebEndpointMappingResolver();
    }

    @ConditionalOnProperty(name = ROSE_SPRING_BOOT_WEBMVC_FILTER_ENABLED_PROPERTY_NAME, matchIfMissing = true)
    @Bean
    public ContentCachingFilter contentCachingFilter() {
        return new ContentCachingFilter();
    }
    @ConditionalOnProperty(name = ROSE_SPRING_BOOT_WEBMVC_CONTENT_NEGOTIATION_ENABLED_PROPERTY_NAME, matchIfMissing = true)
    @Bean
    public WebMvcConfigurer contentNegotiationManagerWebMvcConfigurer() {
        return new ConfigurableContentNegotiationManagerWebMvcConfigurer();
    }

    @ConditionalOnProperty(name = ROSE_SPRING_BOOT_WEBMVC_VIEW_RESOLVER_ENABLED_PROPERTY_NAME, matchIfMissing = true)
    @Bean
    public ExclusiveViewResolverApplicationListener exclusiveViewResolverApplicationListener() {
        return new ExclusiveViewResolverApplicationListener();
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = ROSE_SPRING_BOOT_WEBMVC_LOGGING_ENABLED_PROPERTY_NAME, matchIfMissing = true)
    static class LoggingConfiguration {

        @Bean
        public LoggingMethodHandlerInterceptor loggingMethodHandlerInterceptor() {
            return new LoggingMethodHandlerInterceptor();
        }

        @Bean
        public LoggingPageRenderContextHandlerInterceptor loggingPageRenderContextHandlerInterceptor() {
            return new LoggingPageRenderContextHandlerInterceptor();
        }

        @Bean
        public LoggingHandlerMethodArgumentResolverAdvice loggingHandlerMethodArgumentResolverAdvice() {
            return new LoggingHandlerMethodArgumentResolverAdvice();
        }
    }
}
