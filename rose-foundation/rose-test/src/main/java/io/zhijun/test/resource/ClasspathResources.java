package io.zhijun.test.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Classpath resource helpers for unit tests.
 */
public final class ClasspathResources {

    private ClasspathResources() {
    }

    public static String readUtf8(String classpathResource) {
        try (InputStream input = resolve(classpathResource)) {
            byte[] bytes = readAllBytes(input);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to read classpath resource: " + classpathResource, ex);
        }
    }

    public static InputStream open(String classpathResource) {
        try {
            return resolve(classpathResource);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to open classpath resource: " + classpathResource, ex);
        }
    }

    private static InputStream resolve(String classpathResource) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ClasspathResources.class.getClassLoader();
        }
        InputStream input = classLoader.getResourceAsStream(classpathResource);
        if (input == null) {
            throw new IOException("Classpath resource not found: " + classpathResource);
        }
        return input;
    }

    private static byte[] readAllBytes(InputStream input) throws IOException {
        byte[] buffer = new byte[4096];
        int read;
        int offset = 0;
        byte[] data = new byte[0];
        while ((read = input.read(buffer)) != -1) {
            byte[] next = new byte[offset + read];
            System.arraycopy(data, 0, next, 0, offset);
            System.arraycopy(buffer, 0, next, offset, read);
            data = next;
            offset += read;
        }
        return data;
    }
}
