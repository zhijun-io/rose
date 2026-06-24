package io.zhijun.observation.boot.autoconfigure.otel.exporter.otlp;

import java.net.InetSocketAddress;
import java.net.URI;

import io.opentelemetry.sdk.common.export.ProxyOptions;

import org.junit.jupiter.api.Test;

import io.zhijun.observation.boot.autoconfigure.otel.exporter.OpenTelemetryExporterProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test for {@link OtlpExporterTransportConfigurer}.
 */
class OtlpExporterTransportConfigurerTests {

    @Test
    void shouldReadClasspathCertificate() {
        byte[] bytes = OtlpExporterTransportConfigurer.readBytes("classpath:otel-transport/test-ca.pem");

        assertThat(bytes).isNotEmpty();
        assertThat(new String(bytes)).contains("BEGIN CERTIFICATE");
    }

    @Test
    void shouldResolveProxyFromCommonProperties() {
        OpenTelemetryExporterProperties commonProperties = new OpenTelemetryExporterProperties();
        commonProperties.getOtlp().getProxy().setHost("proxy.local");
        commonProperties.getOtlp().getProxy().setPort(3128);

        ProxyOptions proxyOptions = OtlpExporterTransportConfigurer.resolveProxy(commonProperties, new OtlpExporterConfig());

        assertThat(proxyOptions).isNotNull();
        assertThat(proxyOptions.getProxySelector().select(URI.create("http://collector")).get(0).address())
                .isEqualTo(new InetSocketAddress("proxy.local", 3128));
    }

    @Test
    void shouldPreferSignalProxyOverCommonProxy() {
        OpenTelemetryExporterProperties commonProperties = new OpenTelemetryExporterProperties();
        commonProperties.getOtlp().getProxy().setHost("common-proxy");
        commonProperties.getOtlp().getProxy().setPort(8080);

        OtlpExporterConfig signalProperties = new OtlpExporterConfig();
        signalProperties.getProxy().setHost("signal-proxy");
        signalProperties.getProxy().setPort(9090);

        ProxyOptions proxyOptions = OtlpExporterTransportConfigurer.resolveProxy(commonProperties, signalProperties);

        assertThat(proxyOptions).isNotNull();
        assertThat(proxyOptions.getProxySelector().select(URI.create("http://collector")).get(0).address())
                .isEqualTo(new InetSocketAddress("signal-proxy", 9090));
    }

    @Test
    void shouldResolveTrustedCertificatesFromSignalProperties() {
        OpenTelemetryExporterProperties commonProperties = new OpenTelemetryExporterProperties();
        commonProperties.getOtlp().getTls().setTrustedCertificates("classpath:common.pem");

        OtlpExporterConfig signalProperties = new OtlpExporterConfig();
        signalProperties.getTls().setTrustedCertificates("classpath:signal.pem");

        assertThat(OtlpExporterTransportConfigurer.resolveTrustedCertificates(commonProperties, signalProperties))
                .isEqualTo("classpath:signal.pem");
    }

    @Test
    void shouldFailWhenTransportFileMissing() {
        assertThatThrownBy(() -> OtlpExporterTransportConfigurer.readBytes("classpath:missing.pem"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to read OTLP transport file");
    }

}
