package io.zhijun.spring.web.metadata;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Optional;

/**
 * 基于 Servlet 组件解析 {@link WebEndpointMapping} 的 {@link WebEndpointMappingResolver} 实现。
 */
public class ServletWebEndpointMappingResolver implements WebEndpointMappingResolver {

    @Override
    public Collection<WebEndpointMapping> resolve(ApplicationContext context) {
        ServletContext servletContext = getServletContext(context);
        return resolve(servletContext);
    }

     public Collection<WebEndpointMapping> resolve(ServletContext servletContext) {
         if (servletContext == null) {
             return Collections.emptyList();
         }
        List<WebEndpointMapping> results = new ArrayList<>();
        resolveFromFilters(servletContext, results);
        resolveFromServlets(servletContext, results);
        return results;
    }

    private void resolveFromFilters(ServletContext servletContext, List<WebEndpointMapping> results) {
        Map<String, ? extends FilterRegistration> filterRegistrations = servletContext.getFilterRegistrations();
        if (filterRegistrations.isEmpty()) return;

        FilterRegistrationWebEndpointMappingFactory factory = new FilterRegistrationWebEndpointMappingFactory(servletContext);
        for (String filterName : filterRegistrations.keySet()) {
            Optional<WebEndpointMapping> mapping = factory.create(filterName);
            mapping.ifPresent(results::add);
        }
    }

    private void resolveFromServlets(ServletContext servletContext, List<WebEndpointMapping> results) {
        Map<String, ? extends ServletRegistration> servletRegistrations = servletContext.getServletRegistrations();
        if (servletRegistrations.isEmpty()) return;

        ServletRegistrationWebEndpointMappingFactory factory = new ServletRegistrationWebEndpointMappingFactory(servletContext);
        for (String servletName : servletRegistrations.keySet()) {
            Optional<WebEndpointMapping> mapping = factory.create(servletName);
            mapping.ifPresent(results::add);
        }
    }

    private ServletContext getServletContext(ApplicationContext context) {
        if (context instanceof WebApplicationContext) {
            return ((WebApplicationContext) context).getServletContext();
        }
        return null;
    }
}
