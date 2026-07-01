package io.zhijun.spring.test.web.servlet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableMap;

/**
 * Test {@link FilterRegistration.Dynamic} implementation.
 */
public class TestFilterRegistration implements FilterRegistration.Dynamic {

    private final String filterName;
    private final String filterClassName;
    private final Filter filter;
    private final Set<String> servletNames = new LinkedHashSet<>();
    private final Set<String> urlPatterns = new LinkedHashSet<>();
    private final Map<String, String> initParameters = new HashMap<>();
    private boolean asyncSupported;

    public TestFilterRegistration(String filterName, String filterClassName, Filter filter) {
        if (filterName == null || filterName.isEmpty()) {
            throw new IllegalArgumentException("The 'filterName' argument must not be empty!");
        }
        if (filterClassName == null || filterClassName.isEmpty()) {
            throw new IllegalArgumentException("The 'filterClassName' argument must not be empty!");
        }
        if (filter == null) {
            throw new IllegalArgumentException("The 'filter' argument must not be null!");
        }
        this.filterName = filterName;
        this.filterClassName = filterClassName;
        this.filter = filter;
    }

    @Override
    public void setAsyncSupported(boolean isAsyncSupported) {
        this.asyncSupported = isAsyncSupported;
    }

    @Override
    public String getName() {
        return filterName;
    }

    @Override
    public String getClassName() {
        return filterClassName;
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        return initParameters.put(name, value) == null;
    }

    @Override
    public String getInitParameter(String name) {
        return initParameters.get(name);
    }

    @Override
    public Set<String> setInitParameters(Map<String, String> initParameters) {
        this.initParameters.putAll(initParameters);
        return this.initParameters.keySet();
    }

    @Override
    public Map<String, String> getInitParameters() {
        return unmodifiableMap(initParameters);
    }

    @Override
    public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames) {
        if (servletNames == null || servletNames.length == 0) {
            throw new IllegalArgumentException("The 'servletNames' argument must not be null or empty!");
        }
        for (String servletName : servletNames) {
            this.servletNames.add(servletName);
        }
    }

    @Override
    public Collection<String> getServletNameMappings() {
        return unmodifiableCollection(servletNames);
    }

    @Override
    public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns) {
        if (urlPatterns == null || urlPatterns.length == 0) {
            throw new IllegalArgumentException("The 'urlPatterns' argument must not be null or empty!");
        }
        for (String urlPattern : urlPatterns) {
            this.urlPatterns.add(urlPattern);
        }
    }

    @Override
    public Collection<String> getUrlPatternMappings() {
        return unmodifiableCollection(urlPatterns);
    }

    public Filter getFilter() {
        return filter;
    }

    public boolean isAsyncSupported() {
        return asyncSupported;
    }
}
