package io.zhijun.annotation.processor;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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
        try (InputStream input = SinceProcessor.class.getClassLoader().getResourceAsStream(path)) {
            assertThat(input).as(path).isNotNull();
            String services = new String(readAllBytes(input), StandardCharsets.UTF_8);
            assertThat(services).contains("io.zhijun.annotation.processor.SinceProcessor");
            assertThat(services).contains("io.zhijun.annotation.processor.InternalApiProcessor");
        }
    }

    private static byte[] readAllBytes(InputStream input) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }
}
