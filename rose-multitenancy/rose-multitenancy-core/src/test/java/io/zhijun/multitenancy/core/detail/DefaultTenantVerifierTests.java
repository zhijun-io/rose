package io.zhijun.multitenancy.core.detail;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import io.zhijun.multitenancy.core.exception.TenantVerificationException;

/**
 * Unit test for {@link DefaultTenantVerifier}.
 */
class DefaultTenantVerifierTests {

    @Test
    void whenNullTenantDetailsServiceThenThrow() {
        assertThatThrownBy(() -> new DefaultTenantVerifier(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("tenantDetailsService cannot be null");
    }

    @Test
    void whenNullTenantIdentifierThenThrow() {
        TenantDetailsService service = new StubTenantDetailsService(null);
        DefaultTenantVerifier verifier = new DefaultTenantVerifier(service);

        assertThatThrownBy(() -> verifier.verify(null))
                .isInstanceOf(TenantVerificationException.class)
                .hasMessageContaining(
                        "The tenant identifier must contain only alphanumeric characters, dashes (-), and underscores (_)");
    }

    @Test
    void whenTenantIdentifierContainsInvalidCharactersThenThrow() {
        TenantDetailsService service = new StubTenantDetailsService(null);
        DefaultTenantVerifier verifier = new DefaultTenantVerifier(service);

        assertThatThrownBy(() -> verifier.verify("acme\nmalicious"))
                .isInstanceOf(TenantVerificationException.class)
                .hasMessageContaining(
                        "The tenant identifier must contain only alphanumeric characters, dashes (-), and underscores (_)");
    }

    @Test
    void whenTenantIdentifierContainsAlphanumericDashUnderscoreThenPass() {
        Tenant tenant = Tenant.builder().identifier("acme-corp_2").enabled(true).build();
        TenantDetailsService service = new StubTenantDetailsService(tenant);
        DefaultTenantVerifier verifier = new DefaultTenantVerifier(service);

        assertThatNoException().isThrownBy(() -> verifier.verify("acme-corp_2"));
    }

    @Test
    void whenTenantExistsAndEnabledThenPass() {
        Tenant tenant = Tenant.builder().identifier("acme").enabled(true).build();
        TenantDetailsService service = new StubTenantDetailsService(tenant);
        DefaultTenantVerifier verifier = new DefaultTenantVerifier(service);

        assertThatNoException().isThrownBy(() -> verifier.verify("acme"));
    }

    @Test
    void whenTenantExistsButDisabledThenThrow() {
        Tenant tenant = Tenant.builder().identifier("acme").enabled(false).build();
        TenantDetailsService service = new StubTenantDetailsService(tenant);
        DefaultTenantVerifier verifier = new DefaultTenantVerifier(service);

        assertThatThrownBy(() -> verifier.verify("acme"))
                .isInstanceOf(TenantVerificationException.class)
                .hasMessageContaining("The resolved multitenancy is invalid or disabled");
    }

    @Test
    void whenTenantNotFoundThenThrow() {
        TenantDetailsService service = new StubTenantDetailsService(null);
        DefaultTenantVerifier verifier = new DefaultTenantVerifier(service);

        assertThatThrownBy(() -> verifier.verify("unknown"))
                .isInstanceOf(TenantVerificationException.class)
                .hasMessageContaining("The resolved multitenancy is invalid or disabled");
    }

    private static final class StubTenantDetailsService implements TenantDetailsService {

        private final TenantDetails tenantDetails;

        private StubTenantDetailsService(TenantDetails tenantDetails) {
            this.tenantDetails = tenantDetails;
        }

        @Override
        public java.util.List<? extends TenantDetails> loadAllTenants() {
            throw new UnsupportedOperationException("Not needed for this test");
        }

        @Override
        public TenantDetails loadTenantByIdentifier(String tenantIdentifier) {
            return tenantDetails;
        }
    }
}
