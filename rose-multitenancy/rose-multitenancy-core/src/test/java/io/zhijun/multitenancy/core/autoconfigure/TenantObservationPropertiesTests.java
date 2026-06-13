package io.zhijun.multitenancy.core.autoconfigure;

import org.junit.jupiter.api.Test;

import io.zhijun.multitenancy.core.observability.Cardinality;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link TenantObservationProperties}.
 */
class TenantObservationPropertiesTests {

  @Test
  void defaultsAndMutators() {
    TenantObservationProperties properties = new TenantObservationProperties();

    assertThat(properties.isEnabled()).isTrue();
    assertThat(properties.getKeyName()).isEqualTo("tenant.id");
    assertThat(properties.getCardinality()).isEqualTo(Cardinality.HIGH);

    properties.setEnabled(false);
    properties.setKeyName("tenant");
    properties.setCardinality(Cardinality.LOW);

    assertThat(properties.isEnabled()).isFalse();
    assertThat(properties.getKeyName()).isEqualTo("tenant");
    assertThat(properties.getCardinality()).isEqualTo(Cardinality.LOW);
  }

}
