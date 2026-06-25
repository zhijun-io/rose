package io.zhijun.devservice.boot.registration;

import org.springframework.lang.Nullable;
import org.testcontainers.containers.Container;
import org.testcontainers.lifecycle.Startable;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

/**
 * Optional OpenTelemetry spans around dev service container startup.
 */
final class DevServiceContainerTracing {

    private static final String SPAN_NAME = "devservice.container.start";

    private DevServiceContainerTracing() {
    }

    static void startIfNecessary(Container<?> container, String serviceName, @Nullable Tracer tracer) {
        if (container.isRunning()) {
            return;
        }
        if (tracer == null) {
            start(container);
            return;
        }

        Span span = tracer.spanBuilder(SPAN_NAME)
                .setAttribute("devservice.name", serviceName)
                .setAttribute("devservice.container.type", container.getClass().getName())
                .startSpan();
        try (Scope scope = span.makeCurrent()) {
            start(container);
            String containerId = container.getContainerId();
            if (containerId != null) {
                span.setAttribute("devservice.container.id", containerId);
            }
        } catch (RuntimeException ex) {
            span.setStatus(StatusCode.ERROR);
            span.recordException(ex);
            throw ex;
        } finally {
            span.end();
        }
    }

    private static void start(Container<?> container) {
        if (container instanceof Startable) {
            ((Startable) container).start();
        } else {
            throw new IllegalStateException("Container does not implement Startable: " + container.getClass().getName());
        }
    }

}
