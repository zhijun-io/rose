package io.zhijun.devservice.boot.registration;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.testcontainers.containers.GenericContainer;

import java.util.Map;

/**
 * Starts dev service containers before datasource initialization.
 */
public class DevServiceContainersInitializer implements ApplicationContextAware, InitializingBean {

    private final BeanFactory beanFactory;

    private ApplicationContext applicationContext;

    public DevServiceContainersInitializer(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void afterPropertiesSet() {
        Map<String, GenericContainer> containers = applicationContext.getBeansOfType(GenericContainer.class);
        for (Map.Entry<String, GenericContainer> entry : containers.entrySet()) {
            GenericContainer container = entry.getValue();
            if (!container.isRunning()) {
                container.start();
            }
        }
    }

    private static String resolveServiceName(String beanName) {
        String prefix = "devService.container.";
        if (beanName.startsWith(prefix)) {
            return beanName.substring(prefix.length());
        }
        return beanName;
    }
}
