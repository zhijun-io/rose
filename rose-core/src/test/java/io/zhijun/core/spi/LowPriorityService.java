package io.zhijun.core.spi;

import io.zhijun.core.spi.annotation.Priority;
import io.zhijun.core.spi.annotation.SpiImpl;

@SpiImpl
@Priority(200)
public class LowPriorityService implements TestSpi {
    @Override
    public String greet() {
        return "low";
    }
}
