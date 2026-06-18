package io.zhijun.local.core.registration;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.testcontainers.containers.GenericContainer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LocalServiceContainersInitializer}.
 */
class LocalServiceContainersInitializerTests {

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

        LocalServiceContainersInitializer initializer = new LocalServiceContainersInitializer();
        initializer.setApplicationContext(applicationContext);
        initializer.afterPropertiesSet();

        verify(stoppedContainer).start();
        verify(runningContainer, never()).start();
    }

}
