package io.zhijun.observability.logback.autoconfigure;

import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;

import org.springframework.lang.Nullable;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import ch.qos.logback.classic.Logger;

import io.zhijun.boot.context.properties.bind.RoseBinder;
import io.zhijun.observability.otel.autoconfigure.OpenTelemetryProperties;

/**
 * An {@link ApplicationListener} that configures the OpenTelemetry Logback appender
 * with the root Logback logger.
 */
class LogbackOpenTelemetryBridgeApplicationListener implements GenericApplicationListener {

    private static final Class<?>[] EVENT_TYPES = {ApplicationEnvironmentPreparedEvent.class};
    private static final Class<?>[] SOURCE_TYPES = {ApplicationContext.class, SpringApplication.class};

    @Override
    public boolean supportsEventType(ResolvableType eventType) {
        return isAssignableFrom(eventType.getRawClass(), EVENT_TYPES);
    }

    @Override
    public boolean supportsSourceType(@Nullable Class<?> sourceType) {
        return isAssignableFrom(sourceType, SOURCE_TYPES);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (!shouldRegisterLogbackAppender()) {
            return;
        }

        if (!(event instanceof ApplicationEnvironmentPreparedEvent)) {
            return;
        }
        ApplicationEnvironmentPreparedEvent applicationEvent = (ApplicationEnvironmentPreparedEvent) event;

        RoseBinder binder = RoseBinder.get(applicationEvent.getEnvironment());
        if (!isOpenTelemetryEnabled(binder) || !isLogbackAppenderBridgeEnabled(binder)) {
            return;
        }

        OpenTelemetryAppender openTelemetryAppender = new OpenTelemetryAppender();
        configureOpenTelemetryAppender(openTelemetryAppender, binder);
        openTelemetryAppender.start();

        Logger rootLogbackLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogbackLogger.addAppender(openTelemetryAppender);
    }

    private void configureOpenTelemetryAppender(OpenTelemetryAppender openTelemetryAppender, RoseBinder binder) {
        boolean captureExperimentalAttributes = binder.bindBoolean(
                LogbackOpenTelemetryBridgeProperties.CONFIG_PREFIX + ".capture-experimental-attributes", false);
        openTelemetryAppender.setCaptureExperimentalAttributes(captureExperimentalAttributes);

        boolean captureKeyValuePairAttributes = binder.bindBoolean(
                LogbackOpenTelemetryBridgeProperties.CONFIG_PREFIX + ".capture-key-value-pair-attributes", false);
        openTelemetryAppender.setCaptureKeyValuePairAttributes(captureKeyValuePairAttributes);

        boolean captureMarkerAttribute = binder.bindBoolean(
                LogbackOpenTelemetryBridgeProperties.CONFIG_PREFIX + ".capture-marker-attribute", false);
        openTelemetryAppender.setCaptureMarkerAttribute(captureMarkerAttribute);

        String captureMdcAttributes = binder.bindString(
                LogbackOpenTelemetryBridgeProperties.CONFIG_PREFIX + ".capture-mdc-attributes", null);
        openTelemetryAppender.setCaptureMdcAttributes(captureMdcAttributes);
    }

    private boolean isAssignableFrom(@Nullable Class<?> type, Class<?>... supportedTypes) {
        if (type != null) {
            for (Class<?> supportedType : supportedTypes) {
                if (supportedType.isAssignableFrom(type)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean shouldRegisterLogbackAppender() {
        return isLogbackPresent() && isOpenTelemetryPresent();
    }

    private boolean isLogbackPresent() {
        return ClassUtils.isPresent("ch.qos.logback.core.Appender", null);
    }

    private boolean isOpenTelemetryPresent() {
        return ClassUtils.isPresent("io.opentelemetry.api.OpenTelemetry", null);
    }

    private boolean isLogbackAppenderBridgeEnabled(RoseBinder binder) {
        return binder.bindBoolean(LogbackOpenTelemetryBridgeProperties.CONFIG_PREFIX + ".enabled", true);
    }

    private boolean isOpenTelemetryEnabled(RoseBinder binder) {
        return binder.bindBoolean(OpenTelemetryProperties.CONFIG_PREFIX + ".enabled", true);
    }

    @Override
    public int getOrder() {
        return LoggingApplicationListener.DEFAULT_ORDER + 1;
    }

}
