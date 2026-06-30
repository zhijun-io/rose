package io.zhijun.spring.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfigurationPropertyRepositoryTests {

    private ConfigurationPropertyRepository repository;

    private MockEnvironment environment;

    @BeforeEach
    void setUp() {
        repository = new ConfigurationPropertyRepository();
        environment = new MockEnvironment();
        repository.setEnvironment(environment);
        repository.afterPropertiesSet();
    }

    @AfterEach
    void tearDown() throws Exception {
        repository.destroy();
    }

    @Test
    void shouldAddAndGetProperty() {
        ConfigurationProperty prop = new ConfigurationProperty("app.name", "test-app", null);
        repository.add(prop);

        assertThat(repository.get("app.name")).isSameAs(prop);
        assertThat(repository.contains("app.name")).isTrue();
    }

    @Test
    void shouldReturnNullForMissingProperty() {
        assertThat(repository.get("missing")).isNull();
        assertThat(repository.contains("missing")).isFalse();
    }

    @Test
    void shouldRemoveProperty() {
        ConfigurationProperty prop = new ConfigurationProperty("to.remove");
        repository.add(prop);
        assertThat(repository.contains("to.remove")).isTrue();

        ConfigurationProperty removed = repository.remove("to.remove");
        assertThat(removed).isSameAs(prop);
        assertThat(repository.contains("to.remove")).isFalse();
    }

    @Test
    void shouldCreateIfAbsent() {
        ConfigurationProperty prop = repository.createIfAbsent("dynamic.key");
        assertThat(prop).isNotNull();
        assertThat(prop.getName()).isEqualTo("dynamic.key");
        assertThat(repository.get("dynamic.key")).isSameAs(prop);
    }

    @Test
    void shouldReturnExistingOnCreateIfAbsent() {
        ConfigurationProperty original = new ConfigurationProperty("existing", "val", null);
        repository.add(original);

        ConfigurationProperty result = repository.createIfAbsent("existing");
        assertThat(result).isSameAs(original);
    }

    @Test
    void shouldGetAllProperties() {
        repository.add(new ConfigurationProperty("a"));
        repository.add(new ConfigurationProperty("b"));

        assertThat(repository.getAll()).hasSize(2);
    }

    @Test
    void shouldReturnEmptyCollectionForEmptyRepository() {
        assertThat(repository.getAll()).isEmpty();
    }

    @Test
    void shouldThrowWhenExceedingMaxSize() {
        environment.setProperty(ConfigurationPropertyRepository.MAX_SIZE_PROPERTY_NAME, "2");
        repository.setEnvironment(environment);
        repository.afterPropertiesSet();

        repository.add(new ConfigurationProperty("prop1"));
        repository.add(new ConfigurationProperty("prop2"));

        assertThatThrownBy(() -> repository.add(new ConfigurationProperty("prop3")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max size");
    }

    @Test
    void shouldSupportMaxSizeFromConfig() {
        environment.setProperty(ConfigurationPropertyRepository.MAX_SIZE_PROPERTY_NAME, "10");
        repository.setEnvironment(environment);
        repository.afterPropertiesSet();

        assertThat(repository.getMaxSize()).isEqualTo(10);
    }

    @Test
    void shouldUseDefaultMaxSize() {
        assertThat(repository.getMaxSize()).isEqualTo(ConfigurationPropertyRepository.DEFAULT_MAX_SIZE_PROPERTY_VALUE);
    }

    @Test
    void shouldDestroyClearsRepository() throws Exception {
        repository.add(new ConfigurationProperty("temp"));
        assertThat(repository.getAll()).isNotEmpty();

        repository.destroy();
        repository.afterPropertiesSet();
        assertThat(repository.getAll()).isEmpty();
    }

    @Test
    void shouldBeNamed() {
        assertThat(ConfigurationPropertyRepository.BEAN_NAME).isEqualTo("configurationPropertyRepository");
    }
}
