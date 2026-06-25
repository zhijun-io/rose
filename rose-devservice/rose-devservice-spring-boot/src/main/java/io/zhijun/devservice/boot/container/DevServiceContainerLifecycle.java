package io.zhijun.devservice.boot.container;

import org.testcontainers.containers.Container;
import org.testcontainers.lifecycle.Startable;

import io.zhijun.core.annotation.Incubating;

/**
 * Starts dev service containers when connection details are resolved before the global initializer runs.
 */
@Incubating
public final class DevServiceContainerLifecycle {

    private DevServiceContainerLifecycle() {
    }

    public static void startIfNecessary(Container<?> container) {
        if (!container.isRunning()) {
            ((Startable) container).start();
        }
    }

}
