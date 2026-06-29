package io.zhijun.observation.boot.autoconfigure.otel.resource.contributor;

import static org.mockito.Mockito.verify;

import io.opentelemetry.sdk.resources.ResourceBuilder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit test for {@link ProcessResourceContributor}.
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
        String jvmName =
                java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        try {
            return Long.parseLong(jvmName.split("@")[0]);
        } catch (Exception e) {
            return -1;
        }
    }
}
