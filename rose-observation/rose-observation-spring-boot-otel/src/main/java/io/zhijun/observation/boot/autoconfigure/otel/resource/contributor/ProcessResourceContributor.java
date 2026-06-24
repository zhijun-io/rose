package io.zhijun.observation.boot.autoconfigure.otel.resource.contributor;

import java.lang.management.ManagementFactory;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.resources.ResourceBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.zhijun.core.annotation.Incubating;

/**
 * A {@link ResourceContributor} that contributes process attributes (OpenTelemetry semantic conventions).
 */
@Incubating
public final class ProcessResourceContributor implements ResourceContributor {

    private static final Logger logger = LoggerFactory.getLogger(ProcessResourceContributor.class);

    public static final AttributeKey<Long> PROCESS_PID = AttributeKey.longKey("process.pid");

    @Override
    public void contribute(ResourceBuilder builder) {
        long pid = currentPid();
        if (pid > 0) {
            builder.put(PROCESS_PID, pid);
        }
    }

    private static long currentPid() {
        try {
            String jvmName = ManagementFactory.getRuntimeMXBean().getName();
            return Long.parseLong(jvmName.split("@")[0]);
        } catch (Exception ex) {
            logger.debug("Failed to resolve process PID from JVM name", ex);
            return -1;
        }
    }
}
