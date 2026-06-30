package io.zhijun.core.spi;

import io.zhijun.core.spi.annotation.Priority;
import io.zhijun.core.spi.annotation.SpiImpl;

@SpiImpl
@Priority(100)
public class HighPriorityService implements TestSpi {
    @Override
    public String greet() {
        return "high";
    }
}
