package io.zhijun.devservice.core.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OtlpPortsTests {

    @Test
    void testDefaultPorts() {
        assertEquals(4317, OtlpPorts.GRPC);
        assertEquals(4318, OtlpPorts.HTTP);
    }
}
