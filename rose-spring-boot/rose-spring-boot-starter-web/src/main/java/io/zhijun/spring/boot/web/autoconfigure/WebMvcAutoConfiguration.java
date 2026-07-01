 package io.zhijun.spring.boot.web.autoconfigure;

 import io.zhijun.spring.boot.condition.ConditionalOnWebMvcAvailable;
 import io.zhijun.spring.webmvc.ConfigurableContentNegotiationManagerWebMvcConfigurer;
import io.zhijun.spring.webmvc.servlet.ContentCachingFilter;
import io.zhijun.spring.webmvc.SingleViewResolverConfigurationListener;
import io.zhijun.spring.webmvc.annotation.EnableWebMvcExtension;
import io.zhijun.spring.webmvc.interceptor.LoggingMethodHandlerInterceptor;
import io.zhijun.spring.webmvc.interceptor.LoggingPageRenderContextHandlerInterceptor;
import io.zhijun.spring.webmvc.method.LoggingHandlerMethodArgumentResolverAdvice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Rose Spring Boot WebMVC 自动配置。
 * <p>
 * 在 Spring Boot {@link org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration}
 * 之后生效，提供：响应缓存 Filter、可配置的内容协商管理器、独占 ViewResolver 支持、
 * 以及 基于 {@link EnableWebMvcExtension} 的拦截器自动注册和反向代理 HandlerMapping。
 * <p>
 * 所有子功能均可通过配置属性关闭：
 * <ul>
 *   <li>{@code rose.spring.boot.webmvc.filter.enabled} — 响应缓存 Filter</li>
 *   <li>{@code rose.spring.boot.webmvc.content-negotiation.enabled} — 内容协商</li>
 *   <li>{@code rose.spring.webmvc.view-resolver.exclusive-bean-name} — 独占 ViewResolver</li>
 *   <li>{@code rose.spring.boot.webmvc.logging.enabled} — 日志记录</li>
 * </ul>
 *
 * @see org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
 * @see EnableWebMvcExtension
 * @since 1.0.0
 */
@ConditionalOnWebMvcAvailable
@AutoConfiguration(afterName = "org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration")
@EnableWebMvcExtension(
    registerHandlerInterceptors = true,
    reversedProxyHandlerMapping = true
)
@Import(WebMvcAutoConfiguration.LoggingConfiguration.class)
public class WebMvcAutoConfiguration {

    /**
     * 属性前缀：{@code rose.spring.boot.webmvc.}
     */
    static final String ROSE_SPRING_BOOT_WEBMVC_PROPERTY_NAME_PREFIX = "rose.spring.boot.webmvc.";

    /**
     * 响应缓存 Filter 启用属性名：{@code rose.spring.boot.webmvc.filter.enabled}
     */
    static final String ROSE_SPRING_BOOT_WEBMVC_FILTER_ENABLED_PROPERTY_NAME =
        ROSE_SPRING_BOOT_WEBMVC_PROPERTY_NAME_PREFIX + "filter.enabled";

    /**
     * 内容协商启用属性名：{@code rose.spring.boot.webmvc.content-negotiation.enabled}
     */
    static final String ROSE_SPRING_BOOT_WEBMVC_CONTENT_NEGOTIATION_ENABLED_PROPERTY_NAME =
        ROSE_SPRING_BOOT_WEBMVC_PROPERTY_NAME_PREFIX + "content-negotiation.enabled";

    /**
     * 日志记录启用属性名：{@code rose.spring.boot.webmvc.logging.enabled}
     */
    static final String ROSE_SPRING_BOOT_WEBMVC_LOGGING_ENABLED_PROPERTY_NAME =
        ROSE_SPRING_BOOT_WEBMVC_PROPERTY_NAME_PREFIX + "logging.enabled";

    @ConditionalOnProperty(name = ROSE_SPRING_BOOT_WEBMVC_FILTER_ENABLED_PROPERTY_NAME, matchIfMissing = true)
    @Bean
    public ContentCachingFilter contentCachingFilter() {
        return new ContentCachingFilter();
    }

    @ConditionalOnProperty(name = ROSE_SPRING_BOOT_WEBMVC_CONTENT_NEGOTIATION_ENABLED_PROPERTY_NAME, matchIfMissing = true)
    @Bean
    public ConfigurableContentNegotiationManagerWebMvcConfigurer contentNegotiationManagerWebMvcConfigurer() {
        return new ConfigurableContentNegotiationManagerWebMvcConfigurer();
    }

    @ConditionalOnProperty(name = SingleViewResolverConfigurationListener.EXCLUSIVE_VIEW_RESOLVER_BEAN_NAME_PROPERTY_NAME)
    @Bean
    public SingleViewResolverConfigurationListener exclusiveViewResolverApplicationListener() {
        return new SingleViewResolverConfigurationListener();
    }

    @ConditionalOnProperty(name = ROSE_SPRING_BOOT_WEBMVC_LOGGING_ENABLED_PROPERTY_NAME, matchIfMissing = true)
    @Import({
        LoggingMethodHandlerInterceptor.class,
        LoggingPageRenderContextHandlerInterceptor.class,
        LoggingHandlerMethodArgumentResolverAdvice.class
    })
    static class LoggingConfiguration {
    }
}
