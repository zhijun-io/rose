package io.zhijun.spring.test.util;

import io.zhijun.spring.test.web.servlet.TestFilter;
import io.zhijun.spring.test.web.servlet.TestServlet;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.util.EnumSet;

import static io.zhijun.spring.test.web.servlet.TestFilter.DEFAULT_FILTER_NAME;
import static io.zhijun.spring.test.web.servlet.TestFilter.DEFAULT_FILTER_URL_PATTERN;
import static io.zhijun.spring.test.web.servlet.TestServlet.DEFAULT_SERVLET_NAME;
import static io.zhijun.spring.test.web.servlet.TestServlet.DEFAULT_SERVLET_URL_PATTERN;
import static javax.servlet.DispatcherType.REQUEST;

/**
 * Servlet 测试工具类
 */
public abstract class ServletTestUtils {

    public static ServletRegistration.Dynamic addTestServlet(ServletContext servletContext) {
        ServletRegistration.Dynamic servletRegistration = servletContext.addServlet(DEFAULT_SERVLET_NAME, new TestServlet());
        servletRegistration.addMapping(DEFAULT_SERVLET_URL_PATTERN);
        return servletRegistration;
    }

    public static FilterRegistration.Dynamic addTestFilter(ServletContext servletContext) {
        ServletRegistration.Dynamic servletRegistration = servletContext.addServlet(DEFAULT_SERVLET_NAME, new TestServlet());
        servletRegistration.addMapping(DEFAULT_SERVLET_URL_PATTERN);

        FilterRegistration.Dynamic filterRegistration = servletContext.addFilter(DEFAULT_FILTER_NAME, new TestFilter());
        filterRegistration.addMappingForServletNames(EnumSet.of(REQUEST), true, DEFAULT_SERVLET_NAME);
        filterRegistration.addMappingForUrlPatterns(EnumSet.of(REQUEST), true, DEFAULT_FILTER_URL_PATTERN);
        return filterRegistration;
    }

    private ServletTestUtils() {
    }
}
