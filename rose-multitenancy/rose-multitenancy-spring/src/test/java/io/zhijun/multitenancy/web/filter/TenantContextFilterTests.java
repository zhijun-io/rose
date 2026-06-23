package io.zhijun.multitenancy.web.filter;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.ServletException;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import io.zhijun.multitenancy.spring.context.event.TenantContextAttachedEvent;
import io.zhijun.multitenancy.spring.context.event.TenantContextClosedEvent;
import io.zhijun.multitenancy.core.exception.TenantVerificationException;
import io.zhijun.multitenancy.core.detail.TenantVerifier;
import io.zhijun.multitenancy.web.resolver.HeaderTenantResolver;
import io.zhijun.multitenancy.web.resolver.HttpRequestTenantResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test for {@link TenantContextFilter}.
 */
class TenantContextFilterTests {

    @Test
    void whenNullTenantResolverThenThrow() {
        TenantContextIgnorePathMatcher noTenantPathMatcher = Mockito.mock(TenantContextIgnorePathMatcher.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        assertThatThrownBy(() -> TenantContextFilter.builder()
            .tenantContextIgnorePathMatcher(noTenantPathMatcher)
            .eventPublisher(eventPublisher)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("httpRequestTenantResolver cannot be null");
    }

    @Test
    void whenNullPathMatcherThenThrow() {
        HttpRequestTenantResolver httpRequestTenantResolver = Mockito.mock(HttpRequestTenantResolver.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        assertThatThrownBy(() -> TenantContextFilter.builder()
            .httpRequestTenantResolver(httpRequestTenantResolver)
            .eventPublisher(eventPublisher)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ignorePathMatcher cannot be null");
    }

    @Test
    void whenNullEventPublisherThenThrow() {
        HttpRequestTenantResolver httpRequestTenantResolver = Mockito.mock(HttpRequestTenantResolver.class);
        TenantContextIgnorePathMatcher noTenantPathMatcher = Mockito.mock(TenantContextIgnorePathMatcher.class);
        assertThatThrownBy(() -> TenantContextFilter.builder()
            .httpRequestTenantResolver(httpRequestTenantResolver)
            .tenantContextIgnorePathMatcher(noTenantPathMatcher)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("eventPublisher cannot be null");
    }

    @Test
    void whenTenantResolvedThenPublishEvents() throws ServletException, IOException {
        String tenantIdentifier = "acme";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HeaderTenantResolver.DEFAULT_HEADER_NAME, tenantIdentifier);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        TenantContextFilter filter = TenantContextFilter.builder()
            .httpRequestTenantResolver(new HeaderTenantResolver())
            .tenantContextIgnorePathMatcher(new TenantContextIgnorePathMatcher(Collections.<String>emptySet()))
            .eventPublisher(eventPublisher)
            .build();

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<ApplicationEvent> eventCaptor = ArgumentCaptor.forClass(ApplicationEvent.class);
        Mockito.verify(eventPublisher, Mockito.times(2)).publishEvent(eventCaptor.capture());

        assertThat(eventCaptor.getAllValues().get(0))
            .isExactlyInstanceOf(TenantContextAttachedEvent.class)
            .extracting(event -> (TenantContextAttachedEvent) event)
            .matches(event -> event.getTenantIdentifier().equals(tenantIdentifier))
            .matches(event -> event.getSource().equals(request));

        assertThat(eventCaptor.getAllValues().get(1))
            .isExactlyInstanceOf(TenantContextClosedEvent.class)
            .extracting(event -> (TenantContextClosedEvent) event)
            .matches(event -> event.getTenantIdentifier().equals(tenantIdentifier))
            .matches(event -> event.getSource().equals(request));
    }

    @Test
    void whenTenantVerifierRejectsThenReturnBadRequest() throws ServletException, IOException {
        String tenantIdentifier = "invalid-tenant";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HeaderTenantResolver.DEFAULT_HEADER_NAME, tenantIdentifier);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        TenantVerifier tenantVerifier = id -> {
            throw new TenantVerificationException("The resolved tenant is invalid or disabled");
        };
        TenantContextFilter filter = TenantContextFilter.builder()
            .httpRequestTenantResolver(new HeaderTenantResolver())
            .tenantContextIgnorePathMatcher(new TenantContextIgnorePathMatcher(Collections.<String>emptySet()))
            .eventPublisher(eventPublisher)
            .tenantVerifier(tenantVerifier)
            .build();

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("The resolved tenant is invalid or disabled");

        Mockito.verify(eventPublisher, Mockito.times(0)).publishEvent(Mockito.any(ApplicationEvent.class));
    }

    @Test
    void whenOptionalTenantPathThenContinueWithoutBinding() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/public/ping");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        TenantContextRequiredPathMatcher requiredPathMatcher = new TenantContextRequiredPathMatcher(
                Collections.singleton("/secured/**"), Collections.emptySet());
        TenantContextFilter filter = TenantContextFilter.builder()
            .httpRequestTenantResolver(new HeaderTenantResolver())
            .tenantContextIgnorePathMatcher(new TenantContextIgnorePathMatcher(Collections.<String>emptySet()))
            .tenantContextRequiredPathMatcher(requiredPathMatcher)
            .eventPublisher(eventPublisher)
            .build();

        filter.doFilter(request, response, filterChain);

        assertThat(filterChain.getRequest()).isNotNull();
        Mockito.verify(eventPublisher, Mockito.times(0)).publishEvent(Mockito.any(ApplicationEvent.class));
    }

    @Test
    void whenRequiredTenantNotResolvedThenReturnBadRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        TenantContextFilter filter = TenantContextFilter.builder()
            .httpRequestTenantResolver(new HeaderTenantResolver())
            .tenantContextIgnorePathMatcher(new TenantContextIgnorePathMatcher(Collections.<String>emptySet()))
            .eventPublisher(eventPublisher)
            .build();

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).contains("A tenant identifier must be specified for HTTP requests");

        Mockito.verify(eventPublisher, Mockito.times(0)).publishEvent(Mockito.any(ApplicationEvent.class));
    }

    @Test
    void whenIgnorePathThenNoTenantResolvedAndNoEventPublished() throws ServletException, IOException {
        String path = "/ignore-path";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(path);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        TenantContextFilter filter = TenantContextFilter.builder()
            .httpRequestTenantResolver(new HeaderTenantResolver())
            .tenantContextIgnorePathMatcher(new TenantContextIgnorePathMatcher(Collections.singleton(path)))
            .eventPublisher(eventPublisher)
            .build();

        filter.doFilter(request, response, filterChain);

        Mockito.verify(eventPublisher, Mockito.times(0)).publishEvent(Mockito.any(ApplicationEvent.class));
    }

}
