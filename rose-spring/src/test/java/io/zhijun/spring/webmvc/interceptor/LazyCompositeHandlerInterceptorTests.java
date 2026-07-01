package io.zhijun.spring.webmvc.interceptor;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class LazyCompositeHandlerInterceptorTests {

    @Test
    void preHandleReturnsTrueWhenNoInterceptorsResolved() throws Exception {
        LazyCompositeHandlerInterceptor interceptor = new LazyCompositeHandlerInterceptor();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void postHandleDoesNotThrowWithNoInterceptors() {
        LazyCompositeHandlerInterceptor interceptor = new LazyCompositeHandlerInterceptor();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        assertDoesNotThrow(() ->
                interceptor.postHandle(request, response, new Object(), null));
    }

    @Test
    void afterCompletionDoesNotThrowWithNoInterceptors() {
        LazyCompositeHandlerInterceptor interceptor = new LazyCompositeHandlerInterceptor();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        assertDoesNotThrow(() ->
                interceptor.afterCompletion(request, response, new Object(), null));
    }

    @Test
    void beanNameConstant() {
        assertEquals("lazyCompositeHandlerInterceptor", LazyCompositeHandlerInterceptor.BEAN_NAME);
    }
}
