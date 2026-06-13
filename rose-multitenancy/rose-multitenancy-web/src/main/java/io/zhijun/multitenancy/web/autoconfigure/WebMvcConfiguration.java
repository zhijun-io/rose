package io.zhijun.multitenancy.web.autoconfigure;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.zhijun.multitenancy.web.context.annotations.TenantIdentifierArgumentResolver;

/**
 * Register Rose Spring Web MVC configuration.
 */
@Configuration(proxyBeanMethods = false)
public final class WebMvcConfiguration implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new TenantIdentifierArgumentResolver());
    }

}
