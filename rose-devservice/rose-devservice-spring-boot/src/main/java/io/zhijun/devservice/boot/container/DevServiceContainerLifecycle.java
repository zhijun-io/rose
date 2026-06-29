package io.zhijun.devservice.boot.container;

import org.testcontainers.containers.Container;
import org.testcontainers.lifecycle.Startable;



/**
 * Starts dev service containers when connection details are resolved before the global initializer runs.
 */

public final class DevServiceContainerLifecycle {

    private DevServiceContainerLifecycle() {}

    public static void startIfNecessary(Container<?> container) {
        if (!container.isRunning()) {
            ((Startable) container).start();
        }
    }
}
