package io.zhijun.observability.otel.autoconfigure.resource.contributor;

import java.lang.management.ManagementFactory;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.resources.ResourceBuilder;

import io.zhijun.core.annotation.Incubating;

/**
 * A {@link ResourceContributor} that contributes process attributes (OpenTelemetry semantic conventions).
 */
@Incubating
public final class ProcessResourceContributor implements ResourceContributor {

    public static final AttributeKey<Long> PROCESS_PID = AttributeKey.longKey("process.pid");

    @Override
    public void contribute(ResourceBuilder builder) {
        builder.put(PROCESS_PID, currentPid());
    }

    private static long currentPid() {
        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        return Long.parseLong(jvmName.split("@")[0]);
    }
}
