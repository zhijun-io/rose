package io.zhijun.spring.webmvc.advice;

import org.junit.jupiter.api.Test;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import static org.junit.jupiter.api.Assertions.*;

class StoringRequestBodyArgumentAdviceTests {

    @Test
    void supportsReturnsTrueForJacksonConverter() {
        StoringRequestBodyArgumentAdvice advice = new StoringRequestBodyArgumentAdvice();
        assertTrue(advice.supports(null, null, MappingJackson2HttpMessageConverter.class));
    }

    @Test
    void supportsReturnsTrueForStringConverter() {
        StoringRequestBodyArgumentAdvice advice = new StoringRequestBodyArgumentAdvice();
        assertTrue(advice.supports(null, null, StringHttpMessageConverter.class));
    }

    @Test
    void supportsReturnsFalseForUnsupportedConverter() {
        StoringRequestBodyArgumentAdvice advice = new StoringRequestBodyArgumentAdvice();
        assertFalse(advice.supports(null, null, ByteArrayHttpMessageConverter.class));
    }

    @Test
    void handleEmptyBodyReturnsBody() {
        StoringRequestBodyArgumentAdvice advice = new StoringRequestBodyArgumentAdvice();
        Object body = new Object();
        assertSame(body, advice.handleEmptyBody(body, null, null, null, null));
    }
}
