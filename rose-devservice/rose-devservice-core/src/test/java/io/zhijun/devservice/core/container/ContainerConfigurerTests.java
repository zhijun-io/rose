package io.zhijun.devservice.core.container;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.WaitStrategy;


import io.zhijun.devservice.core.api.config.BaseDevServiceProperties;
import io.zhijun.devservice.core.api.config.JdbcDevServiceProperties;
import io.zhijun.devservice.core.api.config.ResourceMapping;
import io.zhijun.devservice.core.api.config.VolumeMapping;

/**
 * Unit test for {@link ContainerConfigurer}.
 */

class ContainerConfigurerTests {

    private static Map<String, String> mapOf(String k1, String v1) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(k1, v1);
        return map;
    }

    private static Map<String, String> mapOf(String k1, String v1, String k2, String v2) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

    @Test
    void baseConfigurationShouldApplyEnvironmentVariables() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseDevServiceProperties properties =
                new TestBaseDevServiceProperties().withEnvironment(mapOf("KEY1", "VALUE1", "KEY2", "VALUE2"));

        ContainerConfigurer.base(container, properties);

        assertThat(container.getEnvMap()).containsEntry("KEY1", "VALUE1").containsEntry("KEY2", "VALUE2");
    }

    @Test
    void baseConfigurationShouldApplyNetworkAliases() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseDevServiceProperties properties =
                new TestBaseDevServiceProperties().withNetworkAliases(Arrays.asList("alias1", "alias2", "alias3"));

        ContainerConfigurer.base(container, properties);

        assertThat(container.getNetworkAliases()).contains("alias1", "alias2", "alias3");
    }

    @Test
    void baseConfigurationShouldApplyStartupTimeout() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        Duration customTimeout = Duration.ofMinutes(2);
        BaseDevServiceProperties properties = new TestBaseDevServiceProperties().withStartupTimeout(customTimeout);

        ContainerConfigurer.base(container, properties);

        WaitStrategy waitStrategy = getWaitStrategy(container);
        Duration actualTimeout = getStartupTimeout(waitStrategy);

        assertThat(actualTimeout).isEqualTo(customTimeout);
    }

    /**
     * Helper method to extract the WaitStrategy from a GenericContainer using reflection.
     */
    private WaitStrategy getWaitStrategy(GenericContainer<?> container) {
        return readField(GenericContainer.class, container, "waitStrategy", WaitStrategy.class);
    }

    /**
     * Helper method to extract the startup timeout from a WaitStrategy using reflection.
     */
    private Duration getStartupTimeout(WaitStrategy waitStrategy) {
        return readField(waitStrategy.getClass(), waitStrategy, "startupTimeout", Duration.class);
    }

    private static <T> T readField(Class<?> type, Object target, String fieldName, Class<T> fieldType) {
        try {
            Field field = findDeclaredField(type, fieldName);
            field.setAccessible(true);
            return fieldType.cast(field.get(target));
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("Failed to read field " + fieldName + " from " + type.getName(), ex);
        }
    }

    private static Field findDeclaredField(Class<?> type, String fieldName) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ex) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    @Test
    void baseConfigurationShouldApplyEmptyEnvironmentVariables() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseDevServiceProperties properties =
                new TestBaseDevServiceProperties().withEnvironment(Collections.emptyMap());

        ContainerConfigurer.base(container, properties);

        assertThat(container.getEnvMap()).isEmpty();
    }

    @Test
    void baseConfigurationShouldApplyEmptyNetworkAliases() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseDevServiceProperties properties =
                new TestBaseDevServiceProperties().withNetworkAliases(Collections.emptyList());

        ContainerConfigurer.base(container, properties);

        assertThat(container.getNetworkAliases()).hasSize(1); // default alias added by Testcontainers
    }

    @Test
    void resourcesConfigurationShouldCopyClasspathResourceWithExplicitPrefix() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseDevServiceProperties properties = new TestBaseDevServiceProperties()
                .withResources(
                    Collections.singletonList(new ResourceMapping("classpath:test-resource.txt", "/etc/config/test.txt")));

        ContainerConfigurer.resources(container, properties);

        // Then
        assertThat(container.getCopyToFileContainerPathMap()).isNotEmpty();
        assertThat(container.getCopyToFileContainerPathMap().values()).contains("/etc/config/test.txt");
    }

    @Test
    void resourcesConfigurationShouldCopyClasspathResourceWithoutPrefix() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseDevServiceProperties properties = new TestBaseDevServiceProperties()
                .withResources(Collections.singletonList(new ResourceMapping("test-resource.txt", "/etc/config/test.txt")));

        ContainerConfigurer.resources(container, properties);

        assertThat(container.getCopyToFileContainerPathMap()).isNotEmpty();
        assertThat(container.getCopyToFileContainerPathMap().values()).contains("/etc/config/test.txt");
    }

    @Test
    void resourcesConfigurationShouldCopyMultipleResources() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseDevServiceProperties properties = new TestBaseDevServiceProperties()
                .withResources(Arrays.asList(
                        new ResourceMapping("classpath:test-resource.txt", "/etc/config/test1.txt"),
                        new ResourceMapping("test-resource.txt", "/etc/config/test2.txt")));

        ContainerConfigurer.resources(container, properties);

        assertThat(container.getCopyToFileContainerPathMap()).hasSize(2);
        assertThat(container.getCopyToFileContainerPathMap().values())
                .contains("/etc/config/test1.txt", "/etc/config/test2.txt");
    }

    @Test
    void resourcesConfigurationShouldThrowExceptionWhenSourcePathIsNull() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseDevServiceProperties properties = new TestBaseDevServiceProperties()
                .withResources(Collections.singletonList(new ResourceMapping(null, "/etc/config/test.txt")));

        assertThatThrownBy(() -> ContainerConfigurer.resources(container, properties))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("source path");
    }

    @Test
    void resourcesConfigurationShouldThrowExceptionWhenSourcePathIsEmpty() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseDevServiceProperties properties = new TestBaseDevServiceProperties()
                .withResources(Collections.singletonList(new ResourceMapping("", "/etc/config/test.txt")));

        assertThatThrownBy(() -> ContainerConfigurer.resources(container, properties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("source path");
    }

    @Test
    void resourcesConfigurationShouldThrowExceptionWhenContainerPathIsNull() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseDevServiceProperties properties = new TestBaseDevServiceProperties()
                .withResources(Collections.singletonList(new ResourceMapping("test-resource.txt", null)));

        assertThatThrownBy(() -> ContainerConfigurer.resources(container, properties))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("container path");
    }

    @Test
    void resourcesConfigurationShouldThrowExceptionWhenContainerPathIsEmpty() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseDevServiceProperties properties = new TestBaseDevServiceProperties()
                .withResources(Collections.singletonList(new ResourceMapping("test-resource.txt", "")));

        assertThatThrownBy(() -> ContainerConfigurer.resources(container, properties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("container path");
    }

    @Test
    void resourcesConfigurationShouldThrowExceptionWhenResourceNotFound() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseDevServiceProperties properties = new TestBaseDevServiceProperties()
                .withResources(Collections.singletonList(new ResourceMapping("non-existent-resource.txt", "/etc/config/test.txt")));

        assertThatThrownBy(() -> ContainerConfigurer.resources(container, properties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Resource not found");
    }

    @Test
    void resourcesConfigurationShouldHandleEmptyResourcesList() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseDevServiceProperties properties = new TestBaseDevServiceProperties().withResources(Collections.emptyList());

        ContainerConfigurer.resources(container, properties);

        assertThat(container.getCopyToFileContainerPathMap()).isEmpty();
    }

    @Test
    void volumesConfigurationShouldBindSingleVolume() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseDevServiceProperties properties = new TestBaseDevServiceProperties()
                .withVolumes(Collections.singletonList(new VolumeMapping("/host/path", "/container/path")));

        ContainerConfigurer.volumes(container, properties);

        assertThat(container.getBinds()).hasSize(1);
        assertThat(container.getBinds().get(0).getPath()).isEqualTo("/host/path");
        assertThat(container.getBinds().get(0).getVolume().getPath()).isEqualTo("/container/path");
        assertThat(container.getBinds().get(0).getAccessMode().toString()).isEqualTo("rw");
    }

    @Test
    void volumesConfigurationShouldBindMultipleVolumes() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseDevServiceProperties properties = new TestBaseDevServiceProperties()
                .withVolumes(Arrays.asList(
                        new VolumeMapping("/host/path1", "/container/path1"),
                        new VolumeMapping("/host/path2", "/container/path2"),
                        new VolumeMapping("/host/path3", "/container/path3")));

        ContainerConfigurer.volumes(container, properties);

        assertThat(container.getBinds()).hasSize(3);
        assertThat(container.getBinds().get(0).getPath()).isEqualTo("/host/path1");
        assertThat(container.getBinds().get(0).getVolume().getPath()).isEqualTo("/container/path1");
        assertThat(container.getBinds().get(0).getAccessMode().toString()).isEqualTo("rw");
        assertThat(container.getBinds().get(1).getPath()).isEqualTo("/host/path2");
        assertThat(container.getBinds().get(1).getVolume().getPath()).isEqualTo("/container/path2");
        assertThat(container.getBinds().get(1).getAccessMode().toString()).isEqualTo("rw");
        assertThat(container.getBinds().get(2).getPath()).isEqualTo("/host/path3");
        assertThat(container.getBinds().get(2).getVolume().getPath()).isEqualTo("/container/path3");
        assertThat(container.getBinds().get(2).getAccessMode().toString()).isEqualTo("rw");
    }

    @Test
    void volumesConfigurationShouldHandleEmptyVolumesList() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseDevServiceProperties properties = new TestBaseDevServiceProperties().withVolumes(Collections.emptyList());

        ContainerConfigurer.volumes(container, properties);

        assertThat(container.getBinds()).isEmpty();
    }

    @Test
    void jdbcConfigurationShouldApplyUsername() {
        JdbcDatabaseContainer<?> container = new PostgreSQLContainer<>("postgres:latest");
        JdbcDevServiceProperties properties = new TestJdbcDevServiceProperties().withUsername("testuser");

        ContainerConfigurer.jdbc(container, properties);

        assertThat(container.getUsername()).isEqualTo("testuser");
    }

    @Test
    void jdbcConfigurationShouldApplyPassword() {
        JdbcDatabaseContainer<?> container = new PostgreSQLContainer<>("postgres:latest");
        JdbcDevServiceProperties properties = new TestJdbcDevServiceProperties().withPassword("testpassword");

        ContainerConfigurer.jdbc(container, properties);

        assertThat(container.getPassword()).isEqualTo("testpassword");
    }

    @Test
    void jdbcConfigurationShouldApplyDatabaseName() {
        JdbcDatabaseContainer<?> container = new PostgreSQLContainer<>("postgres:latest");
        JdbcDevServiceProperties properties = new TestJdbcDevServiceProperties().withDbName("testdb");

        ContainerConfigurer.jdbc(container, properties);

        assertThat(container.getDatabaseName()).isEqualTo("testdb");
    }

    @Test
    void jdbcConfigurationShouldApplyInitScripts() {
        JdbcDatabaseContainer<?> container = new PostgreSQLContainer<>("postgres:latest");
        JdbcDevServiceProperties properties =
                new TestJdbcDevServiceProperties().withInitScriptPaths(Arrays.asList("init1.sql", "init2.sql"));

        ContainerConfigurer.jdbc(container, properties);

        String[] initScripts = getInitScripts(container);
        assertThat(initScripts).containsExactly("init1.sql", "init2.sql");
    }

    @Test
    void jdbcConfigurationShouldHandleEmptyInitScripts() {
        JdbcDatabaseContainer<?> container = new PostgreSQLContainer<>("postgres:latest");
        JdbcDevServiceProperties properties =
                new TestJdbcDevServiceProperties().withInitScriptPaths(Collections.emptyList());

        assertThatCode(() -> ContainerConfigurer.jdbc(container, properties)).doesNotThrowAnyException();

        String[] initScripts = getInitScripts(container);
        assertThat(initScripts).isEmpty();
    }

    /**
     * Helper method to extract the init scripts from a JdbcDatabaseContainer using reflection.
     */
    private String[] getInitScripts(JdbcDatabaseContainer<?> container) {
        try {
            Field initScriptsField = JdbcDatabaseContainer.class.getDeclaredField("initScriptPaths");
            initScriptsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<String> scripts = (List<String>) initScriptsField.get(container);
            return scripts != null ? scripts.toArray(new String[0]) : new String[0];
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("Failed to read initScriptPaths from JdbcDatabaseContainer", ex);
        }
    }

    private static class TestBaseDevServiceProperties extends BaseDevServiceProperties {

        TestBaseDevServiceProperties() {
            setImageName("test-image:latest");
        }

        public TestBaseDevServiceProperties withEnvironment(Map<String, String> environment) {
            setEnvironment(environment);
            return this;
        }

        public TestBaseDevServiceProperties withNetworkAliases(List<String> networkAliases) {
            setNetworkAliases(networkAliases);
            return this;
        }

        public TestBaseDevServiceProperties withStartupTimeout(Duration startupTimeout) {
            setStartupTimeout(startupTimeout);
            return this;
        }

        public TestBaseDevServiceProperties withResources(List<ResourceMapping> resources) {
            setResources(resources);
            return this;
        }

        public TestBaseDevServiceProperties withVolumes(List<VolumeMapping> volumes) {
            setVolumes(volumes);
            return this;
        }

        public TestBaseDevServiceProperties withShared(boolean shared) {
            setShared(shared);
            return this;
        }
    }

    private static class TestJdbcDevServiceProperties extends JdbcDevServiceProperties {

        TestJdbcDevServiceProperties() {
            setImageName("test-db:latest");
            setUsername("user");
            setPassword("password");
            setDbName("testdb");
        }

        public TestJdbcDevServiceProperties withUsername(String username) {
            setUsername(username);
            return this;
        }

        public TestJdbcDevServiceProperties withPassword(String password) {
            setPassword(password);
            return this;
        }

        public TestJdbcDevServiceProperties withDbName(String dbName) {
            setDbName(dbName);
            return this;
        }

        public TestJdbcDevServiceProperties withInitScriptPaths(List<String> initScriptPaths) {
            setInitScriptPaths(initScriptPaths);
            return this;
        }
    }
}
