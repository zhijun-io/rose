package io.zhijun.spring.core.env.refresh;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.support.SpringFactoriesLoader;

import io.zhijun.spring.core.env.event.PropertySourcesChangedEvent;
import io.zhijun.spring.core.env.EnvironmentListener;

/**
 * Dispatches property source changes to {@link Refreshable} extensions.
 */
@Order
public final class PropertySourcesRefreshEnvironmentListener implements EnvironmentListener {

    private static final Logger logger = LoggerFactory.getLogger(PropertySourcesRefreshEnvironmentListener.class);

    private volatile List<Refreshable> refreshables;

    public PropertySourcesRefreshEnvironmentListener() {}

    public PropertySourcesRefreshEnvironmentListener(List<Refreshable> refreshables) {
        this.refreshables = refreshables;
    }

    @Override
    public void onPropertySourcesChanged(PropertySourcesChangedEvent event) {
        ApplicationContext context = (ApplicationContext) event.getSource();
        if (!isDispatchAllowed(context)) {
            return;
        }
        Set<String> changedKeys = event.getChangedKeys();
        if (changedKeys.isEmpty()) {
            return;
        }
        dispatch(context, changedKeys);
    }

    public void onEnvironmentChangeKeys(Set<String> keys) {
        ApplicationContext context = RefreshableContextHolder.getApplicationContext();
        if (!isDispatchAllowed(context)) {
            return;
        }
        dispatch(context, keys);
    }

    private boolean isDispatchAllowed(ApplicationContext context) {
        if (context == null) {
            return false;
        }
        if (!EnvRefreshProperties.isRefreshEnabled(context.getEnvironment())) {
            return false;
        }
        if (context instanceof ConfigurableApplicationContext) {
            return ((ConfigurableApplicationContext) context).isActive();
        }
        return true;
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
            } catch (RuntimeException ex) {
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
                refreshables = SpringFactoriesLoader.loadFactories(Refreshable.class, classLoader);
            }
        }
        return refreshables;
    }
}
