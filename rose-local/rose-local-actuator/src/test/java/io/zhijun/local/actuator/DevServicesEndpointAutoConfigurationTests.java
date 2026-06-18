package io.zhijun.local.actuator;

import java.util.Map;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zhijun.local.actuator.autoconfigure.DevServicesEndpointAutoConfiguration;
import io.zhijun.local.api.registration.ContainerInfo;
import io.zhijun.local.api.registration.LocalServiceRegistration;
import io.zhijun.local.bootstrap.BootstrapMode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DevServicesEndpointAutoConfiguration}.
 */
class DevServicesEndpointAutoConfigurationTests {

	private static final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(DevServicesEndpointAutoConfiguration.class));

	@BeforeEach
	void setUp() {
		BootstrapMode.clear();
	}

	@Test
	void endpointBeanIsAvailableWhenDevModeIsActive() {
		contextRunner
				.withSystemProperties("rose.bootstrap.mode=dev")
				.run(context -> {
					assertThat(context).hasSingleBean(DevServicesEndpoint.class);
				});
	}

	@Test
	void endpointBeanIsNotAvailableWhenTestModeIsActive() {
		contextRunner
				.withSystemProperties("rose.bootstrap.mode=test")
				.run(context -> {
					assertThat(context).doesNotHaveBean(DevServicesEndpoint.class);
				});
	}

	@Test
	void endpointBeanIsNotAvailableWhenProdModeIsActive() {
		contextRunner
				.withSystemProperties("rose.bootstrap.mode=prod")
				.run(context -> {
					assertThat(context).doesNotHaveBean(DevServicesEndpoint.class);
				});
	}

	@Test
	void endpointBeanUsesCustomBeanWhenProvided() {
		contextRunner
				.withSystemProperties("rose.bootstrap.mode=dev")
				.withUserConfiguration(CustomEndpointConfiguration.class)
				.run(context -> {
					assertThat(context).hasSingleBean(DevServicesEndpoint.class);
					assertThat(context.getBean(DevServicesEndpoint.class))
							.isSameAs(CustomEndpointConfiguration.customEndpoint);
				});
	}

	@Test
	void endpointBeanIsCreatedWithRegistrations() {
		contextRunner
				.withSystemProperties("rose.bootstrap.mode=dev")
				.withUserConfiguration(RegistrationsConfiguration.class)
				.run(context -> {
					assertThat(context).hasSingleBean(DevServicesEndpoint.class);
					DevServicesEndpoint endpoint = context.getBean(DevServicesEndpoint.class);
					assertThat(endpoint.devServices()).hasSize(2);
					assertThat(endpoint.devServices()).containsKeys("postgresql", "docling");
				});
	}

	@Test
	void endpointBeanIsCreatedWithNoRegistrations() {
		contextRunner
				.withSystemProperties("rose.bootstrap.mode=dev")
				.run(context -> {
					assertThat(context).hasSingleBean(DevServicesEndpoint.class);
					DevServicesEndpoint endpoint = context.getBean(DevServicesEndpoint.class);
					assertThat(endpoint.devServices()).isEmpty();
				});
	}

	@Configuration(proxyBeanMethods = false)
	static class CustomEndpointConfiguration {

		static final DevServicesEndpoint customEndpoint = new DevServicesEndpoint(new java.util.HashMap<String, LocalServiceRegistration>());

		@Bean
		DevServicesEndpoint devServicesEndpoint() {
			return customEndpoint;
		}

	}

	@Configuration(proxyBeanMethods = false)
	static class RegistrationsConfiguration {

		@Bean
        LocalServiceRegistration postgresqlRegistration() {
			return new LocalServiceRegistration(
					"postgresql",
					"PostgreSQL Database",
					mockContainerInfo("postgres:18", "1234")
			);
		}

		@Bean
        LocalServiceRegistration doclingRegistration() {
			return new LocalServiceRegistration(
					"docling",
					"Docling Serve",
					mockContainerInfo("docling:1.10", "5678")
			);
		}

		private Supplier<ContainerInfo> mockContainerInfo(String imageName, String containerId) {
			return new Supplier<ContainerInfo>() {
				@Override
				public ContainerInfo get() {
					return new ContainerInfo(
							containerId,
							imageName,
							java.util.Arrays.asList("/" + imageName.split(":")[0]),
							java.util.Arrays.asList(new ContainerInfo.ContainerPort("0.0.0.0", 5432, 5432, "tcp")),
							createSingleMap("test", "label"),
							"running");
				}
			};
		}

	}

	private static Map<String, String> createSingleMap(String k, String v) {
        Map<String, String> m = new java.util.HashMap<String, String>();
        m.put(k, v);
        return m;
    }

}
