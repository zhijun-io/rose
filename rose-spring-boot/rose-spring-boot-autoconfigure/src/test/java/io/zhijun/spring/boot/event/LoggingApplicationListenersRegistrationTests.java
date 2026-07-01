package io.zhijun.spring.boot.event;

import org.junit.jupiter.api.Test;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingApplicationListenersRegistrationTests {

    @Test
    void shouldRegisterLoggingApplicationListenersInSpringFactories() throws IOException {
        String content = resource("META-INF/spring.factories");
        assertThat(content).contains(LoggingSpringApplicationRunListener.class.getName());
        assertThat(content).contains(LoggingOnceApplicationPreparedEventListener.class.getName());
        assertThat(content).contains(LoggingOnceMainApplicationPreparedEventListener.class.getName());
    }

    private String resource(String name) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(name);
        assertThat(inputStream).as(name).isNotNull();
        try {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } finally {
            inputStream.close();
        }
    }
}
