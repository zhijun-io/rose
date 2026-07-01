package io.zhijun.spring.webmvc.interceptor;

import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class LoggingMethodHandlerInterceptorTests {

    private final LoggingMethodHandlerInterceptor interceptor = new LoggingMethodHandlerInterceptor();

    @Test
    void preHandleReturnsTrue() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HandlerMethod handlerMethod = mock(HandlerMethod.class);
        assertTrue(interceptor.preHandle(request, response, handlerMethod));
    }

    @Test
    void supportsReturnsTrueWhenRequestNotNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HandlerMethod handlerMethod = mock(HandlerMethod.class);
        assertTrue(interceptor.supports(request, response, handlerMethod));
    }

    @Test
    void lifecycleMethodsDoNotThrow() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HandlerMethod handlerMethod = mock(HandlerMethod.class);
        ModelAndView mav = mock(ModelAndView.class);
        assertDoesNotThrow(() -> interceptor.postHandle(request, response, handlerMethod, mav));
        assertDoesNotThrow(() -> interceptor.afterCompletion(request, response, handlerMethod, new RuntimeException("test")));
    }
}
