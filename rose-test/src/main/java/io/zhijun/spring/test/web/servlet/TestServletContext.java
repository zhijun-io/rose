package io.zhijun.spring.test.web.servlet;

import org.springframework.beans.BeanUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockRequestDispatcher;
import org.springframework.mock.web.MockServletContext;
import org.springframework.util.ClassUtils;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.EventListener;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * Test {@link ServletContext} based on {@link MockServletContext}.
 */
public class TestServletContext extends MockServletContext {

    private final Map<String, TestServletRegistration> servletRegistrations = new LinkedHashMap<>();

    private final Map<String, TestFilterRegistration> filterRegistrations = new LinkedHashMap<>();

    private final List<EventListener> listeners = new LinkedList<>();

    public TestServletContext() {
        this("");
    }

    public TestServletContext(String resourceBasePath) {
        this(resourceBasePath, null);
    }

    public TestServletContext(String resourceBasePath, ResourceLoader resourceLoader) {
        super(resourceBasePath, resourceLoader);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        return addServlet(servletName, className, null);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        return addServlet(servletName, servlet.getClass().getName(), servlet);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        return addServlet(servletName, servletClass.getName());
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> c) throws ServletException {
        return createInstance(c);
    }

    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        return servletRegistrations.get(servletName);
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return unmodifiableMap(this.servletRegistrations);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        return addFilter(filterName, className, null);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        return addFilter(filterName, filter.getClass().getName(), filter);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        return addFilter(filterName, filterClass.getName());
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> c) throws ServletException {
        return createInstance(c);
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        return filterRegistrations.get(filterName);
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return unmodifiableMap(filterRegistrations);
    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {
        EventListener listener = BeanUtils.instantiateClass(listenerClass);
        addListener(listener);
    }

    @Override
    public void addListener(String className) {
        EventListener listener = createInstance(className);
        addListener(listener);
    }

    @Override
    public <T extends EventListener> void addListener(T t) {
        listeners.add(t);
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> c) throws ServletException {
        return createInstance(c);
    }

    protected ServletRegistration.Dynamic addServlet(String servletName, String servletClassName, Servlet servlet) {
        Servlet actualServlet = servlet == null ? createInstance(servletClassName) : servlet;
        TestServletRegistration registration = new TestServletRegistration(servletName, servletClassName, actualServlet);
        servletRegistrations.put(servletName, registration);
        return registration;
    }

    protected FilterRegistration.Dynamic addFilter(String filterName, String filterClassName, Filter filter) {
        Filter actualFilter = filter == null ? createInstance(filterClassName) : filter;
        TestFilterRegistration filterRegistration = new TestFilterRegistration(filterName, filterClassName, actualFilter);
        filterRegistrations.put(filterName, filterRegistration);
        return filterRegistration;
    }

    @SuppressWarnings("unchecked")
    protected <T> T createInstance(String className) {
        Class<T> klass;
        try {
            klass = (Class<T>) ClassUtils.forName(className, getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return createInstance(klass);
    }

    protected <T> T createInstance(Class<T> c) {
        return BeanUtils.instantiateClass(c);
    }
}
