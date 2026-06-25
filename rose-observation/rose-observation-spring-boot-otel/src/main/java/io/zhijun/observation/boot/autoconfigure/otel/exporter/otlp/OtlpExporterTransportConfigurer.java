package io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;

import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporterBuilder;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporterBuilder;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporterBuilder;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporterBuilder;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporterBuilder;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;
import io.opentelemetry.sdk.common.export.ProxyOptions;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterProperties;

/**
 * Applies TLS and proxy settings to OTLP exporter builders.
 */
public final class OtlpExporterTransportConfigurer {

    private OtlpExporterTransportConfigurer() {}

    public interface TrustedCertificatesSetter<B> {
        void setTrustedCertificates(B builder, byte[] trustedCertificates);
    }

    public interface ClientTlsSetter<B> {
        void setClientTls(B builder, byte[] privateKey, byte[] certificate);
    }

    public interface ProxySetter<B> {
        void setProxy(B builder, ProxyOptions proxyOptions);
    }

    public static <B> void applyTls(B builder, OpenTelemetryExporterProperties commonProperties,
            OtlpExporterConfig signalProperties, TrustedCertificatesSetter<B> trustedCertificatesSetter,
            ClientTlsSetter<B> clientTlsSetter) {
        byte[] trustedCertificates = readBytes(resolveTrustedCertificates(commonProperties, signalProperties));
        if (trustedCertificates != null) {
            trustedCertificatesSetter.setTrustedCertificates(builder, trustedCertificates);
        }

        byte[] certificate = readBytes(resolveClientCertificate(commonProperties, signalProperties));
        byte[] key = readBytes(resolveClientKey(commonProperties, signalProperties));
        if (certificate != null && key != null) {
            clientTlsSetter.setClientTls(builder, key, certificate);
        }
    }

    public static <B> void applyProxy(B builder, OpenTelemetryExporterProperties commonProperties,
            OtlpExporterConfig signalProperties, ProxySetter<B> proxySetter) {
        ProxyOptions proxyOptions = resolveProxy(commonProperties, signalProperties);
        if (proxyOptions != null) {
            proxySetter.setProxy(builder, proxyOptions);
        }
    }

    @Nullable
    static String resolveTrustedCertificates(OpenTelemetryExporterProperties commonProperties,
            OtlpExporterConfig signalProperties) {
        return firstNonEmpty(signalProperties.getTls().getTrustedCertificates(),
                commonProperties.getOtlp().getTls().getTrustedCertificates());
    }

    @Nullable
    static String resolveClientCertificate(OpenTelemetryExporterProperties commonProperties,
            OtlpExporterConfig signalProperties) {
        return firstNonEmpty(signalProperties.getTls().getCertificate(),
                commonProperties.getOtlp().getTls().getCertificate());
    }

    @Nullable
    static String resolveClientKey(OpenTelemetryExporterProperties commonProperties, OtlpExporterConfig signalProperties) {
        return firstNonEmpty(signalProperties.getTls().getKey(), commonProperties.getOtlp().getTls().getKey());
    }

    @Nullable
    static ProxyOptions resolveProxy(OpenTelemetryExporterProperties commonProperties, OtlpExporterConfig signalProperties) {
        ProxyConfig signalProxy = signalProperties.getProxy();
        if (StringUtils.hasText(signalProxy.getHost())) {
            return ProxyOptions.create(new InetSocketAddress(signalProxy.getHost(), signalProxy.getPort()));
        }
        ProxyConfig commonProxy = commonProperties.getOtlp().getProxy();
        if (StringUtils.hasText(commonProxy.getHost())) {
            return ProxyOptions.create(new InetSocketAddress(commonProxy.getHost(), commonProxy.getPort()));
        }
        return null;
    }

    public static <B> void configureHttpTransport(B builder, OpenTelemetryExporterProperties commonProperties,
            OtlpExporterConfig signalProperties, TrustedCertificatesSetter<B> trustedCertificatesSetter,
            ClientTlsSetter<B> clientTlsSetter, ProxySetter<B> proxySetter) {
        applyTls(builder, commonProperties, signalProperties, trustedCertificatesSetter, clientTlsSetter);
        applyProxy(builder, commonProperties, signalProperties, proxySetter);
    }

