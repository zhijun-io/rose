package io.zhijun.test.boot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ApplicationContextTestSupportTests {

    @Test
    void createsApplicationContextRunner() {
        ApplicationContextRunner runner = ApplicationContextTestSupport.autoConfiguration();
        assertThat(runner).isNotNull();
    }
}
