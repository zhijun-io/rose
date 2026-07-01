package io.zhijun.spring.webmvc.advice;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * {@link ResponseBodyAdvice} 空实现适配器，子类只需重写需要的方法。
 *
 * @param <T> 响应体类型
 */
public abstract class ResponseBodyAdviceAdapter<T> implements ResponseBodyAdvice<T> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return false;
    }

    @Override
    public T beforeBodyWrite(T body, MethodParameter returnType, MediaType selectedContentType,
                             Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
                             ServerHttpResponse response) {
        return body;
    }
}
