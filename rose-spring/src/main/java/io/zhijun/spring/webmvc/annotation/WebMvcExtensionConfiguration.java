package io.zhijun.spring.webmvc.annotation;

import io.zhijun.spring.webmvc.interceptor.LazyCompositeHandlerInterceptor;
import io.zhijun.spring.webmvc.method.InterceptingHandlerMethodProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The configuration class for {@link EnableWebMvcExtension}
 *
 * @see WebMvcConfigurer
 * @see EnableWebMvcExtension
 * @since 1.0.0
 */
public class WebMvcExtensionConfiguration implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebMvcExtensionConfiguration.class);

    private final ObjectProvider<LazyCompositeHandlerInterceptor[]> lazyCompositeHandlerInterceptorProvider;
    private final ObjectProvider<InterceptingHandlerMethodProcessor> interceptingHandlerMethodProcessorProvider;

    public WebMvcExtensionConfiguration(ObjectProvider<LazyCompositeHandlerInterceptor[]> lazyCompositeHandlerInterceptorProvider,
                                        ObjectProvider<InterceptingHandlerMethodProcessor> interceptingHandlerMethodProcessorProvider) {
        this.lazyCompositeHandlerInterceptorProvider = lazyCompositeHandlerInterceptorProvider;
        this.interceptingHandlerMethodProcessorProvider = interceptingHandlerMethodProcessorProvider;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Register InterceptingHandlerMethodProcessor as interceptor (if present)
        InterceptingHandlerMethodProcessor processor = interceptingHandlerMethodProcessorProvider.getIfAvailable();
        if (processor != null) {
            registry.addInterceptor(processor);
        }

        LazyCompositeHandlerInterceptor[] lazyCompositeHandlerInterceptors = lazyCompositeHandlerInterceptorProvider.getIfAvailable();
        int length = lazyCompositeHandlerInterceptors != null ? lazyCompositeHandlerInterceptors.length : 0;
        if (length == 0) {
            if (logger.isTraceEnabled()) {
                logger.trace("No LazyCompositeHandlerInterceptor Bean was registered.");
            }
            return;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("{} LazyCompositeHandlerInterceptor Beans will be added into InterceptorRegistry.", length);
        }
        for (int i = 0; i < length; i++) {
            LazyCompositeHandlerInterceptor lazyCompositeHandlerInterceptor = lazyCompositeHandlerInterceptors[i];
            registry.addInterceptor(lazyCompositeHandlerInterceptor);
        }
    }
}
