package io.zhijun.spring.webmvc.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Store the {@link HandlerMethod} {@link RequestBody} parameter {@link RequestBodyAdvice} implementation
 *
 * @since 1.0.0
 */
@RestControllerAdvice
public final class StoringRequestBodyArgumentAdvice implements RequestBodyAdvice {

    private static final Logger logger = LoggerFactory.getLogger(StoringRequestBodyArgumentAdvice.class);

    private static final String ATTRIBUTE_NAME_PREFIX = "HM.RB.ARG:";

    private static final Set<Class<? extends HttpMessageConverter<?>>> SUPPORTED_CONVERTER_TYPES =
            new HashSet<Class<? extends HttpMessageConverter<?>>>(Arrays.asList(
                    MappingJackson2HttpMessageConverter.class,
                    StringHttpMessageConverter.class
            ));

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return SUPPORTED_CONVERTER_TYPES.contains(converterType);
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
                                           Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        return inputMessage;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        Method method = parameter.getMethod();
        if (method != null) {
            setHandlerMethodRequestBodyArgument(method, body);
        }
        return body;
    }

    @Override
    public Object handleEmptyBody(@Nullable Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                  Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    private static void setHandlerMethodRequestBodyArgument(Method method, Object requestBodyArgument) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null && requestBodyArgument != null) {
            String attributeName = ATTRIBUTE_NAME_PREFIX + method;
            requestAttributes.setAttribute(attributeName, requestBodyArgument, RequestAttributes.SCOPE_REQUEST);
        }
    }
}
