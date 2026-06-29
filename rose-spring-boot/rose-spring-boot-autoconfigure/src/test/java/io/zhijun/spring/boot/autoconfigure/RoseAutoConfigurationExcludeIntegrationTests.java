package io.zhijun.spring.boot.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import io.zhijun.spring.boot.bootstrap.config.DefaultConfigEnvironmentPostProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.mock.env.MockEnvironment;

class RoseAutoConfigurationExcludeIntegrationTests {

    private static final String GSON_AUTO_CONFIGURATION =
            "org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration";

    private static final String WEB_MVC_AUTO_CONFIGURATION =
            "org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration";

    private static final AutoConfigurationMetadata METADATA = new AutoConfigurationMetadata() {
        @Override
        public boolean wasProcessed(String className) {
            return false;
        }

        @Override
        public Integer getInteger(String className, String key) {
            return null;
        }

        @Override
        public Integer getInteger(String className, String key, Integer defaultValue) {
            return defaultValue;
        }

        @Override
        public Set<String> getSet(String className, String key) {
            return null;
        }

        @Override
        public Set<String> getSet(String className, String key, Set<String> defaultValue) {
            return defaultValue;
        }

        @Override
        public String get(String className, String key) {
            return null;
        }

        @Override
        public String get(String className, String key, String defaultValue) {
            return defaultValue;
        }
    };

    @Test
    void shouldExcludeAutoConfigurationDeclaredInRoseDefaults() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(
                DefaultConfigEnvironmentPostProcessor.LOCATIONS_PROPERTY,
                "classpath*:config/default/integration-exclude.properties");
        new DefaultConfigEnvironmentPostProcessor()
                .postProcessEnvironment(environment, new SpringApplication());

        ConfigurableAutoConfigurationImportFilter filter = new ConfigurableAutoConfigurationImportFilter();
        filter.setEnvironment(environment);

        boolean[] results = filter.match(new String[] {GSON_AUTO_CONFIGURATION, WEB_MVC_AUTO_CONFIGURATION}, METADATA);

        assertThat(results).containsExactly(false, true);
    }
}
