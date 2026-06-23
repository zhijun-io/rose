package io.zhijun.devservice.core.registration;

import java.util.concurrent.atomic.AtomicReference;

import io.zhijun.devservice.core.registration.DevServiceDynamicPropertySource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.env.MockEnvironment;

import io.zhijun.devservice.bootstrap.BootstrapMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Unit test for {@link DevServiceDynamicPropertySource}.
 */
class DevServiceDynamicPropertySourceTests {

    @BeforeEach
    void setUp() {
        BootstrapMode.clear();
    }

    @Test
    void addAndResolveProperty() {
        MockEnvironment environment = new MockEnvironment();
        DevServiceDynamicPropertySource propertySource = DevServiceDynamicPropertySource.getOrCreate(environment);

        propertySource.add("my.property", () -> "my-value");

        assertThat(environment.getProperty("my.property")).isEqualTo("my-value");
    }

    @Test
    void supplierIsResolvedLazily() {
        MockEnvironment environment = new MockEnvironment();
        DevServiceDynamicPropertySource propertySource = DevServiceDynamicPropertySource.getOrCreate(environment);

        int[] callCount = {0};
        propertySource.add("lazy.property", () -> {
            callCount[0]++;
            return "resolved";
        });

        assertThat(callCount[0]).isZero();
        assertThat(environment.getProperty("lazy.property")).isEqualTo("resolved");
        assertThat(callCount[0]).isEqualTo(1);
    }

    @Test
    void supplierIsInvokedOnEachAccess() {
        MockEnvironment environment = new MockEnvironment();
        DevServiceDynamicPropertySource propertySource = DevServiceDynamicPropertySource.getOrCreate(environment);

        AtomicReference<String> value = new AtomicReference<>("initial");
        propertySource.add("dynamic.property", value::get);

        assertThat(environment.getProperty("dynamic.property")).isEqualTo("initial");

        value.set("updated");
        assertThat(environment.getProperty("dynamic.property")).isEqualTo("updated");
    }

    @Test
    void addOverridesExistingProperty() {
        MockEnvironment environment = new MockEnvironment();
        DevServiceDynamicPropertySource propertySource = DevServiceDynamicPropertySource.getOrCreate(environment);

        propertySource.add("my.property", () -> "original");
        assertThat(environment.getProperty("my.property")).isEqualTo("original");

        propertySource.add("my.property", () -> "overridden");
        assertThat(environment.getProperty("my.property")).isEqualTo("overridden");
    }

    @Test
    void getOrCreateReturnsSameInstance() {
        MockEnvironment environment = new MockEnvironment();

        DevServiceDynamicPropertySource first = DevServiceDynamicPropertySource.getOrCreate(environment);
        DevServiceDynamicPropertySource second = DevServiceDynamicPropertySource.getOrCreate(environment);

        assertThat(first).isSameAs(second);
    }

    @Test
    void getOrCreateCreatesDistinctInstancesPerEnvironment() {
        MockEnvironment environment1 = new MockEnvironment();
        MockEnvironment environment2 = new MockEnvironment();

        DevServiceDynamicPropertySource first = DevServiceDynamicPropertySource.getOrCreate(environment1);
        DevServiceDynamicPropertySource second = DevServiceDynamicPropertySource.getOrCreate(environment2);

        assertThat(first).isNotSameAs(second);
    }

    @Test
    void multipleRegistrarsShareSamePropertySource() {
        MockEnvironment environment = new MockEnvironment();

        DevServiceDynamicPropertySource propertySource = DevServiceDynamicPropertySource.getOrCreate(environment);
        propertySource.add("first.property", () -> "first-value");

        DevServiceDynamicPropertySource samePropertySource = DevServiceDynamicPropertySource.getOrCreate(environment);
        samePropertySource.add("second.property", () -> "second-value");

        assertThat(environment.getProperty("first.property")).isEqualTo("first-value");
        assertThat(environment.getProperty("second.property")).isEqualTo("second-value");
    }

    @Test
    void unknownPropertyReturnsNull() {
        MockEnvironment environment = new MockEnvironment();
        DevServiceDynamicPropertySource propertySource = DevServiceDynamicPropertySource.getOrCreate(environment);

        assertThat(propertySource.getProperty("unknown.property")).isNull();
    }

    @Test
    void containsPropertyReturnsTrueForRegisteredProperty() {
        MockEnvironment environment = new MockEnvironment();
        DevServiceDynamicPropertySource propertySource = DevServiceDynamicPropertySource.getOrCreate(environment);

        propertySource.add("my.property", () -> "value");

        assertThat(propertySource.containsProperty("my.property")).isTrue();
        assertThat(propertySource.containsProperty("unknown.property")).isFalse();
    }

    @Test
    void getPropertyNamesReturnsRegisteredNames() {
        MockEnvironment environment = new MockEnvironment();
        DevServiceDynamicPropertySource propertySource = DevServiceDynamicPropertySource.getOrCreate(environment);

        propertySource.add("first.property", () -> "value1");
        propertySource.add("second.property", () -> "value2");

        assertThat(propertySource.getPropertyNames()).containsExactly("first.property", "second.property");
    }

    @Test
    void addWithNullNameThrows() {
        MockEnvironment environment = new MockEnvironment();
        DevServiceDynamicPropertySource propertySource = DevServiceDynamicPropertySource.getOrCreate(environment);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> propertySource.add(null, () -> "value"));
    }

    @Test
    void addWithBlankNameThrows() {
        MockEnvironment environment = new MockEnvironment();
        DevServiceDynamicPropertySource propertySource = DevServiceDynamicPropertySource.getOrCreate(environment);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> propertySource.add("", () -> "value"));
    }

    @Test
    void addWithNullSupplierThrows() {
        MockEnvironment environment = new MockEnvironment();
        DevServiceDynamicPropertySource propertySource = DevServiceDynamicPropertySource.getOrCreate(environment);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> propertySource.add("my.property", null));
    }

    @Test
    void getOrCreateThrowsWhenConflictingPropertySourceExists() {
        MockEnvironment environment = new MockEnvironment();
        environment.getPropertySources().addFirst(
                new MapPropertySource(DevServiceDynamicPropertySource.PROPERTY_SOURCE_NAME, new java.util.HashMap<String, Object>()));

        assertThatIllegalStateException()
                .isThrownBy(() -> DevServiceDynamicPropertySource.getOrCreate(environment));
    }

    @Test
    void propertySourceHasHighestPrecedence() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("my.property", "from-environment");

        DevServiceDynamicPropertySource propertySource = DevServiceDynamicPropertySource.getOrCreate(environment);
        propertySource.add("my.property", () -> "from-dev-service");

        assertThat(environment.getProperty("my.property")).isEqualTo("from-dev-service");
    }

}
