package io.zhijun.spring.boot.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.springframework.util.ClassUtils.isPresent;

/**
 * Main {@link ApplicationContext} 的 OnceApplicationPreparedEventListener
 * <p>
 * 用于区分 Main 上下文和 Bootstrap 上下文（Spring Cloud）
 */
public abstract class OnceMainApplicationPreparedEventListener extends OnceApplicationPreparedEventListener {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    static final String BOOTSTRAP_APPLICATION_LISTENER_CLASS_NAME = "org.springframework.cloud.bootstrap.BootstrapApplicationListener";

    static final String BOOTSTRAP_CONTEXT_ID_PROPERTY_NAME = "spring.cloud.bootstrap.name";

    static final String DEFAULT_BOOTSTRAP_CONTEXT_ID = "bootstrap";

    @Override
    protected final boolean isIgnored(SpringApplication springApplication, String[] args, ConfigurableApplicationContext context) {
        return isIgnored(context);
    }

    protected boolean isIgnored(ConfigurableApplicationContext context) {
        if (isBootstrapApplicationListenerPresent(context)) {
            return isBootstrapContext(context) || !isMainApplicationContext(context);
        }
        return false;
    }

    boolean isBootstrapContext(ConfigurableApplicationContext context) {
        return getBootstrapContextId(context).equals(context.getId());
    }

    boolean isMainApplicationContext(ConfigurableApplicationContext context) {
        boolean main = true;
        String parentId = null;
        ApplicationContext parentContext = context.getParent();
        if (parentContext instanceof ConfigurableApplicationContext) {
            parentId = parentContext.getId();
            main = isBootstrapContext((ConfigurableApplicationContext) parentContext);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Current ApplicationContext[id: '{}', parentId: '{}'] is{}main ApplicationContext",
                    context.getId(), parentId, main ? " " : " not ");
        }
        return main;
    }

    String getBootstrapContextId(ConfigurableApplicationContext context) {
        return getBootstrapContextId(context.getEnvironment());
    }

    String getBootstrapContextId(ConfigurableEnvironment environment) {
        return environment.getProperty(BOOTSTRAP_CONTEXT_ID_PROPERTY_NAME, DEFAULT_BOOTSTRAP_CONTEXT_ID);
    }

    private boolean isBootstrapApplicationListenerPresent(ConfigurableApplicationContext context) {
        ClassLoader classLoader = context.getClassLoader();
        return isPresent(BOOTSTRAP_APPLICATION_LISTENER_CLASS_NAME, classLoader);
    }
}
