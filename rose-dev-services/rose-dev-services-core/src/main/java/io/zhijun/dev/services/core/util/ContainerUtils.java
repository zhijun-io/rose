package io.zhijun.dev.services.core.util;

import io.zhijun.core.support.Internal;

/**
 * Container utilities.
 */
@Internal
public final class ContainerUtils {

    public static boolean isValidPort(int port) {
        return port > 0 && port <= 65535;
    }

    private ContainerUtils() {
    }
}
