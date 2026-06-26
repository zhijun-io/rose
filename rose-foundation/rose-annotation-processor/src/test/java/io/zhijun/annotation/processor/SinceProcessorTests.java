package io.zhijun.annotation.processor;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SinceProcessorTests {

    @Test
    void processorServiceIsRegistered() throws Exception {
        Class<?> processor = Class.forName("io.zhijun.annotation.processor.SinceProcessor");
        assertThat(javax.annotation.processing.AbstractProcessor.class).isAssignableFrom(processor);
    }

    @Test
    void autoServiceGeneratesProcessorSpi() throws Exception {
        String path = "META-INF/services/javax.annotation.processing.Processor";
        Enumeration<URL> resources = SinceProcessor.class.getClassLoader().getResources(path);
        assertThat(resources.hasMoreElements()).as(path).isTrue();

        boolean foundSince = false;
        boolean foundInternal = false;
        while (resources.hasMoreElements()) {
            String services = readAllBytes(resources.nextElement().openStream());
            if (services.contains("io.zhijun.annotation.processor.SinceProcessor")) {
                foundSince = true;
            }
            if (services.contains("io.zhijun.annotation.processor.InternalApiProcessor")) {
                foundInternal = true;
            }
        }

        assertThat(foundSince).isTrue();
        assertThat(foundInternal).isTrue();
    }

    private static String readAllBytes(InputStream input) throws Exception {
        return new String(readAllBytesToArray(input), StandardCharsets.UTF_8);
    }

    private static byte[] readAllBytesToArray(InputStream input) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }
}
