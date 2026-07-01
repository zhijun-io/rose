package io.zhijun.spring.test.web.servlet;

import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletSecurityElement;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

/**
 * Test {@link ServletRegistration.Dynamic} implementation.
 */
public class TestServletRegistration implements ServletRegistration.Dynamic {

    private final String servletName;
    private final String servletClassName;
    private final Servlet servlet;
    private ServletSecurityElement servletSecurityElement;
    private MultipartConfigElement multipartConfig;
    private final Set<String> urlPatterns = new LinkedHashSet<>();
    private final Map<String, String> initParameters = new LinkedHashMap<>();
    private int loadOnStartup;
    private String roleName;
    private boolean asyncSupported;

    public TestServletRegistration(String servletName, String servletClassName, Servlet servlet) {
        if (servletName == null || servletName.isEmpty()) {
            throw new IllegalArgumentException("The 'servletName' argument must not be empty!");
        }
        if (servletClassName == null || servletClassName.isEmpty()) {
            throw new IllegalArgumentException("The 'servletClassName' argument must not be empty!");
        }
        if (servlet == null) {
            throw new IllegalArgumentException("The 'servlet' argument must not be null!");
        }
        this.servletName = servletName;
        this.servletClassName = servletClassName;
        this.servlet = servlet;
    }

    @Override
    public void setLoadOnStartup(int loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
    }

    @Override
    public Set<String> setServletSecurity(ServletSecurityElement constraint) {
        this.servletSecurityElement = constraint;
        return emptySet();
    }

    @Override
    public void setMultipartConfig(MultipartConfigElement multipartConfig) {
        this.multipartConfig = multipartConfig;
    }

    @Override
    public void setRunAsRole(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public void setAsyncSupported(boolean isAsyncSupported) {
        this.asyncSupported = isAsyncSupported;
    }

    @Override
    public Set<String> addMapping(String... urlPatterns) {
        if (urlPatterns == null || urlPatterns.length == 0) {
            throw new IllegalArgumentException("The 'urlPatterns' argument must not be null or empty!");
        }
        for (String urlPattern : urlPatterns) {
            this.urlPatterns.add(urlPattern);
        }
        return unmodifiableSet(this.urlPatterns);
    }

    @Override
    public Collection<String> getMappings() {
        return unmodifiableCollection(urlPatterns);
    }

    @Override
    public String getRunAsRole() {
        return roleName;
    }

    @Override
    public String getName() {
        return servletName;
    }

    @Override
    public String getClassName() {
        return servletClassName;
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
        return unmodifiableMap(this.initParameters);
    }

    public ServletSecurityElement getServletSecurityElement() {
        return servletSecurityElement;
    }

    public MultipartConfigElement getMultipartConfig() {
        return multipartConfig;
    }

    public Servlet getServlet() {
        return servlet;
    }

    public Set<String> getUrlPatterns() {
        return unmodifiableSet(urlPatterns);
    }

    public int getLoadOnStartup() {
        return loadOnStartup;
    }

    public String getRoleName() {
        return roleName;
    }

    public boolean isAsyncSupported() {
        return asyncSupported;
    }
}
