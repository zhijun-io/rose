package io.zhijun.spring.web.metadata;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 从 {@link FilterRegistration} 创建 {@link WebEndpointMapping} 的工厂。
 */
public class FilterRegistrationWebEndpointMappingFactory extends RegistrationWebEndpointMappingFactory<FilterRegistration> {

    private final ServletRegistrationWebEndpointMappingFactory servletFactory;

    public FilterRegistrationWebEndpointMappingFactory(ServletContext servletContext) {
        super(servletContext);
        this.servletFactory = new ServletRegistrationWebEndpointMappingFactory(servletContext);
    }


    @Override
    protected FilterRegistration getRegistration(String name, ServletContext servletContext) {
        return servletContext.getFilterRegistration(name);
    }

    @Override
    protected Collection<String> getMethods(FilterRegistration registration) {
        Set<String> allMethods = new LinkedHashSet<>();

        // URL pattern 映射时所有 HTTP 方法
        Collection<String> urlPatternMappings = registration.getUrlPatternMappings();
        if (!urlPatternMappings.isEmpty()) {
            allMethods.addAll(ServletRegistrationWebEndpointMappingFactory.ALL_HTTP_METHODS);
        }

        // 从关联的 Servlet 获取方法
        for (String servletName : registration.getServletNameMappings()) {
            ServletRegistration servletRegistration = getServletRegistration(servletName);
            if (servletRegistration != null) {
                allMethods.addAll(servletFactory.getMethods(servletRegistration));
            }
        }
        return allMethods;
    }
    @Override
    protected Collection<String> getPatterns(FilterRegistration registration) {
        Set<String> patterns = new LinkedHashSet<>();
        patterns.addAll(registration.getUrlPatternMappings());

        // 从关联的 Servlet 获取 pattern
        for (String servletName : registration.getServletNameMappings()) {
            ServletRegistration servletRegistration = getServletRegistration(servletName);
            if (servletRegistration != null) {
                patterns.addAll(servletFactory.getPatterns(servletRegistration));
            }
        }
        return patterns;
    }

    private ServletRegistration getServletRegistration(String servletName) {
        return servletContext.getServletRegistration(servletName);
    }
}
