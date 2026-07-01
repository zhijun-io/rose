package io.zhijun.spring.web.metadata;

import javax.servlet.FilterRegistration;
import javax.servlet.Registration;
import javax.servlet.ServletContext;
import java.util.Collection;

/**
 * Servlet {@link Registration} 的 {@link AbstractWebEndpointMappingFactory} 抽象实现。
 *
 * @param <R> Registration 类型
 */
public abstract class RegistrationWebEndpointMappingFactory<R extends Registration> extends AbstractWebEndpointMappingFactory<String> {

    protected final ServletContext servletContext;

    public RegistrationWebEndpointMappingFactory(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public final boolean supports(String endpoint) {
        return getRegistration(endpoint, servletContext) != null;
    }

    @Override
    protected final WebEndpointMapping doCreate(String endpoint) throws Throwable {
        R registration = getRegistration(endpoint, servletContext);
        String className = registration.getClassName();
        Collection<String> methods = getMethods(registration);
        Collection<String> patterns = getPatterns(registration);

        WebEndpointMapping.Builder builder = WebEndpointMapping.builder()
                .endpoint(endpoint)
                .patterns(patterns)
                .methods(methods)
                .source(className);
        contribute(endpoint, servletContext, builder);
        return builder.build();
    }

    protected abstract Collection<String> getMethods(R registration);

    protected abstract R getRegistration(String name, ServletContext servletContext);

    protected abstract Collection<String> getPatterns(R registration);

    protected void contribute(String endpoint, ServletContext servletContext, WebEndpointMapping.Builder builder) throws Throwable {
    }
}
