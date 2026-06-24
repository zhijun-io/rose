package io.zhijun.multitenancy.autoconfigure;

import io.zhijun.multitenancy.autoconfigure.observability.TenantObservationProperties;

import org.junit.jupiter.api.Test;

import io.zhijun.multitenancy.observability.Cardinality;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link TenantObservationProperties}.
 */
class TenantObservationPropertiesTests {

  @Test
  void defaultsAndMutators() {
    TenantObservationProperties properties = new TenantObservationProperties();

    assertThat(properties.isEnabled()).isTrue();
    assertThat(properties.getKeyName()).isEqualTo("multitenancy.id");
    assertThat(properties.getCardinality()).isEqualTo(Cardinality.HIGH);

    properties.setEnabled(false);
    properties.setKeyName("multitenancy");
    properties.setCardinality(Cardinality.LOW);

    assertThat(properties.isEnabled()).isFalse();
    assertThat(properties.getKeyName()).isEqualTo("multitenancy");
    assertThat(properties.getCardinality()).isEqualTo(Cardinality.LOW);
  }

}