    public static <B> void configureGrpcTransport(B builder, OpenTelemetryExporterProperties commonProperties,
            OtlpExporterConfig signalProperties, TrustedCertificatesSetter<B> trustedCertificatesSetter,
            ClientTlsSetter<B> clientTlsSetter) {
        applyTls(builder, commonProperties, signalProperties, trustedCertificatesSetter, clientTlsSetter);
    }

    public static void configureHttpTraceTransport(OtlpHttpSpanExporterBuilder builder,
            OpenTelemetryExporterProperties commonProperties, OtlpExporterConfig signalProperties) {
        configureHttpTransport(builder, commonProperties, signalProperties, OtlpHttpSpanExporterBuilder::setTrustedCertificates,
                OtlpHttpSpanExporterBuilder::setClientTls, OtlpHttpSpanExporterBuilder::setProxy);
    }

    public static void configureGrpcTraceTransport(OtlpGrpcSpanExporterBuilder builder,
            OpenTelemetryExporterProperties commonProperties, OtlpExporterConfig signalProperties) {
        configureGrpcTransport(builder, commonProperties, signalProperties, OtlpGrpcSpanExporterBuilder::setTrustedCertificates,
                OtlpGrpcSpanExporterBuilder::setClientTls);
    }

    public static void configureHttpMetricTransport(OtlpHttpMetricExporterBuilder builder,
            OpenTelemetryExporterProperties commonProperties, OtlpExporterConfig signalProperties) {
        configureHttpTransport(builder, commonProperties, signalProperties, OtlpHttpMetricExporterBuilder::setTrustedCertificates,
                OtlpHttpMetricExporterBuilder::setClientTls, OtlpHttpMetricExporterBuilder::setProxyOptions);
    }

    public static void configureGrpcMetricTransport(OtlpGrpcMetricExporterBuilder builder,
            OpenTelemetryExporterProperties commonProperties, OtlpExporterConfig signalProperties) {
        configureGrpcTransport(builder, commonProperties, signalProperties, OtlpGrpcMetricExporterBuilder::setTrustedCertificates,
                OtlpGrpcMetricExporterBuilder::setClientTls);
    }

    public static void configureHttpLogTransport(OtlpHttpLogRecordExporterBuilder builder,
            OpenTelemetryExporterProperties commonProperties, OtlpExporterConfig signalProperties) {
        configureHttpTransport(builder, commonProperties, signalProperties, OtlpHttpLogRecordExporterBuilder::setTrustedCertificates,
                OtlpHttpLogRecordExporterBuilder::setClientTls, OtlpHttpLogRecordExporterBuilder::setProxyOptions);
    }

    public static void configureGrpcLogTransport(OtlpGrpcLogRecordExporterBuilder builder,
            OpenTelemetryExporterProperties commonProperties, OtlpExporterConfig signalProperties) {
        configureGrpcTransport(builder, commonProperties, signalProperties, OtlpGrpcLogRecordExporterBuilder::setTrustedCertificates,
                OtlpGrpcLogRecordExporterBuilder::setClientTls);
    }

    @Nullable
    private static String firstNonEmpty(@Nullable String primary, @Nullable String fallback) {
        if (StringUtils.hasText(primary)) {
            return primary;
        }
        return StringUtils.hasText(fallback) ? fallback : null;
    }

    @Nullable
    static byte[] readBytes(@Nullable String location) {
        if (!StringUtils.hasText(location)) {
            return null;
        }
        try {
            Resource resource = toResource(location);
            try (InputStream inputStream = resource.getInputStream()) {
                return StreamUtils.copyToByteArray(inputStream);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read OTLP transport file: " + location, ex);
        }
    }

    private static Resource toResource(String location) {
        if (location.startsWith("classpath:")) {
            return new ClassPathResource(location.substring("classpath:".length()));
        }
        return new FileSystemResource(location);
    }

}
