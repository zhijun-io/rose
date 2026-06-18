package io.zhijun.spring.core.env;

import java.util.List;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import io.zhijun.spring.core.io.support.SpringFactoriesLoaderUtils;

/**
 * Initializes the listenable environment.
 */
public class ListenableConfigurableEnvironmentInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        applicationContext.setEnvironment(new ListenableConfigurableEnvironment(applicationContext.getEnvironment(),
                applicationContext));
    }
}
