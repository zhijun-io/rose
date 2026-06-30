package io.zhijun.multitenancy.spring.async;

import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskDecorator;

import static org.assertj.core.api.Assertions.assertThat;

class TaskDecoratorChainTests {

    @Test
    void shouldChainOuterBeforeInner() {
        TaskDecorator outer = runnable -> () -> runnable.run();
        TaskDecorator inner = runnable -> () -> runnable.run();
        Runnable task = () -> {};

        Runnable chained = TaskDecoratorChain.chain(outer, inner).decorate(task);
        assertThat(chained).isNotSameAs(task);
    }

    @Test
    void shouldReturnTenantDecoratorWhenExistingIsNull() {
        TenantContextTaskDecorator tenantDecorator = new TenantContextTaskDecorator();
        assertThat(TaskDecoratorChain.merge(null, tenantDecorator)).isSameAs(tenantDecorator);
    }

    @Test
    void shouldPreserveExistingTenantDecorator() {
        TenantContextTaskDecorator tenantDecorator = new TenantContextTaskDecorator();
        assertThat(TaskDecoratorChain.merge(tenantDecorator, tenantDecorator)).isSameAs(tenantDecorator);
    }
}
