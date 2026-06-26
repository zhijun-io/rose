package io.zhijun.devservice.boot.autoconfigure.ollama;

import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * Condition to check if Ollama native connection is available.
 */
class OnOllamaNativeUnavailable extends SpringBootCondition {

    private static final String DEFAULT_BASE_URL = "http://localhost:11434";

    private static final String OLLAMA_BASE_URL_PROPERTY = OllamaDevServiceProperties.BASE_URL_PROPERTY;

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment environment = context.getEnvironment();

        try {
            OllamaDevServiceProperties devServicesProperties = Binder.get(environment)
                    .bindOrCreate(OllamaDevServiceProperties.CONFIG_PREFIX, OllamaDevServiceProperties.class);

            if (devServicesProperties.isIgnoreNativeService()) {
                return ConditionOutcome.match(String.format(
                        "Usage of Ollama native service is ignored: %s=%s",
                        OllamaDevServiceProperties.IGNORE_NATIVE_SERVICE_PROPERTY,
                        devServicesProperties.isIgnoreNativeService()));
            }

            String ollamaBaseUrl = resolveBaseUrl(environment);

            boolean isNativeConnection = isOllamaNativeConnection(ollamaBaseUrl);
            if (!isNativeConnection) {
                return ConditionOutcome.match("Ollama native connection is not available");
            }

            return ConditionOutcome.noMatch(String.format("Ollama native connection detected at %s", ollamaBaseUrl));
        } catch (Exception e) {
            return ConditionOutcome.match("Failed to evaluate Ollama condition: " + e.getMessage());
        }
    }

    private String resolveBaseUrl(Environment environment) {
        String configured = environment.getProperty(OLLAMA_BASE_URL_PROPERTY);
        if (StringUtils.hasText(configured)) {
            return configured;
        }
        return DEFAULT_BASE_URL;
    }

    boolean isOllamaNativeConnection(String baseUrl) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(baseUrl).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);
            int status = connection.getResponseCode();
            return status == 200;
        } catch (Exception e) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
