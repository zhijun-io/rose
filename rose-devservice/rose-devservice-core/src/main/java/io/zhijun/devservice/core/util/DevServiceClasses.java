package io.zhijun.devservice.core.util;

/**
 * Classpath helpers for devservice-core (no Spring dependency).
 */
public final class DevServiceClasses {

    private DevServiceClasses() {
    }

    public static boolean isPresent(String className, ClassLoader classLoader) {
        try {
            Class.forName(className, false, classLoader != null ? classLoader : DevServiceClasses.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

}
