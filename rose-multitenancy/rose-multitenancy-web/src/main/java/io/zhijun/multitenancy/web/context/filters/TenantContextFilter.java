package io.zhijun.multitenancy.web.context.filters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.zhijun.core.support.Incubating;
import io.zhijun.multitenancy.core.context.TenantContext;
import io.zhijun.multitenancy.core.context.events.TenantContextAttachedEvent;
import io.zhijun.multitenancy.core.context.events.TenantContextClosedEvent;
import io.zhijun.multitenancy.core.exceptions.TenantVerificationException;
import io.zhijun.multitenancy.core.observability.TenantObservationFilter;
import io.zhijun.multitenancy.core.tenantdetails.TenantVerifier;
import io.zhijun.multitenancy.web.context.resolvers.HttpRequestTenantResolver;

/**
 * Establish a tenant context from an HTTP request, if tenant information is available.
 */
@Incubating
public final class TenantContextFilter extends OncePerRequestFilter {

    private static final String MISSING_TENANT_ERROR_MESSAGE =
            "A tenant identifier must be specified for HTTP requests to %s";

    private final HttpRequestTenantResolver httpRequestTenantResolver;

    private final TenantContextIgnorePathMatcher tenantContextIgnorePathMatcher;

    private final ApplicationEventPublisher eventPublisher;

    @Nullable
    private final TenantVerifier tenantVerifier;

    @Nullable
    private final TenantObservationFilter tenantObservationFilter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TenantContextFilter(HttpRequestTenantResolver httpRequestTenantResolver,
            TenantContextIgnorePathMatcher tenantContextIgnorePathMatcher,
            ApplicationEventPublisher eventPublisher, @Nullable TenantVerifier tenantVerifier,
            @Nullable TenantObservationFilter tenantObservationFilter) {
        Assert.notNull(httpRequestTenantResolver, "httpRequestTenantResolver cannot be null");
        Assert.notNull(tenantContextIgnorePathMatcher, "ignorePathMatcher cannot be null");
        Assert.notNull(eventPublisher, "eventPublisher cannot be null");
        this.httpRequestTenantResolver = httpRequestTenantResolver;
        this.tenantContextIgnorePathMatcher = tenantContextIgnorePathMatcher;
        this.eventPublisher = eventPublisher;
        this.tenantVerifier = tenantVerifier;
        this.tenantObservationFilter = tenantObservationFilter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String tenantIdentifier = httpRequestTenantResolver.resolveTenantIdentifier(request);
        if (!StringUtils.hasText(tenantIdentifier)) {
            handleTenantVerificationException(response,
                    String.format(MISSING_TENANT_ERROR_MESSAGE, request.getRequestURI()));
            return;
        }

        if (tenantVerifier != null) {
            try {
                tenantVerifier.verify(tenantIdentifier);
            } catch (TenantVerificationException exception) {
                handleTenantVerificationException(response, exception.getMessage());
                return;
            }
        }

        try {
            TenantContext.where(tenantIdentifier).call(new java.util.concurrent.Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    eventPublisher.publishEvent(new TenantContextAttachedEvent(tenantIdentifier, request));
                    try {
                        filterChain.doFilter(request, response);
                    } finally {
                        eventPublisher.publishEvent(new TenantContextClosedEvent(tenantIdentifier, request));
                    }
                    return null;
                }
            });
        } catch (ServletException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return tenantContextIgnorePathMatcher.matches(request);
    }

    private void handleTenantVerificationException(HttpServletResponse response, String exceptionMessage)
            throws IOException {
        Map<String, Object> body = errorResponse(exceptionMessage);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private static Map<String, Object> errorResponse(String detail) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("status", 400);
        body.put("detail", detail);
        return body;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private HttpRequestTenantResolver httpRequestTenantResolver;

        private TenantContextIgnorePathMatcher tenantContextIgnorePathMatcher;

        private ApplicationEventPublisher eventPublisher;

        @Nullable
        private TenantVerifier tenantVerifier;

        @Nullable
        private TenantObservationFilter tenantObservationFilter;

        private Builder() {}

        public Builder httpRequestTenantResolver(HttpRequestTenantResolver httpRequestTenantResolver) {
            this.httpRequestTenantResolver = httpRequestTenantResolver;
            return this;
        }

        public Builder tenantContextIgnorePathMatcher(TenantContextIgnorePathMatcher tenantContextIgnorePathMatcher) {
            this.tenantContextIgnorePathMatcher = tenantContextIgnorePathMatcher;
            return this;
        }

        public Builder eventPublisher(ApplicationEventPublisher eventPublisher) {
            this.eventPublisher = eventPublisher;
            return this;
        }

        public Builder tenantVerifier(@Nullable TenantVerifier tenantVerifier) {
            this.tenantVerifier = tenantVerifier;
            return this;
        }

        public Builder tenantObservationFilter(@Nullable TenantObservationFilter tenantObservationFilter) {
            this.tenantObservationFilter = tenantObservationFilter;
            return this;
        }

        public TenantContextFilter build() {
            return new TenantContextFilter(httpRequestTenantResolver, tenantContextIgnorePathMatcher, eventPublisher,
                    tenantVerifier, tenantObservationFilter);
        }
    }

}
