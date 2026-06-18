package io.zhijun.spring.core.env.refresh;

import java.util.List;
import java.util.Set;

import io.zhijun.spring.core.env.listener.EnvironmentListener;
import io.zhijun.spring.core.env.event.PropertySourcesChangedEvent;
import io.zhijun.spring.core.io.support.SpringFactoriesLoaderUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Dispatches property source changes to {@link Refreshable} extensions.
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public final class PropertySourcesRefreshOrchestrator implements EnvironmentListener {

    private static final Logger logger = LoggerFactory.getLogger(PropertySourcesRefreshOrchestrator.class);

    private volatile List<Refreshable> refreshables;

    public PropertySourcesRefreshOrchestrator() {
    }

    public PropertySourcesRefreshOrchestrator(List<Refreshable> refreshables) {
        this.refreshables = refreshables;
    }

    @Override
    public void onPropertySourcesChanged(PropertySourcesChangedEvent event) {
        ApplicationContext context = (ApplicationContext) event.getSource();
        if (!RoseSpringEnvironmentRefreshProperties.isOrchestratorEnabled(context.getEnvironment())) {
            return;
        }
        Set<String> changedKeys = event.getChangedKeys();
        if (changedKeys.isEmpty()) {
            return;
        }
        dispatch(context, changedKeys);
    }

    public void onEnvironmentChangeKeys(Set<String> keys) {
        dispatch(RefreshableContextHolder.getApplicationContext(), keys);
    }

    private void dispatch(ApplicationContext context, Set<String> changedKeys) {
        if (changedKeys == null || changedKeys.isEmpty()) {
            return;
        }
        for (Refreshable refreshable : resolveRefreshables(context)) {
            try {
                if (refreshable.supports(changedKeys)) {
                    refreshable.refresh(changedKeys);
                }
            }
            catch (RuntimeException ex) {
                logger.warn("Refreshable {} failed", refreshable.getClass().getName(), ex);
            }
        }
    }

    private List<Refreshable> resolveRefreshables(ApplicationContext context) {
        if (refreshables != null) {
            return refreshables;
        }
        synchronized (this) {
            if (refreshables == null) {
                ClassLoader classLoader = context == null ? null : context.getClassLoader();
                refreshables = SpringFactoriesLoaderUtils.loadFactories(Refreshable.class, classLoader);
            }
        }
        return refreshables;
    }
}
