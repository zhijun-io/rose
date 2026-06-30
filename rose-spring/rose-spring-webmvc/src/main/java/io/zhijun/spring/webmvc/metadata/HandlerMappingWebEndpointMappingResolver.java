package io.zhijun.spring.webmvc.metadata;

import io.zhijun.spring.web.metadata.WebEndpointMapping;
import io.zhijun.spring.web.metadata.WebEndpointMappingResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 从 {@link HandlerMapping HandlerMapping} Bean 解析 {@link WebEndpointMapping}。
 * <p>
 * 每种 {@link HandlerMapping} 子类型由独立策略处理，避免 {@code instanceof} 扩散。
 *
 * @since 1.0.0
 */
public class HandlerMappingWebEndpointMappingResolver implements WebEndpointMappingResolver {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<HandlerMappingStrategy> strategies;

    public HandlerMappingWebEndpointMappingResolver() {
        this.strategies = new ArrayList<>();
        this.strategies.add(new RequestMappingInfoHandlerMappingStrategy());
        this.strategies.add(new UrlHandlerMappingStrategy());
    }

    @Override
    public Collection<WebEndpointMapping> resolve(ApplicationContext context) {
        Map<String, HandlerMapping> handlerMappingBeans =
                context.getBeansOfType(HandlerMapping.class);

        List<WebEndpointMapping> result = new ArrayList<>();
        for (HandlerMapping handlerMapping : handlerMappingBeans.values()) {
            resolve(handlerMapping, result);
        }
        return result;
    }

    private void resolve(HandlerMapping handlerMapping, List<WebEndpointMapping> result) {
        for (HandlerMappingStrategy strategy : strategies) {
            if (strategy.supports(handlerMapping)) {
                try {
                    Collection<WebEndpointMapping> mappings = strategy.resolve(handlerMapping);
                    if (mappings != null) {
                        result.addAll(mappings);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to resolve WebEndpointMapping from {} using {}",
                            handlerMapping, strategy.getClass().getSimpleName(), e);
                }
            }
        }
    }

    // ---- Strategy interface & implementations ----

    interface HandlerMappingStrategy {

        boolean supports(HandlerMapping handlerMapping);

        Collection<WebEndpointMapping> resolve(HandlerMapping handlerMapping);
    }

    /**
     * 处理 {@link RequestMappingInfoHandlerMapping}：提取 {@link RequestMappingInfo} → 路径 + HTTP 方法。
     */
    static class RequestMappingInfoHandlerMappingStrategy implements HandlerMappingStrategy {

        @Override
        public boolean supports(HandlerMapping handlerMapping) {
            return handlerMapping instanceof RequestMappingInfoHandlerMapping;
        }

        @Override
        public Collection<WebEndpointMapping> resolve(HandlerMapping handlerMapping) {
            RequestMappingInfoHandlerMapping mapping =
                    (RequestMappingInfoHandlerMapping) handlerMapping;
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();

            List<WebEndpointMapping> result = new ArrayList<>(handlerMethods.size());
            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
                RequestMappingInfo info = entry.getKey();
                HandlerMethod handlerMethod = entry.getValue();

                WebEndpointMapping.Builder builder = WebEndpointMapping.builder()
                        .endpoint(handlerMethod)
                        .source(mapping)
                        .patterns(info.getPatternValues());

                if (!info.getMethodsCondition().getMethods().isEmpty()) {
                    info.getMethodsCondition().getMethods()
                            .forEach(m -> builder.method(m.name()));
                }

                if (!info.getParamsCondition().getExpressions().isEmpty()) {
                    info.getParamsCondition().getExpressions()
                            .forEach(e -> builder.param(e.toString()));
                }

                if (!info.getHeadersCondition().getExpressions().isEmpty()) {
                    info.getHeadersCondition().getExpressions()
                            .forEach(e -> builder.header(e.toString()));
                }

                if (!info.getConsumesCondition().getExpressions().isEmpty()) {
                    info.getConsumesCondition().getExpressions()
                            .forEach(e -> builder.consume(e.toString()));
                }

                if (!info.getProducesCondition().getExpressions().isEmpty()) {
                    info.getProducesCondition().getExpressions()
                            .forEach(e -> builder.produce(e.toString()));
                }

                result.add(builder.build());
            }
            return result;
        }
    }

    /**
     * 处理 {@link AbstractUrlHandlerMapping}：提取 URL 模式 → 处理器 Bean。
     */
    static class UrlHandlerMappingStrategy implements HandlerMappingStrategy {

        @Override
        public boolean supports(HandlerMapping handlerMapping) {
            return handlerMapping instanceof AbstractUrlHandlerMapping;
        }

        @Override
        public Collection<WebEndpointMapping> resolve(HandlerMapping handlerMapping) {
            AbstractUrlHandlerMapping urlHandlerMapping =
                    (AbstractUrlHandlerMapping) handlerMapping;
            Map<String, Object> handlerMap = obtainHandlerMap(urlHandlerMapping);
            if (handlerMap == null || handlerMap.isEmpty()) {
                return java.util.Collections.emptyList();
            }

            List<WebEndpointMapping> result = new ArrayList<>(handlerMap.size());
            for (Map.Entry<String, Object> entry : handlerMap.entrySet()) {
                result.add(WebEndpointMapping.builder()
                        .endpoint(entry.getValue())
                        .source(urlHandlerMapping)
                        .pattern(entry.getKey())
                        .build());
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        private static Map<String, Object> obtainHandlerMap(AbstractUrlHandlerMapping urlHandlerMapping) {
            try {
                java.lang.reflect.Method method = AbstractUrlHandlerMapping.class.getDeclaredMethod("getHandlerMap");
                ReflectionUtils.makeAccessible(method);
                return (Map<String, Object>) method.invoke(urlHandlerMapping);
            } catch (Exception e) {
                return java.util.Collections.emptyMap();
            }
        }
    }
}
