package io.zhijun.multitenancy.core.detail;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import io.zhijun.multitenancy.core.exception.TenantVerificationException;

class FormatTenantVerifierTests {

    private final FormatTenantVerifier verifier = new FormatTenantVerifier();

    @Test
    void shouldAcceptValidIdentifier() {
        assertThatCode(() -> verifier.verify("tenant-1")).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectNullIdentifier() {
        assertThatThrownBy(() -> verifier.verify(null)).isInstanceOf(TenantVerificationException.class);
    }

    @Test
    void shouldRejectInvalidCharacters() {
        assertThatThrownBy(() -> verifier.verify("tenant;drop")).isInstanceOf(TenantVerificationException.class);
    }
}
