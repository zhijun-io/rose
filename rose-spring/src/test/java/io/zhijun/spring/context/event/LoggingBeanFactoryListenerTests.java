package io.zhijun.spring.context.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoggingBeanFactoryListenerTests {

    private final LoggingBeanFactoryListener listener = new LoggingBeanFactoryListener();

    @Test
    void methodsDoNotThrowWithNull() {
        assertDoesNotThrow(() -> listener.onBeanDefinitionRegistryReady(null));
        assertDoesNotThrow(() -> listener.onBeanFactoryReady(null));
        assertDoesNotThrow(() -> listener.onBeanFactoryConfigurationFrozen(null));
    }
}
