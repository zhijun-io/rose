package io.zhijun.spring.webmvc.advice;

import org.junit.jupiter.api.Test;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import static org.junit.jupiter.api.Assertions.*;

class StoringResponseBodyReturnValueAdviceTests {

    @Test
    void supportsReturnsTrueForJacksonConverter() {
        StoringResponseBodyReturnValueAdvice advice = new StoringResponseBodyReturnValueAdvice();
        assertTrue(advice.supports(null, MappingJackson2HttpMessageConverter.class));
    }

    @Test
    void supportsReturnsTrueForStringConverter() {
        StoringResponseBodyReturnValueAdvice advice = new StoringResponseBodyReturnValueAdvice();
        assertTrue(advice.supports(null, StringHttpMessageConverter.class));
    }

    @Test
    void supportsReturnsFalseForUnsupportedConverter() {
        StoringResponseBodyReturnValueAdvice advice = new StoringResponseBodyReturnValueAdvice();
        assertFalse(advice.supports(null, ByteArrayHttpMessageConverter.class));
    }
}
