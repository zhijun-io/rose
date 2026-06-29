package io.zhijun.devservice.boot.autoconfigure.bootstrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.Assert;

import io.zhijun.devservice.boot.autoconfigure.bootstrap.dev.BootstrapDevProperties;
import io.zhijun.devservice.boot.autoconfigure.bootstrap.test.BootstrapTestProperties;
import io.zhijun.devservice.boot.autoconfigure.bootstrap.template.BootstrapProfilesTemplate;
import io.zhijun.devservice.core.bootstrap.BootstrapMode;

/**
 * Activates profiles based on bootstrap mode.
 */
class BootstrapEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapEnvironmentPostProcessor.class);

    private final BootstrapProfilesTemplate template = new BootstrapProfilesTemplate();

    @Override
    @SuppressWarnings("unchecked")
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Assert.notNull(environment, "environment cannot be null");
        Assert.notNull(application, "application cannot be null");

        Boolean profilesEnabled =
                environment.getProperty(BootstrapProperties.PROFILES_ENABLED_PROPERTY, Boolean.class, true);
        if (!profilesEnabled) {
            return;
        }

        List<String> currentProfiles = environment.getProperty(
                StandardEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, List.class, new ArrayList<String>());

        BootstrapMode mode = BootstrapMode.detect();
        List<String> additionalProfiles;

        switch (mode) {
            case DEV:
                logger.info("The application is running in dev mode");
                List<String> developmentProfiles = environment.getProperty(
                        BootstrapDevProperties.PROFILES_PROPERTY,
                        List.class,
                        new ArrayList<String>(Collections.singletonList("dev")));
                additionalProfiles = template.resolve(mode, currentProfiles, developmentProfiles, null);
                break;
            case TEST:
                logger.info("The application is running in test mode");
                List<String> testProfiles = environment.getProperty(
                        BootstrapTestProperties.PROFILES_PROPERTY,
                        List.class,
                        new ArrayList<String>(Collections.singletonList("test")));
                additionalProfiles = template.resolve(mode, currentProfiles, null, testProfiles);
                break;
            case PROD:
                logger.debug("The application is running in prod mode");
                additionalProfiles = new ArrayList<String>();
                break;
            default:
                additionalProfiles = new ArrayList<String>();
                break;
        }

        ConfigDataEnvironmentPostProcessor.applyTo(
                environment, application.getResourceLoader(), null, additionalProfiles);
    }

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER + 5;
    }
}
