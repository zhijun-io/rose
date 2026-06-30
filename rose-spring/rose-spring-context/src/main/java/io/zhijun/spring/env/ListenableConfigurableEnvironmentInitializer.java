package io.zhijun.spring.env;

import io.zhijun.spring.context.SpringContextHolder;

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
