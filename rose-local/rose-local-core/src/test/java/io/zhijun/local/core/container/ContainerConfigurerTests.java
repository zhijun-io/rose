package io.zhijun.local.core.container;

import java.util.Collections;
import java.util.Arrays;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

import io.zhijun.core.annotation.Incubating;
import io.zhijun.local.api.config.BaseLocalServiceProperties;
import io.zhijun.local.api.config.JdbcLocalServiceProperties;
import io.zhijun.local.api.config.ResourceMapping;
import io.zhijun.local.api.config.VolumeMapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link ContainerConfigurer}.
 */
@Incubating
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
        BaseLocalServiceProperties properties = new TestBaseLocalServiceProperties()
                .withEnvironment(mapOf("KEY1", "VALUE1", "KEY2", "VALUE2"));

        ContainerConfigurer.base(container, properties);

        assertThat(container.getEnvMap())
                .containsEntry("KEY1", "VALUE1")
                .containsEntry("KEY2", "VALUE2");
    }

    @Test
    void baseConfigurationShouldApplyNetworkAliases() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseLocalServiceProperties properties = new TestBaseLocalServiceProperties()
                .withNetworkAliases(Arrays.asList("alias1", "alias2", "alias3"));

        ContainerConfigurer.base(container, properties);

        assertThat(container.getNetworkAliases())
                .contains("alias1", "alias2", "alias3");
    }

    @Test
    void baseConfigurationShouldApplyStartupTimeout() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        Duration customTimeout = Duration.ofMinutes(2);
        BaseLocalServiceProperties properties = new TestBaseLocalServiceProperties()
                .withStartupTimeout(customTimeout);

        ContainerConfigurer.base(container, properties);

        WaitStrategy waitStrategy = getWaitStrategy(container);
        Duration actualTimeout = getStartupTimeout(waitStrategy);

        assertThat(actualTimeout).isEqualTo(customTimeout);
    }

    /**
     * Helper method to extract the WaitStrategy from a GenericContainer using reflection.
     */
    private WaitStrategy getWaitStrategy(GenericContainer<?> container) {
        Field waitStrategyField = ReflectionUtils.findField(GenericContainer.class, "waitStrategy");
        assertThat(waitStrategyField).isNotNull();
        ReflectionUtils.makeAccessible(waitStrategyField);
        return (WaitStrategy) ReflectionUtils.getField(waitStrategyField, container);
    }

    /**
     * Helper method to extract the startup timeout from a WaitStrategy using reflection.
     */
    private Duration getStartupTimeout(WaitStrategy waitStrategy) {
        Field startupTimeoutField = ReflectionUtils.findField(waitStrategy.getClass(), "startupTimeout");
        assertThat(startupTimeoutField).isNotNull();
        ReflectionUtils.makeAccessible(startupTimeoutField);
        return (Duration) ReflectionUtils.getField(startupTimeoutField, waitStrategy);
    }

    @Test
    void baseConfigurationShouldApplyEmptyEnvironmentVariables() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseLocalServiceProperties properties = new TestBaseLocalServiceProperties()
                .withEnvironment(Collections.emptyMap());

        ContainerConfigurer.base(container, properties);

        assertThat(container.getEnvMap()).isEmpty();
    }

    @Test
    void baseConfigurationShouldApplyEmptyNetworkAliases() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseLocalServiceProperties properties = new TestBaseLocalServiceProperties()
                .withNetworkAliases(Collections.emptyList());

        ContainerConfigurer.base(container, properties);

        assertThat(container.getNetworkAliases()).hasSize(1); // default alias added by Testcontainers
    }

    @Test
    void resourcesConfigurationShouldCopyClasspathResourceWithExplicitPrefix() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseLocalServiceProperties properties = new TestBaseLocalServiceProperties()
                .withResources(Arrays.asList(
                        new ResourceMapping("classpath:test-resource.txt", "/etc/config/test.txt")
                ));

        ContainerConfigurer.resources(container, properties);

        // Then
        assertThat(container.getCopyToFileContainerPathMap()).isNotEmpty();
        assertThat(container.getCopyToFileContainerPathMap().values())
                .contains("/etc/config/test.txt");
    }

    @Test
    void resourcesConfigurationShouldCopyClasspathResourceWithoutPrefix() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseLocalServiceProperties properties = new TestBaseLocalServiceProperties()
                .withResources(Arrays.asList(
                        new ResourceMapping("test-resource.txt", "/etc/config/test.txt")
                ));

        ContainerConfigurer.resources(container, properties);

        assertThat(container.getCopyToFileContainerPathMap()).isNotEmpty();
        assertThat(container.getCopyToFileContainerPathMap().values())
                .contains("/etc/config/test.txt");
    }

    @Test
    void resourcesConfigurationShouldCopyMultipleResources() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseLocalServiceProperties properties = new TestBaseLocalServiceProperties()
                .withResources(Arrays.asList(
                        new ResourceMapping("classpath:test-resource.txt", "/etc/config/test1.txt"),
                        new ResourceMapping("test-resource.txt", "/etc/config/test2.txt")
                ));

        ContainerConfigurer.resources(container, properties);

        assertThat(container.getCopyToFileContainerPathMap()).hasSize(2);
        assertThat(container.getCopyToFileContainerPathMap().values())
                .contains("/etc/config/test1.txt", "/etc/config/test2.txt");
    }

    @Test
    void resourcesConfigurationShouldThrowExceptionWhenSourcePathIsNull() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseLocalServiceProperties properties = new TestBaseLocalServiceProperties()
                .withResources(Arrays.asList(
                        new ResourceMapping(null, "/etc/config/test.txt")
                ));

        assertThatThrownBy(() -> ContainerConfigurer.resources(container, properties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("source path");
    }

    @Test
    void resourcesConfigurationShouldThrowExceptionWhenSourcePathIsEmpty() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseLocalServiceProperties properties = new TestBaseLocalServiceProperties()
                .withResources(Arrays.asList(
                        new ResourceMapping("", "/etc/config/test.txt")
                ));

        assertThatThrownBy(() -> ContainerConfigurer.resources(container, properties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("source path");
    }

    @Test
    void resourcesConfigurationShouldThrowExceptionWhenContainerPathIsNull() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseLocalServiceProperties properties = new TestBaseLocalServiceProperties()
                .withResources(Arrays.asList(
                        new ResourceMapping("test-resource.txt", null)
                ));

        assertThatThrownBy(() -> ContainerConfigurer.resources(container, properties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("container path");
    }

    @Test
    void resourcesConfigurationShouldThrowExceptionWhenContainerPathIsEmpty() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseLocalServiceProperties properties = new TestBaseLocalServiceProperties()
                .withResources(Arrays.asList(
                        new ResourceMapping("test-resource.txt", "")
                ));

        assertThatThrownBy(() -> ContainerConfigurer.resources(container, properties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("container path");
    }

    @Test
    void resourcesConfigurationShouldThrowExceptionWhenResourceNotFound() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseLocalServiceProperties properties = new TestBaseLocalServiceProperties()
                .withResources(Arrays.asList(
                        new ResourceMapping("non-existent-resource.txt", "/etc/config/test.txt")
                ));

        assertThatThrownBy(() -> ContainerConfigurer.resources(container, properties))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Resource not found");
    }

    @Test
    void resourcesConfigurationShouldHandleEmptyResourcesList() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseLocalServiceProperties properties = new TestBaseLocalServiceProperties()
                .withResources(Collections.emptyList());

        ContainerConfigurer.resources(container, properties);

        assertThat(container.getCopyToFileContainerPathMap()).isEmpty();
    }

    @Test
    void volumesConfigurationShouldBindSingleVolume() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseLocalServiceProperties properties = new TestBaseLocalServiceProperties()
                .withVolumes(Arrays.asList(
                        new VolumeMapping("/host/path", "/container/path")
                ));

        ContainerConfigurer.volumes(container, properties);

        assertThat(container.getBinds()).hasSize(1);
        assertThat(container.getBinds().get(0).getPath()).isEqualTo("/host/path");
        assertThat(container.getBinds().get(0).getVolume().getPath()).isEqualTo("/container/path");
        assertThat(container.getBinds().get(0).getAccessMode().toString()).isEqualTo("rw");
    }

    @Test
    void volumesConfigurationShouldBindMultipleVolumes() {
        GenericContainer<?> container = new GenericContainer<>("alpine:latest");
        BaseLocalServiceProperties properties = new TestBaseLocalServiceProperties()
                .withVolumes(Arrays.asList(
                        new VolumeMapping("/host/path1", "/container/path1"),
                        new VolumeMapping("/host/path2", "/container/path2"),
                        new VolumeMapping("/host/path3", "/container/path3")
                ));

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
        BaseLocalServiceProperties properties = new TestBaseLocalServiceProperties()
                .withVolumes(Collections.emptyList());

        ContainerConfigurer.volumes(container, properties);

        assertThat(container.getBinds()).isEmpty();
    }

    @Test
    void jdbcConfigurationShouldApplyUsername() {
        JdbcDatabaseContainer<?> container = new PostgreSQLContainer<>("postgres:latest");
        JdbcLocalServiceProperties properties = new TestJdbcLocalServiceProperties()
                .withUsername("testuser");

        ContainerConfigurer.jdbc(container, properties);

        assertThat(container.getUsername()).isEqualTo("testuser");
    }

    @Test
    void jdbcConfigurationShouldApplyPassword() {
        JdbcDatabaseContainer<?> container = new PostgreSQLContainer<>("postgres:latest");
        JdbcLocalServiceProperties properties = new TestJdbcLocalServiceProperties()
                .withPassword("testpassword");

        ContainerConfigurer.jdbc(container, properties);

        assertThat(container.getPassword()).isEqualTo("testpassword");
    }

    @Test
    void jdbcConfigurationShouldApplyDatabaseName() {
        JdbcDatabaseContainer<?> container = new PostgreSQLContainer<>("postgres:latest");
        JdbcLocalServiceProperties properties = new TestJdbcLocalServiceProperties()
                .withDbName("testdb");

        ContainerConfigurer.jdbc(container, properties);

        assertThat(container.getDatabaseName()).isEqualTo("testdb");
    }

    @Test
    void jdbcConfigurationShouldApplyInitScripts() {
        JdbcDatabaseContainer<?> container = new PostgreSQLContainer<>("postgres:latest");
        JdbcLocalServiceProperties properties = new TestJdbcLocalServiceProperties()
                .withInitScriptPaths(Arrays.asList("init1.sql", "init2.sql"));

        ContainerConfigurer.jdbc(container, properties);

        String[] initScripts = getInitScripts(container);
        assertThat(initScripts)
                .containsExactly("init1.sql", "init2.sql");
    }
    @Test
    void jdbcConfigurationShouldHandleEmptyInitScripts() {
        JdbcDatabaseContainer<?> container = new PostgreSQLContainer<>("postgres:latest");
        JdbcLocalServiceProperties properties = new TestJdbcLocalServiceProperties()
                .withInitScriptPaths(Collections.emptyList());

        assertThatCode(() -> ContainerConfigurer.jdbc(container, properties))
                .doesNotThrowAnyException();

        String[] initScripts = getInitScripts(container);
        assertThat(initScripts).isEmpty();
    }

    /**
     * Helper method to extract the init scripts from a JdbcDatabaseContainer using reflection.
     */
    private String[] getInitScripts(JdbcDatabaseContainer<?> container) {
        Field initScriptsField = ReflectionUtils.findField(JdbcDatabaseContainer.class, "initScriptPaths");
        assertThat(initScriptsField).isNotNull();
        ReflectionUtils.makeAccessible(initScriptsField);
        @SuppressWarnings("unchecked")
        List<String> scripts = (List<String>) ReflectionUtils.getField(initScriptsField, container);
        return scripts != null ? scripts.toArray(new String[0]) : new String[0];
    }

    private static class TestBaseLocalServiceProperties implements BaseLocalServiceProperties {
        private Map<String, String> environment = Collections.emptyMap();
        private List<String> networkAliases = Collections.emptyList();
        private Duration startupTimeout = Duration.ofSeconds(30);
        private List<ResourceMapping> resources = Collections.emptyList();
        private List<VolumeMapping> volumes = Collections.emptyList();
        private boolean shared = false;

        @Override
        public String getImageName() {
            return "test-image:latest";
        }

        @Override
        public void setImageName(String imageName) {

        }

        @Override
        public Map<String, String> getEnvironment() {
            return environment;
        }

        public TestBaseLocalServiceProperties withEnvironment(Map<String, String> environment) {
            this.environment = environment;
            return this;
        }

        @Override
        public List<String> getNetworkAliases() {
            return networkAliases;
        }

        public TestBaseLocalServiceProperties withNetworkAliases(List<String> networkAliases) {
            this.networkAliases = networkAliases;
            return this;
        }

        @Override
        public Duration getStartupTimeout() {
            return startupTimeout;
        }

        public TestBaseLocalServiceProperties withStartupTimeout(Duration startupTimeout) {
            this.startupTimeout = startupTimeout;
            return this;
        }

        @Override
        public List<ResourceMapping> getResources() {
            return resources;
        }

        public TestBaseLocalServiceProperties withResources(List<ResourceMapping> resources) {
            this.resources = resources;
            return this;
        }

        @Override
        public List<VolumeMapping> getVolumes() {
            return volumes;
        }

        public TestBaseLocalServiceProperties withVolumes(List<VolumeMapping> volumes) {
            this.volumes = volumes;
            return this;
        }

        @Override
        public boolean isShared() {
            return shared;
        }

        public TestBaseLocalServiceProperties withShared(boolean shared) {
            this.shared = shared;
            return this;
        }
    }

    private static class TestJdbcLocalServiceProperties implements JdbcLocalServiceProperties {
        private String username = "user";
        private String password = "password";
        private String dbName = "testdb";
        private List<String> initScriptPaths = Collections.emptyList();

        @Override
        public String getImageName() {
            return "test-db:latest";
        }

        @Override
        public void setImageName(String imageName) {}

        @Override
        public String getUsername() {
            return username;
        }

        public TestJdbcLocalServiceProperties withUsername(String username) {
            this.username = username;
            return this;
        }

        @Override
        public String getPassword() {
            return password;
        }

        public TestJdbcLocalServiceProperties withPassword(String password) {
            this.password = password;
            return this;
        }

        @Override
        public String getDbName() {
            return dbName;
        }

        public TestJdbcLocalServiceProperties withDbName(String dbName) {
            this.dbName = dbName;
            return this;
        }

        @Override
        public List<String> getInitScriptPaths() {
            return initScriptPaths;
        }

        public TestJdbcLocalServiceProperties withInitScriptPaths(List<String> initScriptPaths) {
            this.initScriptPaths = initScriptPaths;
            return this;
        }
    }

}
