package io.zhijun.spring.beans.factory;

import io.zhijun.spring.beans.factory.support.AutowireCandidateResolvingListener;
import io.zhijun.spring.beans.factory.support.ListenableAutowireCandidateResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateResolver;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ListenableAutowireCandidateResolverTests {

    @Test
    void shouldDelegateIsAutowireCandidate() {
        AutowireCandidateResolver delegate = mock(AutowireCandidateResolver.class);
        ListenableAutowireCandidateResolver resolver = createResolver(delegate);
        resolver.isAutowireCandidate(null, null);
        verify(delegate).isAutowireCandidate(null, null);
    }

    @Test
    void shouldDelegateIsRequired() {
        AutowireCandidateResolver delegate = mock(AutowireCandidateResolver.class);
        ListenableAutowireCandidateResolver resolver = createResolver(delegate);
        resolver.isRequired(null);
        verify(delegate).isRequired(null);
    }

    @Test
    void shouldDelegateHasQualifier() {
        AutowireCandidateResolver delegate = mock(AutowireCandidateResolver.class);
        ListenableAutowireCandidateResolver resolver = createResolver(delegate);
        resolver.hasQualifier(null);
        verify(delegate).hasQualifier(null);
    }

    @Test
    void shouldGetSuggestedValueAndNotifyListeners() {
        AutowireCandidateResolver delegate = mock(AutowireCandidateResolver.class);
        when(delegate.getSuggestedValue(any())).thenReturn("testValue");
        AutowireCandidateResolvingListener listener = mock(AutowireCandidateResolvingListener.class);
        ListenableAutowireCandidateResolver resolver = createResolver(delegate);
        resolver.addListener(listener);

        DependencyDescriptor descriptor = mock(DependencyDescriptor.class);
        Object value = resolver.getSuggestedValue(descriptor);

        assertThat(value).isEqualTo("testValue");
        verify(listener).suggestedValueResolved(descriptor, "testValue");
    }

    @Test
    void shouldAddMultipleListeners() {
        AutowireCandidateResolver delegate = mock(AutowireCandidateResolver.class);
        AutowireCandidateResolvingListener l1 = mock(AutowireCandidateResolvingListener.class);
        AutowireCandidateResolvingListener l2 = mock(AutowireCandidateResolvingListener.class);
        ListenableAutowireCandidateResolver resolver = createResolver(delegate);
        resolver.addListener(l1, l2);

        when(delegate.getSuggestedValue(any())).thenReturn("val");
        resolver.getSuggestedValue(mock(DependencyDescriptor.class));
        verify(l1).suggestedValueResolved(any(), eq("val"));
        verify(l2).suggestedValueResolved(any(), eq("val"));
    }

    @Test
    void shouldCloneIfNecessary() {
        AutowireCandidateResolver delegate = mock(AutowireCandidateResolver.class);
        ListenableAutowireCandidateResolver resolver = createResolver(delegate);
        resolver.cloneIfNecessary();
        verify(delegate).cloneIfNecessary();
    }

    @Test
    void shouldNotWrapNonDefaultListableBeanFactory() {
        ListenableAutowireCandidateResolver resolver = new ListenableAutowireCandidateResolver();
        resolver.wrap(mock(org.springframework.beans.factory.BeanFactory.class));
    }

    private static ListenableAutowireCandidateResolver createResolver(AutowireCandidateResolver delegate) {
        ListenableAutowireCandidateResolver resolver = new ListenableAutowireCandidateResolver();
        setField(resolver, "delegate", delegate);
        return resolver;
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = ListenableAutowireCandidateResolver.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
