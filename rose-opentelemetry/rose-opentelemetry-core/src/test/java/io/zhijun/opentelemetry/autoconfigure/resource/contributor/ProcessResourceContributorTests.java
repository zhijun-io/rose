package io.zhijun.opentelemetry.autoconfigure.resource.contributor;

import io.opentelemetry.sdk.resources.ResourceBuilder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link ProcessResourceContributor}.
 */
@ExtendWith(MockitoExtension.class)
class ProcessResourceContributorTests {

    private final ProcessResourceContributor contributor = new ProcessResourceContributor();

    @Mock
    private ResourceBuilder resourceBuilder;

    @Test
    void shouldContributeProcessPid() {
        contributor.contribute(resourceBuilder);

        verify(resourceBuilder).put(ProcessResourceContributor.PROCESS_PID, currentPid());
    }

    private static long currentPid() {
        String jvmName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        return Long.parseLong(jvmName.split("@")[0]);
    }

}
