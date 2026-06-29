package io.zhijun.observation.boot.autoconfigure.logback;

import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;
import io.zhijun.core.annotation.Nullable;
import org.springframework.util.ClassUtils;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.Logger;

import io.zhijun.boot.bind.RoseBinder;
import io.zhijun.observation.boot.autoconfigure.otel.OpenTelemetryProperties;

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
                LogbackOpenTelemetryBridgeProperties.CAPTURE_EXPERIMENTAL_ATTRIBUTES_PROPERTY, false);
        openTelemetryAppender.setCaptureExperimentalAttributes(captureExperimentalAttributes);

        boolean captureKeyValuePairAttributes = binder.bindBoolean(
                LogbackOpenTelemetryBridgeProperties.CAPTURE_KEY_VALUE_PAIR_ATTRIBUTES_PROPERTY, false);
        openTelemetryAppender.setCaptureKeyValuePairAttributes(captureKeyValuePairAttributes);

        boolean captureMarkerAttribute =
                binder.bindBoolean(LogbackOpenTelemetryBridgeProperties.CAPTURE_MARKER_ATTRIBUTE_PROPERTY, false);
        openTelemetryAppender.setCaptureMarkerAttribute(captureMarkerAttribute);

        String captureMdcAttributes =
                binder.bindString(LogbackOpenTelemetryBridgeProperties.CAPTURE_MDC_ATTRIBUTES_PROPERTY, null);
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
        return binder.bindBoolean(LogbackOpenTelemetryBridgeProperties.ENABLED_PROPERTY, true);
    }

    private boolean isOpenTelemetryEnabled(RoseBinder binder) {
        return binder.bindBoolean(OpenTelemetryProperties.ENABLED_PROPERTY, true);
    }

    @Override
    public int getOrder() {
        return LoggingApplicationListener.DEFAULT_ORDER + 1;
    }
}
