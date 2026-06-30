package io.zhijun.spring.web.metadata;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WebEndpointMappingRegistrarTests {

    @Test
    void shouldRegisterResolvedMappings() {
        WebEndpointMappingRegistry registry = mock(WebEndpointMappingRegistry.class);
        when(registry.register(any())).thenReturn(true);

        WebEndpointMapping mapping = mock(WebEndpointMapping.class);
        WebEndpointMappingResolver resolver = mock(WebEndpointMappingResolver.class);
        when(resolver.resolve(any())).thenReturn(java.util.Collections.singletonList(mapping));

        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBeansOfType(WebEndpointMappingResolver.class))
                .thenReturn(java.util.Collections.singletonMap("resolver", resolver));
        when(context.getId()).thenReturn("test-context");

        WebEndpointMappingRegistrar registrar = new WebEndpointMappingRegistrar(registry);
        registrar.setApplicationContext(context);
        registrar.afterSingletonsInstantiated();

        verify(registry).register(mapping);
    }

    @Test
    void shouldHandleNoResolvers() {
        WebEndpointMappingRegistry registry = mock(WebEndpointMappingRegistry.class);
        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBeansOfType(WebEndpointMappingResolver.class))
                .thenReturn(java.util.Collections.emptyMap());
        when(context.getId()).thenReturn("empty-context");

        WebEndpointMappingRegistrar registrar = new WebEndpointMappingRegistrar(registry);
        registrar.setApplicationContext(context);
        registrar.afterSingletonsInstantiated();

        verify(registry, never()).register(any());
    }

    @Test
    void shouldSkipNullResolverResults() {
        WebEndpointMappingRegistry registry = mock(WebEndpointMappingRegistry.class);
        WebEndpointMappingResolver resolver = mock(WebEndpointMappingResolver.class);
        when(resolver.resolve(any())).thenReturn(null);

        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBeansOfType(WebEndpointMappingResolver.class))
                .thenReturn(java.util.Collections.singletonMap("resolver", resolver));
        when(context.getId()).thenReturn("test-context");

        WebEndpointMappingRegistrar registrar = new WebEndpointMappingRegistrar(registry);
        registrar.setApplicationContext(context);
        registrar.afterSingletonsInstantiated();

        verify(registry, never()).register(any());
    }
}
