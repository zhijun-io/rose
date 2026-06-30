package io.zhijun.spring.web.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;

import java.util.Collection;
import java.util.List;

/**
 * 收集所有 {@link WebEndpointMappingResolver} 的解析结果，注册到 {@link WebEndpointMappingRegistry}。
 * <p>
 * 在 {@link SmartInitializingSingleton} 阶段执行，确保所有单例 Bean 已就绪。
 *
 * @since 1.0.0
 */
public class WebEndpointMappingRegistrar extends ApplicationObjectSupport implements SmartInitializingSingleton {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final WebEndpointMappingRegistry registry;

    public WebEndpointMappingRegistrar(WebEndpointMappingRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void afterSingletonsInstantiated() {
        ApplicationContext context = obtainApplicationContext();
        Collection<WebEndpointMapping> mappings = resolveMappings(context);
        int count = 0;
        for (WebEndpointMapping mapping : mappings) {
            if (registry.register(mapping)) {
                count++;
            }
        }
        if (logger.isInfoEnabled()) {
            logger.info("{} WebEndpointMapping(s) registered from context [{}]", count, context.getId());
        }
    }

    private Collection<WebEndpointMapping> resolveMappings(ApplicationContext context) {
        List<WebEndpointMappingResolver> resolvers = lookupResolvers(context);
        Collection<WebEndpointMapping> result = new java.util.ArrayList<>();
        for (WebEndpointMappingResolver resolver : resolvers) {
            Collection<WebEndpointMapping> resolved = resolver.resolve(context);
            if (resolved != null && !resolved.isEmpty()) {
                result.addAll(resolved);
            }
        }
        return result;
    }

    private List<WebEndpointMappingResolver> lookupResolvers(ApplicationContext context) {
        return new java.util.ArrayList<>(context.getBeansOfType(WebEndpointMappingResolver.class).values());
    }
}
