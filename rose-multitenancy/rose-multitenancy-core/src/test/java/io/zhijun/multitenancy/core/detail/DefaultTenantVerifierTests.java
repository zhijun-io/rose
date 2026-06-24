package io.zhijun.multitenancy.core.detail;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.zhijun.multitenancy.core.exception.TenantVerificationException;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link DefaultTenantVerifier}.
 */
class DefaultTenantVerifierTests {

    @Test
    void whenNullTenantDetailsServiceThenThrow() {
        assertThatThrownBy(() -> new DefaultTenantVerifier(null)).isInstanceOf(NullPointerException.class)
            .hasMessageContaining("tenantDetailsService cannot be null");
    }

    @Test
    void whenNullTenantIdentifierThenThrow() {
        TenantDetailsService service = Mockito.mock(TenantDetailsService.class);
        DefaultTenantVerifier verifier = new DefaultTenantVerifier(service);

        assertThatThrownBy(() -> verifier.verify(null)).isInstanceOf(TenantVerificationException.class)
            .hasMessageContaining("The multitenancy identifier must contain only alphanumeric characters, dashes (-), and underscores (_)");
    }

    @Test
    void whenTenantIdentifierContainsInvalidCharactersThenThrow() {
        TenantDetailsService service = Mockito.mock(TenantDetailsService.class);
        DefaultTenantVerifier verifier = new DefaultTenantVerifier(service);

        assertThatThrownBy(() -> verifier.verify("acme\nmalicious")).isInstanceOf(TenantVerificationException.class)
            .hasMessageContaining("The multitenancy identifier must contain only alphanumeric characters, dashes (-), and underscores (_)");
    }

    @Test
    void whenTenantIdentifierContainsAlphanumericDashUnderscoreThenPass() {
        Tenant tenant = Tenant.builder().identifier("acme-corp_2").enabled(true).build();
        TenantDetailsService service = Mockito.mock(TenantDetailsService.class);
        when(service.loadTenantByIdentifier("acme-corp_2")).thenReturn(tenant);
        DefaultTenantVerifier verifier = new DefaultTenantVerifier(service);

        assertThatNoException().isThrownBy(() -> verifier.verify("acme-corp_2"));
    }

    @Test
    void whenTenantExistsAndEnabledThenPass() {
        Tenant tenant = Tenant.builder().identifier("acme").enabled(true).build();
        TenantDetailsService service = Mockito.mock(TenantDetailsService.class);
        when(service.loadTenantByIdentifier("acme")).thenReturn(tenant);
        DefaultTenantVerifier verifier = new DefaultTenantVerifier(service);

        assertThatNoException().isThrownBy(() -> verifier.verify("acme"));
    }

    @Test
    void whenTenantExistsButDisabledThenThrow() {
        Tenant tenant = Tenant.builder().identifier("acme").enabled(false).build();
        TenantDetailsService service = Mockito.mock(TenantDetailsService.class);
        when(service.loadTenantByIdentifier("acme")).thenReturn(tenant);
        DefaultTenantVerifier verifier = new DefaultTenantVerifier(service);

        assertThatThrownBy(() -> verifier.verify("acme")).isInstanceOf(TenantVerificationException.class)
            .hasMessageContaining("The resolved multitenancy is invalid or disabled");
    }

    @Test
    void whenTenantNotFoundThenThrow() {
        TenantDetailsService service = Mockito.mock(TenantDetailsService.class);
        when(service.loadTenantByIdentifier("unknown")).thenReturn(null);
        DefaultTenantVerifier verifier = new DefaultTenantVerifier(service);

        assertThatThrownBy(() -> verifier.verify("unknown")).isInstanceOf(TenantVerificationException.class)
            .hasMessageContaining("The resolved multitenancy is invalid or disabled");
    }

}
