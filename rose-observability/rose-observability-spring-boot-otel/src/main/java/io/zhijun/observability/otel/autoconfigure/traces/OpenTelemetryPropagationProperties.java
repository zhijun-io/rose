package io.zhijun.observability.otel.autoconfigure.traces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Trace propagation settings for Rose OpenTelemetry (Boot 2.7 compatible).
 */
@ConfigurationProperties(prefix = OpenTelemetryPropagationProperties.CONFIG_PREFIX)
public class OpenTelemetryPropagationProperties {

    public static final String CONFIG_PREFIX = "rose.otel.traces.propagation";

    private List<PropagationType> produce = new ArrayList<PropagationType>(Arrays.asList(PropagationType.W3C));

    private List<PropagationType> consume = new ArrayList<PropagationType>(Arrays.asList(PropagationType.W3C));

    public List<PropagationType> getProduce() {
        return produce;
    }

    public void setProduce(List<PropagationType> produce) {
        this.produce = produce;
    }

    public List<PropagationType> getConsume() {
        return consume;
    }

    public void setConsume(List<PropagationType> consume) {
        this.consume = consume;
    }

    public enum PropagationType {
        W3C,
        B3,
        B3_MULTI
    }
}
