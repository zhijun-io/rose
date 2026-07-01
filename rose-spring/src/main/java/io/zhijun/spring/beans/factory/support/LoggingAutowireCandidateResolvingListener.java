 package io.zhijun.spring.beans.factory.support;

 import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;

/**
 * 日志实现的 {@link AutowireCandidateResolvingListener}。
 */
public class LoggingAutowireCandidateResolvingListener implements AutowireCandidateResolvingListener {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAutowireCandidateResolvingListener.class);

    @Override
    public void suggestedValueResolved(DependencyDescriptor descriptor, Object suggestedValue) {
        if (suggestedValue != null && logger.isTraceEnabled()) {
            logger.trace("The suggested value for {} was resolved : {}", descriptor, suggestedValue);
        }
    }

    @Override
    public void lazyProxyResolved(DependencyDescriptor descriptor, String beanName, Object proxy) {
        if (proxy != null && logger.isTraceEnabled()) {
            logger.trace("The lazy proxy[descriptor : {} , bean name : '{}'] was resolved : {}", descriptor, beanName, proxy);
        }
    }
}
