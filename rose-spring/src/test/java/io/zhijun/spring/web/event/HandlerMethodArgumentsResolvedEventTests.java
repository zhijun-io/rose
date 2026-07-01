package io.zhijun.spring.web.event;

import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class HandlerMethodArgumentsResolvedEventTests {

    @Test
    void constructorSetsAllFields() {
        WebRequest webRequest = mock(WebRequest.class);
        HandlerMethod handlerMethod = mock(HandlerMethod.class);
        Object[] args = {"arg1", 2};

        HandlerMethodArgumentsResolvedEvent event =
                new HandlerMethodArgumentsResolvedEvent(webRequest, handlerMethod, args);

        assertSame(webRequest, event.getSource());
        assertSame(webRequest, event.getWebRequest());
        assertSame(handlerMethod, event.getHandlerMethod());
        assertArrayEquals(args, event.getArguments());
    }

    @Test
    void constructorRejectsNullHandlerMethod() {
        WebRequest webRequest = mock(WebRequest.class);
        assertThrows(NullPointerException.class,
                () -> new HandlerMethodArgumentsResolvedEvent(webRequest, null));
    }

    @Test
    void constructorAcceptsEmptyArguments() {
        WebRequest webRequest = mock(WebRequest.class);
        HandlerMethod handlerMethod = mock(HandlerMethod.class);
        HandlerMethodArgumentsResolvedEvent event =
                new HandlerMethodArgumentsResolvedEvent(webRequest, handlerMethod);
        assertEquals(0, event.getArguments().length);
    }

    @Test
    void toStringContainsClassName() {
        WebRequest webRequest = mock(WebRequest.class);
        HandlerMethod handlerMethod = mock(HandlerMethod.class);
        HandlerMethodArgumentsResolvedEvent event =
                new HandlerMethodArgumentsResolvedEvent(webRequest, handlerMethod, "x");
        assertTrue(event.toString().contains("HandlerMethodArgumentsResolvedEvent"));
    }
}
