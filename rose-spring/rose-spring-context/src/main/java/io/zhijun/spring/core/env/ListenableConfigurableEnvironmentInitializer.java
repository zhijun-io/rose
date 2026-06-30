package io.zhijun.spring.core.env;

import io.zhijun.spring.core.context.SpringContextHolder;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;


/**
 * Initializes the listenable environment.
 */
public class ListenableConfigurableEnvironmentInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        applicationContext.setEnvironment(
                new ListenableConfigurableEnvironment(applicationContext.getEnvironment(), applicationContext));
        SpringContextHolder.bind(applicationContext);
    }
}
