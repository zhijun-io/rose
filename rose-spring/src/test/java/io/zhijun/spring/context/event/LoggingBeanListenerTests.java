package io.zhijun.spring.context.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.RootBeanDefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class LoggingBeanListenerTests {

    private final LoggingBeanListener listener = new LoggingBeanListener();

    @Test
    void supportsAllBeans() {
        assertThat(listener.supports("anyBean")).isTrue();
        assertThat(listener.supports("")).isTrue();
    }

    @Test
    void lifecycleMethodsDoNotThrow() {
        RootBeanDefinition rbd = new RootBeanDefinition();
        assertThatCode(() -> listener.onBeanDefinitionReady("bean", rbd)).doesNotThrowAnyException();
        assertThatCode(() -> listener.onBeforeBeanInstantiate("bean", rbd)).doesNotThrowAnyException();
        assertThatCode(() -> listener.onAfterBeanInstantiated("bean", rbd, new Object())).doesNotThrowAnyException();
        assertThatCode(() -> listener.onBeforeBeanInitialize("bean", new Object())).doesNotThrowAnyException();
        assertThatCode(() -> listener.onAfterBeanInitialized("bean", new Object())).doesNotThrowAnyException();
        assertThatCode(() -> listener.onBeanReady("bean", new Object())).doesNotThrowAnyException();
        assertThatCode(() -> listener.onBeforeBeanDestroy("bean", new Object())).doesNotThrowAnyException();
        assertThatCode(() -> listener.onAfterBeanDestroy("bean", new Object())).doesNotThrowAnyException();
    }
}
