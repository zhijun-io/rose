package io.zhijun.multitenancy.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.zhijun.multitenancy.autoconfigure.core.MultitenancyCoreAutoConfiguration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import io.zhijun.multitenancy.spring.async.TenantContextTaskDecorator;
import io.zhijun.multitenancy.core.context.TenantContext;

class MultitenancyAsyncConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MultitenancyCoreAutoConfiguration.class));

    @Test
    void shouldRegisterTenantContextTaskDecorator() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(TenantContextTaskDecorator.class));
    }

    @Test
    void shouldPropagateTenantThroughThreadPoolTaskExecutor() throws Exception {
        contextRunner.withUserConfiguration(TaskExecutorConfig.class).run(context -> {
            ThreadPoolTaskExecutor executor = context.getBean(ThreadPoolTaskExecutor.class);
            AtomicReference<String> capturedTenant = new AtomicReference<String>();
            CountDownLatch latch = new CountDownLatch(1);

            TenantContext.where("acme").run(() -> executor.execute(() -> {
                capturedTenant.set(TenantContext.getTenantId());
                latch.countDown();
            }));

            assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(capturedTenant.get()).isEqualTo("acme");
        });
    }

    @Test
    void shouldDisablePropagationWhenPropertyIsFalse() {
        contextRunner.withPropertyValues("rose.multitenancy.async.propagation-enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(TenantContextTaskDecorator.class));
    }

    @Configuration
    static class TaskExecutorConfig {
        @Bean
        ThreadPoolTaskExecutor taskExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.initialize();
            return executor;
        }
    }

}
