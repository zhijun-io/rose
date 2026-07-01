package io.zhijun.spring.beans.factory.support;

import io.zhijun.spring.beans.factory.AutowireCandidateResolvingListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.DependencyDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompositeAutowireCandidateResolvingListenerTests {

    @Test
    void constructorRejectsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new CompositeAutowireCandidateResolvingListener(null));
    }

    @Test
    void constructorRejectsEmptyList() {
        assertThrows(IllegalArgumentException.class,
                () -> new CompositeAutowireCandidateResolvingListener(Collections.emptyList()));
    }

    @Test
    void suggestedValueResolvedDelegatesToAll() {
        List<String> calls = new ArrayList<>();
        List<AutowireCandidateResolvingListener> listeners = new ArrayList<>();
        listeners.add(recordingListener(calls, "first"));
        listeners.add(recordingListener(calls, "second"));

        CompositeAutowireCandidateResolvingListener composite = new CompositeAutowireCandidateResolvingListener(listeners);
        composite.suggestedValueResolved(null, "value");

        assertEquals(2, calls.size());
        assertEquals("first", calls.get(0));
        assertEquals("second", calls.get(1));
    }

    @Test
    void lazyProxyResolvedDelegatesToAll() {
        List<String> calls = new ArrayList<>();
        List<AutowireCandidateResolvingListener> listeners = new ArrayList<>();
        listeners.add(recordingListener(calls, "first"));
        listeners.add(recordingListener(calls, "second"));

        CompositeAutowireCandidateResolvingListener composite = new CompositeAutowireCandidateResolvingListener(listeners);
        composite.lazyProxyResolved(null, "myBean", new Object());

        assertEquals(2, calls.size());
        assertEquals("first", calls.get(0));
        assertEquals("second", calls.get(1));
    }

    private static AutowireCandidateResolvingListener recordingListener(List<String> calls, String name) {
        return new AutowireCandidateResolvingListener() {
            @Override
            public void suggestedValueResolved(DependencyDescriptor descriptor, Object suggestedValue) {
                calls.add(name);
            }

            @Override
            public void lazyProxyResolved(DependencyDescriptor descriptor, String beanName, Object proxy) {
                calls.add(name);
            }
        };
    }
}
