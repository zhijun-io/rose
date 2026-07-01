package io.zhijun.spring.test.web.servlet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Test {@link Filter} implementation.
 */
public class TestFilter implements Filter {

    public static final String DEFAULT_FILTER_NAME = "testFilter";
    public static final String FILTER_CLASS_NAME = TestFilter.class.getName();
    public static final String DEFAULT_FILTER_URL_PATTERN = "/" + DEFAULT_FILTER_NAME;

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
    }

    @Override
    public void destroy() {
    }
}
