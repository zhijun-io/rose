package io.zhijun.local.api.registration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalServiceRegistrationTests {

  private final java.util.function.Supplier<ContainerInfo> containerInfoSupplier =
          new java.util.function.Supplier<ContainerInfo>() {
              @Override
              public ContainerInfo get() {
                  return ContainerInfo.builder()
                          .id("id")
                          .imageName("img")
                          .status("running")
                          .build();
              }
          };

    @Test
    void rejectsNullName() {
        assertThatThrownBy(() -> new LocalServiceRegistration(null, "desc", containerInfoSupplier))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsEmptyName() {
        assertThatThrownBy(() -> new LocalServiceRegistration("", "desc", containerInfoSupplier))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullContainerInfoSupplier() {
        assertThatThrownBy(() -> new LocalServiceRegistration("postgres", "desc", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void exposesRegistrationFields() {
        LocalServiceRegistration registration =
                new LocalServiceRegistration("postgres", "PostgreSQL", containerInfoSupplier);

        assertThat(registration.getName()).isEqualTo("postgres");
        assertThat(registration.getDescription()).isEqualTo("PostgreSQL");
        assertThat(registration.getContainerInfo().get().getImageName()).isEqualTo("img");
    }

    @Test
    void allowsNullDescription() {
        LocalServiceRegistration registration = new LocalServiceRegistration("postgres", null, containerInfoSupplier);
        assertThat(registration.getDescription()).isNull();
    }
}
