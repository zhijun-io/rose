package io.zhijun.devservice.boot.autoconfigure.docker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import io.zhijun.devservice.core.docker.DockerEnvironmentSupport;

/**
 * Configures Docker / Testcontainers before any dev service container starts.
 */
public final class DevServiceDockerEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        DockerEnvironmentSupport.configureIfNeeded();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
