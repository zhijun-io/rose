package io.zhijun.dev.core.registration;

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.testcontainers.containers.GenericContainer;

/**
 * Starts dev service containers before datasource initialization.
 */
public class LocalServiceContainersInitializer implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        Map<String, GenericContainer> containers = applicationContext.getBeansOfType(GenericContainer.class);
        for (GenericContainer container : containers.values()) {
            if (!container.isRunning()) {
                container.start();
            }
        }
    }
}
