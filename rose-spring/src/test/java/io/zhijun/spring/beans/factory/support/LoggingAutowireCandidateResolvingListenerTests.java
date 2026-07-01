package io.zhijun.spring.beans.factory.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoggingAutowireCandidateResolvingListenerTests {

    private final LoggingAutowireCandidateResolvingListener listener = new LoggingAutowireCandidateResolvingListener();

    @Test
    void suggestedValueResolvedDoesNotThrow() {
        assertDoesNotThrow(() -> listener.suggestedValueResolved(null, null));
        assertDoesNotThrow(() -> listener.suggestedValueResolved(null, "value"));
    }

    @Test
    void lazyProxyResolvedDoesNotThrow() {
        assertDoesNotThrow(() -> listener.lazyProxyResolved(null, "bean", null));
        assertDoesNotThrow(() -> listener.lazyProxyResolved(null, "bean", new Object()));
    }
}
