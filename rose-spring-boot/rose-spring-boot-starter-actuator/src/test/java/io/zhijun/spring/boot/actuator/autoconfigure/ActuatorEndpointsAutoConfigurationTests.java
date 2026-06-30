/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zhijun.spring.boot.actuator.autoconfigure;

import io.zhijun.spring.boot.actuator.endpoint.ArtifactsEndpoint;
import io.zhijun.spring.boot.actuator.endpoint.WebEndpoints;
import io.zhijun.spring.boot.properties.metadata.ConfigurationMetadataReader;
import io.zhijun.spring.boot.properties.metadata.ConfigurationMetadataRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ActuatorEndpointsAutoConfiguration} 自动装配单元测试。
 * <p>
 * 验证该自动配置等价于 microsphere-spring-boot-actuator 的
 * {@code io.microsphere.spring.boot.actuate.autoconfigure.ActuatorEndpointsAutoConfiguration}。
 */
class ActuatorEndpointsAutoConfigurationTests {

    private static final String AUTO_CONFIGURATION_NAME =
            ActuatorEndpointsAutoConfiguration.class.getName();

    // ====== 文件注册验证 ======

    @Test
    void shouldRegisterAutoConfigurationInImportsFile() throws IOException {
        assertThat(resource("META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"))
                .contains(AUTO_CONFIGURATION_NAME);
    }

    @Test
    void shouldRegisterAutoConfigurationInSpringFactories() throws IOException {
        assertThat(resource("META-INF/spring.factories"))
                .contains(AUTO_CONFIGURATION_NAME);
    }

    // ====== Endpoint Bean 注册验证（Servlet Web 环境，端点暴露） ======

    @Test
    void shouldRegisterArtifactsEndpoint() {
        webContextRunner()
                .withPropertyValues("management.endpoints.web.exposure.include=artifacts")
                .run(context ->
                        assertThat(context).hasSingleBean(ArtifactsEndpoint.class));
    }

    @Test
    void shouldNotRegisterArtifactsEndpointWhenNotExposed() {
        webContextRunner()
                .withPropertyValues("management.endpoints.web.exposure.include=")
                .run(context ->
                        assertThat(context).doesNotHaveBean(ArtifactsEndpoint.class));
    }

    @Test
    void shouldRegisterWebEndpoints() {
        webContextRunner()
                .withPropertyValues("management.endpoints.web.exposure.include=webEndpoints")
                .run(context ->
                        assertThat(context).hasSingleBean(WebEndpoints.class));
    }

    // ====== 非 Web 环境回退 (WebEndpoints) ======

    @Test
    void shouldBackOffWebEndpointsInNonWebApplication() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        ActuatorEndpointsAutoConfiguration.class,
                        TestWebEndpointConfiguration.class))
                .withPropertyValues("management.endpoints.web.exposure.include=*")
                .run(context ->
                        assertThat(context).doesNotHaveBean(WebEndpoints.class));
    }

    // ====== 配置处理器 Beans ======

    @Test
    void shouldRegisterConfigurationMetadataBeansWhenProcessorPresent() {
        // 当 spring-boot-configuration-processor 在 classpath 上时，
        // ConfigurationProcessorConfiguration 应创建 Metadata beans
        webContextRunner()
                .withPropertyValues("management.endpoints.web.exposure.include=*")
                .run(context -> {
                    assertThat(context).hasSingleBean(ConfigurationMetadataReader.class);
                    assertThat(context).hasSingleBean(ConfigurationMetadataRepository.class);
                });
    }

    // ====== 工具方法 ======

    private String resource(String name) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(name);
        assertThat(inputStream).as(name).isNotNull();
        try {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } finally {
            inputStream.close();
        }
    }

    private WebApplicationContextRunner webContextRunner() {
        return new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        ActuatorEndpointsAutoConfiguration.class,
                        TestWebEndpointConfiguration.class));
    }

    /**
     * Provides bean dependencies required by {@link ActuatorEndpointsAutoConfiguration}
     * that are normally supplied by Spring Boot Actuator's own auto-configuration.
     */
    @Configuration(proxyBeanMethods = false)
    static class TestWebEndpointConfiguration {

        @Bean
        WebEndpointsSupplier webEndpointsSupplier() {
            return Collections::emptyList;
        }
    }
}
