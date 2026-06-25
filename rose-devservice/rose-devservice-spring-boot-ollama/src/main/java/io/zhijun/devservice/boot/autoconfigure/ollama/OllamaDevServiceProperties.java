package io.zhijun.devservice.boot.autoconfigure.ollama;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;

/**
 * Ollama dev service properties.
 */
@ConfigurationProperties(prefix = OllamaDevServiceProperties.CONFIG_PREFIX)
public class OllamaDevServiceProperties extends BaseDevServiceProperties {

    public static final String CONFIG_PREFIX = "rose.dev.ollama";

    /** Dynamic property published when the dev container is running. */
    public static final String BASE_URL_PROPERTY = CONFIG_PREFIX + ".base-url";

    private boolean ignoreNativeService = false;

    public OllamaDevServiceProperties() {
        setImageName("ollama/ollama:0.30.7");
        setShared(true);
        setStartupTimeout(java.time.Duration.ofMinutes(2));
    }

    public boolean isIgnoreNativeService() {
        return ignoreNativeService;
    }

    public void setIgnoreNativeService(boolean ignoreNativeService) {
        this.ignoreNativeService = ignoreNativeService;
    }
}
