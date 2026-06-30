package io.zhijun.spring.core.env.refresh;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

/**
 * Clears {@link RefreshableContextHolder} when the bound application context closes.
 */
public final class RefreshableContextHolderLifecycle implements ApplicationListener<ContextClosedEvent> {

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        ApplicationContext bound = RefreshableContextHolder.peekApplicationContext();
        if (bound != null && bound == event.getApplicationContext()) {
            RefreshableContextHolder.clear();
        }
    }
}
