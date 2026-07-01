package io.zhijun.spring.context.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class LoggingBeanFactoryListenerTests {

    private final LoggingBeanFactoryListener listener = new LoggingBeanFactoryListener();

    @Test
    void methodsDoNotThrowWithNull() {
        assertThatCode(() -> listener.onBeanDefinitionRegistryReady(null)).doesNotThrowAnyException();
        assertThatCode(() -> listener.onBeanFactoryReady(null)).doesNotThrowAnyException();
        assertThatCode(() -> listener.onBeanFactoryConfigurationFrozen(null)).doesNotThrowAnyException();
    }
}
