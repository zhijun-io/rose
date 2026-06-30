package io.zhijun.observation.boot.autoconfigure.otel.common;

import org.jspecify.annotations.Nullable;

/**
 * TLS settings for OTLP exporters. Paths may be {@code classpath:...} or filesystem paths.
 */
public class TlsConfig {

    /**
     * PEM trusted CA certificate bundle for server verification.
     */
    @Nullable
    private String trustedCertificates;

    /**
     * PEM client certificate for mutual TLS.
     */
    @Nullable
    private String certificate;

    /**
     * PEM client private key for mutual TLS.
     */
    @Nullable
    private String key;

    @Nullable
    public String getTrustedCertificates() {
        return trustedCertificates;
    }

    public void setTrustedCertificates(@Nullable String trustedCertificates) {
        this.trustedCertificates = trustedCertificates;
    }

    @Nullable
    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(@Nullable String certificate) {
        this.certificate = certificate;
    }

    @Nullable
    public String getKey() {
        return key;
    }

    public void setKey(@Nullable String key) {
        this.key = key;
    }
}
