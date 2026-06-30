package io.zhijun.spring.webmvc.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Store {@link HandlerMethod} return value {@link ResponseBodyAdvice}
 *
 * @since 1.0.0
 */
@RestControllerAdvice
public final class StoringResponseBodyReturnValueAdvice implements ResponseBodyAdvice<Object> {

    private static final Logger logger = LoggerFactory.getLogger(StoringResponseBodyReturnValueAdvice.class);

    private static final String ATTRIBUTE_NAME_PREFIX = "HM.RV:";

    private static final Set<Class<? extends HttpMessageConverter<?>>> SUPPORTED_CONVERTER_TYPES =
            new HashSet<Class<? extends HttpMessageConverter<?>>>(Arrays.asList(
                    MappingJackson2HttpMessageConverter.class,
                    StringHttpMessageConverter.class
            ));

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return SUPPORTED_CONVERTER_TYPES.contains(converterType);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        Method method = returnType.getMethod();
        if (method != null && body != null && request instanceof ServletServerHttpRequest) {
            HttpServletRequest httpServletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            RequestAttributes requestAttributes = new org.springframework.web.context.request.ServletRequestAttributes(httpServletRequest);
            String attributeName = ATTRIBUTE_NAME_PREFIX + method;
            requestAttributes.setAttribute(attributeName, body, RequestAttributes.SCOPE_REQUEST);
        }
        return body;
    }
}
