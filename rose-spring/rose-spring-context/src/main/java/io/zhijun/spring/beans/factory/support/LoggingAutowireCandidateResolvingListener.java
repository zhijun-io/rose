package io.zhijun.spring.beans.factory.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;

public class LoggingAutowireCandidateResolvingListener implements AutowireCandidateResolvingListener {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAutowireCandidateResolvingListener.class);

    @Override
    public void suggestedValueResolved(DependencyDescriptor descriptor, Object suggestedValue) {
        if (suggestedValue != null) {
            log("The suggested value for {} was resolved : {}", descriptor, suggestedValue);
        }
    }

    @Override
    public void lazyProxyResolved(DependencyDescriptor descriptor, String beanName, Object proxy) {
        if (proxy != null) {
            log("The lazy proxy[descriptor : {} , bean name : '{}'] was resolved : {}", descriptor, beanName, proxy);
        }
    }

    protected void log(String messagePattern, Object... args) {
        if (logger.isTraceEnabled()) {
            logger.trace(messagePattern, args);
        }
    }
}
