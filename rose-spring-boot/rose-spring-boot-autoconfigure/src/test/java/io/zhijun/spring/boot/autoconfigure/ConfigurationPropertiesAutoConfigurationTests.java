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
package io.zhijun.spring.boot.autoconfigure;

import io.zhijun.spring.boot.properties.ListenableConfigurationPropertiesBindHandlerAdvisor;
import io.zhijun.spring.boot.properties.annotation.EnableConfigurationPropertiesExtension;
import io.zhijun.spring.boot.properties.bind.EventPublishingConfigurationPropertiesBeanPropertyChangedListener;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ConfigurationPropertiesAutoConfiguration} 自动装配单元测试。
 * <p>
 * 验证该自动配置等价于 microsphere-spring-boot-core 的
 * {@code io.microsphere.spring.boot.context.autoconfigure.ConfigurationPropertiesAutoConfiguration}。
 * <p>
 * 注意：auto-configuration 在 Spring Boot 2.7+ 中注册于
 * {@code AutoConfiguration.imports}，而非 {@code spring.factories}。
 * {@link #shouldRegisterBindHandlerAdvisor()} 等 Bean 注册测试使用
 * {@link EnableConfigurationPropertiesExtension} 直接验证注册逻辑，
 * 避免 {@code @ConditionalOnClass} 在 {@link ApplicationContextRunner} 中的评估差异。
 */
class ConfigurationPropertiesAutoConfigurationTests {

    private static final String AUTO_CONFIGURATION_NAME =
            ConfigurationPropertiesAutoConfiguration.class.getName();

    private static final String ADVISOR_BEAN_NAME =
            "listenableConfigurationPropertiesBindHandlerAdvisor";

    // ====== 文件注册验证 ======

    @Test
    void shouldRegisterAutoConfigurationInImportsFile() throws IOException {
        assertThat(resource("META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"))
                .contains(AUTO_CONFIGURATION_NAME);
    }

    /**
     * ConfigurationPropertiesAutoConfiguration 在 Spring Boot 2.7+ 中使用
     * AutoConfiguration.imports 注册，不在 spring.factories 的 EnableAutoConfiguration 条目下。
     * spring.factories 中注册的是 ConfigurationPropertiesBindHandlerAdvisor SPI 和
     * 其他基础设施（EnvironmentPostProcessor 等）。
     */
    @Test
    void shouldNotRegisterAutoConfigurationInSpringFactories() throws IOException {
        assertThat(resource("META-INF/spring.factories"))
                .doesNotContain(AUTO_CONFIGURATION_NAME);
    }

    @Test
    void shouldRegisterConfigurationPropertiesBindHandlerAdvisorInSpringFactories() throws IOException {
        String content = resource("META-INF/spring.factories");
        assertThat(content).contains(
                "org.springframework.boot.context.properties.ConfigurationPropertiesBindHandlerAdvisor");
        assertThat(content).contains(
                ListenableConfigurationPropertiesBindHandlerAdvisor.class.getName());
    }

    // ====== @EnableConfigurationPropertiesExtension 注册验证 ======

    @Test
    void shouldRegisterBindHandlerAdvisor() {
        contextRunner().run(context ->
                assertThat(context).hasBean(ADVISOR_BEAN_NAME));
    }

    @Test
    void shouldRegisterBindHandlerAdvisorBeanType() {
        contextRunner().run(context ->
                assertThat(context).hasSingleBean(
                        ListenableConfigurationPropertiesBindHandlerAdvisor.class));
    }

    @Test
    void shouldRegisterEventPublishingListener() {
        contextRunner().run(context ->
                assertThat(context).hasBean(
                        "eventPublishingConfigurationPropertiesBeanPropertyChangedListener"));
    }

    @Test
    void shouldRegisterEventPublishingListenerBeanType() {
        contextRunner().run(context ->
                assertThat(context).hasSingleBean(
                        EventPublishingConfigurationPropertiesBeanPropertyChangedListener.class));
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

    private ApplicationContextRunner contextRunner() {
        return new ApplicationContextRunner()
                .withUserConfiguration(EnableConfigurationPropertiesTestConfiguration.class);
    }

    @Configuration
    @EnableConfigurationPropertiesExtension
    static class EnableConfigurationPropertiesTestConfiguration {
    }
}
