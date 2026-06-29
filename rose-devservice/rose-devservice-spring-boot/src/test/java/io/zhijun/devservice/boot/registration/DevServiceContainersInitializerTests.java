package io.zhijun.devservice.boot.registration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.trace.Tracer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.testcontainers.containers.GenericContainer;

/**
 * Unit test for {@link DevServiceContainersInitializer}.
 */
class DevServiceContainersInitializerTests {

    @Test
    void startsContainersThatAreNotRunning() {
        GenericContainer<?> stoppedContainer = mock(GenericContainer.class);
        when(stoppedContainer.isRunning()).thenReturn(false);

        GenericContainer<?> runningContainer = mock(GenericContainer.class);
        when(runningContainer.isRunning()).thenReturn(true);

        ApplicationContext applicationContext = mock(ApplicationContext.class);
        java.util.Map<String, GenericContainer> containers = new java.util.HashMap<String, GenericContainer>();
        containers.put("stopped", stoppedContainer);
        containers.put("running", runningContainer);
        when(applicationContext.getBeansOfType(GenericContainer.class)).thenReturn(containers);

        @SuppressWarnings("unchecked")
        ObjectProvider<Tracer> tracerProvider = mock(ObjectProvider.class);
        when(tracerProvider.getIfAvailable()).thenReturn(null);

        BeanFactory beanFactory = mock(BeanFactory.class);
        when(beanFactory.getBeanProvider(Tracer.class)).thenReturn(tracerProvider);

        DevServiceContainersInitializer initializer = new DevServiceContainersInitializer(beanFactory);
        initializer.setApplicationContext(applicationContext);
        initializer.afterPropertiesSet();

        verify(stoppedContainer).start();
        verify(runningContainer, never()).start();
    }
}
