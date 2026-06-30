package io.zhijun.devservice.boot.autoconfigure.ollama;

import io.zhijun.devservice.boot.autoconfigure.DevServiceProperties;
import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Ollama dev service properties.
 */
@ConfigurationProperties(prefix = OllamaDevServiceProperties.CONFIG_PREFIX)
public class OllamaDevServiceProperties extends BaseDevServiceProperties {

    public static final String SERVICE_NAME = "ollama";

    public static final String CONFIG_PREFIX = DevServiceProperties.CONFIG_PREFIX + "." + SERVICE_NAME;

    public static final String DEFAULT_IMAGE_NAME = "ollama/ollama:0.30.7";

    /** Dynamic property published when the dev container is running. */
    public static final String BASE_URL_PROPERTY = CONFIG_PREFIX + ".base-url";

    /** Property key for {@link #ignoreNativeService}. */
    public static final String IGNORE_NATIVE_SERVICE_PROPERTY = CONFIG_PREFIX + ".ignore-native-service";

    /** Skip Dev Service when a native Ollama endpoint is already available. */
    private boolean ignoreNativeService = false;

    public OllamaDevServiceProperties() {
        setImageName(DEFAULT_IMAGE_NAME);
        setShared(true);
        setStartupTimeout(HEAVY_STARTUP_TIMEOUT);
    }

    public boolean isIgnoreNativeService() {
        return ignoreNativeService;
    }

    public void setIgnoreNativeService(boolean ignoreNativeService) {
        this.ignoreNativeService = ignoreNativeService;
    }
}
