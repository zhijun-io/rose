package io.zhijun.multitenancy.spring.web.annotation;

import org.springframework.lang.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import io.zhijun.annotation.Incubating;
import io.zhijun.multitenancy.core.context.TenantContext;

/**
 * Allows resolving the current multitenancy identifier using the {@link TenantId}
 * annotation.
 * <p>
 * Example:
 *
 * <pre>
 * &#64;RestController
 * class MyRestController {
 *     &#64;GetMapping("/multitenancy")
 *     String getCurrentTenant(@TenantIdentifier String tenantIdentifier) {
 *         return tenantIdentifier;
 *     }
 * }
 * </pre>
 */
@Incubating
public final class TenantIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(TenantId.class) != null
                && parameter.getParameterType().getTypeName().equals(String.class.getTypeName());
    }

    @Nullable
    @Override
    public Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) {
        return TenantContext.getTenantId();
    }

}
