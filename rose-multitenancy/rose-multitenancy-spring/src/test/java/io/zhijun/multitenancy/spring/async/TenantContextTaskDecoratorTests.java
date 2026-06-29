package io.zhijun.multitenancy.spring.async;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import io.zhijun.multitenancy.core.context.TenantContext;

class TenantContextTaskDecoratorTests {

    @Test
    void shouldPropagateTenantToAsyncTask() throws Exception {
        TenantContextTaskDecorator decorator = new TenantContextTaskDecorator();
        AtomicReference<String> capturedTenant = new AtomicReference<String>();
        CountDownLatch latch = new CountDownLatch(1);
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setTaskDecorator(decorator);

        TenantContext.where("acme")
                .run(() -> executor.execute(() -> {
                    capturedTenant.set(TenantContext.getTenantId());
                    latch.countDown();
                }));

        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(capturedTenant.get()).isEqualTo("acme");
    }

    @Test
    void shouldNotDecorateWhenTenantMissing() {
        TenantContextTaskDecorator decorator = new TenantContextTaskDecorator();
        Runnable runnable = () -> {};

        assertThat(decorator.decorate(runnable)).isSameAs(runnable);
    }
}
