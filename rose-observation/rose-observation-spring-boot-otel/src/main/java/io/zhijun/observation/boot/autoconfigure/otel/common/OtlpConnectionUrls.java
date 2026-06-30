package io.zhijun.observation.boot.autoconfigure.otel.common;

import java.net.URI;

import org.springframework.util.Assert;

import io.zhijun.observation.boot.autoconfigure.otel.common.OpenTelemetryExporterProperties;

/**
 * Resolves OTLP endpoint URLs from common and signal-specific exporter configuration.
 */
public final class OtlpConnectionUrls {

    private OtlpConnectionUrls() {}

    public static String resolve(
            Protocol protocol,
            OpenTelemetryExporterProperties commonProperties,
            OtlpExporterConfig signalProperties,
            String httpProtobufPath,
            String defaultHttpProtobufEndpoint,
            String defaultGrpcEndpoint) {
        Protocol configuredProtocol = signalProperties.getProtocol() != null
                ? signalProperties.getProtocol()
                : commonProperties.getOtlp().getProtocol();
        Assert.state(
                protocol == configuredProtocol,
                String.format(
                        "Requested protocol %s doesn't match configured protocol %s", protocol, configuredProtocol));

        if (signalProperties.getEndpoint() != null) {
            return signalProperties.getEndpoint().toString();
        }
        if (commonProperties.getOtlp().getEndpoint() != null) {
            URI endpoint = commonProperties.getOtlp().getEndpoint();
            if (configuredProtocol == Protocol.HTTP_PROTOBUF) {
                return endpoint.toString()
                        + (endpoint.getPath().endsWith("/") ? httpProtobufPath.substring(1) : httpProtobufPath);
            }
            return endpoint.toString();
        }
        return configuredProtocol == Protocol.HTTP_PROTOBUF ? defaultHttpProtobufEndpoint : defaultGrpcEndpoint;
    }
}
