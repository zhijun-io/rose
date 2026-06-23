package io.zhijun.dev.core.util;

/**
 * Container utilities.
 */
public final class ContainerUtils {

    public static boolean isValidPort(int port) {
        return port > 0 && port <= 65535;
    }

    private ContainerUtils() {
    }
}
