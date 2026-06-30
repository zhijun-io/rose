package io.zhijun.core.spi;

import io.zhijun.core.spi.annotation.SpiImpl;

@SpiImpl
public class NoPriorityService implements TestSpi {
    @Override
    public String greet() {
        return "none";
    }
}
