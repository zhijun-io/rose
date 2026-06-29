package io.zhijun.spring.boot.autoconfigure.spi;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Configuration;
import io.zhijun.core.spi.SpiLoader;

/**
 * SPI自动配置，在应用关闭时销毁所有SPI实例，调用其生命周期回调
 */
@Configuration(proxyBeanMethods = false)
public class SpiAutoConfiguration implements DisposableBean {

    @Override
    public void destroy() {
        SpiLoader.destroyAll();
    }
}