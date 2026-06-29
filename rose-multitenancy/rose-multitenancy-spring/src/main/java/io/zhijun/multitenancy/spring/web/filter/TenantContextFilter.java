package io.zhijun.multitenancy.spring.web.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;


import io.zhijun.multitenancy.core.context.TenantContext;
import io.zhijun.multitenancy.core.detail.TenantVerifier;
import io.zhijun.multitenancy.core.exception.TenantVerificationException;
import io.zhijun.multitenancy.spring.event.TenantContextAttachedEvent;
import io.zhijun.multitenancy.spring.event.TenantContextClosedEvent;
import io.zhijun.multitenancy.spring.web.resolver.HttpRequestTenantResolver;

/**
 * Establish a multitenancy context from an HTTP request, if multitenancy information is available.
 */

public final class TenantContextFilter extends OncePerRequestFilter implements Ordered {

    private static final String MISSING_TENANT_ERROR_MESSAGE =
            "A tenant identifier must be specified for HTTP requests to %s";

    private final HttpRequestTenantResolver httpRequestTenantResolver;

    private final TenantContextIgnorePathMatcher tenantContextIgnorePathMatcher;

    @Nullable
    private final TenantContextRequiredPathMatcher tenantContextRequiredPathMatcher;

    private final ApplicationEventPublisher eventPublisher;

    @Nullable
    private final TenantVerifier tenantVerifier;

    @Nullable
    private final TenantContextMissingTenantHandler missingTenantHandler;

    @Nullable
    private final Tracer tracer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TenantContextFilter(
            HttpRequestTenantResolver httpRequestTenantResolver,
            TenantContextIgnorePathMatcher tenantContextIgnorePathMatcher,
            @Nullable TenantContextRequiredPathMatcher tenantContextRequiredPathMatcher,
            ApplicationEventPublisher eventPublisher,
            @Nullable TenantVerifier tenantVerifier,
            @Nullable TenantContextMissingTenantHandler missingTenantHandler,
            @Nullable Tracer tracer) {
        Assert.notNull(httpRequestTenantResolver, "httpRequestTenantResolver cannot be null");
        Assert.notNull(tenantContextIgnorePathMatcher, "ignorePathMatcher cannot be null");
        Assert.notNull(eventPublisher, "eventPublisher cannot be null");
        this.httpRequestTenantResolver = httpRequestTenantResolver;
        this.tenantContextIgnorePathMatcher = tenantContextIgnorePathMatcher;
        this.tenantContextRequiredPathMatcher = tenantContextRequiredPathMatcher;
        this.eventPublisher = eventPublisher;
        this.tenantVerifier = tenantVerifier;
        this.missingTenantHandler = missingTenantHandler;
        this.tracer = tracer;
    }

    /**
     * Run early so downstream filters and the dispatcher can access the multitenancy context.
     * Slightly later than {@link Ordered#HIGHEST_PRECEDENCE} to allow character-encoding and
     * request-context filters to run first.
     */
    public static final int FILTER_ORDER = Ordered.HIGHEST_PRECEDENCE + 50;

    @Override
    public int getOrder() {
        return FILTER_ORDER;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String tenantIdentifier = httpRequestTenantResolver.resolveTenantIdentifier(request);
        if (!StringUtils.hasText(tenantIdentifier)) {
            if (tenantContextRequiredPathMatcher != null && !tenantContextRequiredPathMatcher.requires(request)) {
                filterChain.doFilter(request, response);
                return;
            }
            handleMissingTenant(request, response);
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
            if (tracer != null) {
                Span span = tracer.spanBuilder("tenant.context")
                        .setAttribute("tenant.id", tenantIdentifier)
                        .startSpan();
                try (Scope scope = span.makeCurrent()) {
                    runWithTenantContext(tenantIdentifier, request, response, filterChain);
                } catch (Exception ex) {
                    span.setStatus(StatusCode.ERROR);
                    span.recordException(ex);
                    throw ex;
                } finally {
                    span.end();
                }
            } else {
                runWithTenantContext(tenantIdentifier, request, response, filterChain);
            }
        } catch (ServletException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private void runWithTenantContext(
            String tenantIdentifier, HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws Exception {
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
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return tenantContextIgnorePathMatcher.matches(request);
    }

    private void handleMissingTenant(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (missingTenantHandler != null) {
            missingTenantHandler.handle(request, response);
            return;
        }
        handleTenantVerificationException(
                response, String.format(MISSING_TENANT_ERROR_MESSAGE, request.getRequestURI()));
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

        @Nullable
        private TenantContextRequiredPathMatcher tenantContextRequiredPathMatcher;

        private ApplicationEventPublisher eventPublisher;

        @Nullable
        private TenantVerifier tenantVerifier;

        @Nullable
        private TenantContextMissingTenantHandler missingTenantHandler;

        @Nullable
        private Tracer tracer;

        private Builder() {}

        public Builder httpRequestTenantResolver(HttpRequestTenantResolver httpRequestTenantResolver) {
            this.httpRequestTenantResolver = httpRequestTenantResolver;
            return this;
        }

        public Builder tenantContextIgnorePathMatcher(TenantContextIgnorePathMatcher tenantContextIgnorePathMatcher) {
            this.tenantContextIgnorePathMatcher = tenantContextIgnorePathMatcher;
            return this;
        }

        public Builder tenantContextRequiredPathMatcher(
                @Nullable TenantContextRequiredPathMatcher tenantContextRequiredPathMatcher) {
            this.tenantContextRequiredPathMatcher = tenantContextRequiredPathMatcher;
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

        public Builder missingTenantHandler(@Nullable TenantContextMissingTenantHandler missingTenantHandler) {
            this.missingTenantHandler = missingTenantHandler;
            return this;
        }

        public Builder tracer(@Nullable Tracer tracer) {
            this.tracer = tracer;
            return this;
        }

        public TenantContextFilter build() {
            return new TenantContextFilter(
                    httpRequestTenantResolver,
                    tenantContextIgnorePathMatcher,
                    tenantContextRequiredPathMatcher,
                    eventPublisher,
                    tenantVerifier,
                    missingTenantHandler,
                    tracer);
        }
    }
}
