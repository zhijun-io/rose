package io.zhijun.multitenancy.boot.autoconfigure.web;

import io.zhijun.multitenancy.spring.web.annotation.TenantIdArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Register Rose Spring Web MVC configuration.
 */
@Configuration(proxyBeanMethods = false)
public final class WebMvcConfiguration implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new TenantIdArgumentResolver());
    }
}
