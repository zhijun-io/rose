package io.zhijun.spring.context.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.RootBeanDefinition;

import static org.junit.jupiter.api.Assertions.*;

class LoggingBeanListenerTests {

    private final LoggingBeanListener listener = new LoggingBeanListener();

    @Test
    void supportsAllBeans() {
        assertTrue(listener.supports("anyBean"));
        assertTrue(listener.supports(""));
    }

    @Test
    void lifecycleMethodsDoNotThrow() {
        RootBeanDefinition rbd = new RootBeanDefinition();
        assertDoesNotThrow(() -> listener.onBeanDefinitionReady("bean", rbd));
        assertDoesNotThrow(() -> listener.onBeforeBeanInstantiate("bean", rbd));
        assertDoesNotThrow(() -> listener.onAfterBeanInstantiated("bean", rbd, new Object()));
        assertDoesNotThrow(() -> listener.onBeforeBeanInitialize("bean", new Object()));
        assertDoesNotThrow(() -> listener.onAfterBeanInitialized("bean", new Object()));
        assertDoesNotThrow(() -> listener.onBeanReady("bean", new Object()));
        assertDoesNotThrow(() -> listener.onBeforeBeanDestroy("bean", new Object()));
        assertDoesNotThrow(() -> listener.onAfterBeanDestroy("bean", new Object()));
    }
}
