package io.zhijun.spring.boot.event;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static io.zhijun.spring.boot.util.SpringApplicationUtils.DEFAULT_LOGGING_LEVEL;
import static io.zhijun.spring.boot.util.SpringApplicationUtils.getLoggingLevel;
import static io.zhijun.spring.boot.util.SpringApplicationUtils.log;

/**
 * 日志记录 {@link OnceApplicationPreparedEventListener}
 */
public class LoggingOnceApplicationPreparedEventListener extends OnceApplicationPreparedEventListener {

    public LoggingOnceApplicationPreparedEventListener() {
        super.setOrder(LOWEST_PRECEDENCE);
    }

    @Override
    protected void onApplicationEvent(SpringApplication springApplication, String[] args, ConfigurableApplicationContext context) {
        log(springApplication, args, context, "onApplicationPreparedEvent");
    }

    @Override
    protected boolean isIgnored(SpringApplication springApplication, String[] args, ConfigurableApplicationContext context) {
        String level = getLoggingLevel(context);
        return DEFAULT_LOGGING_LEVEL.equals(level);
    }
}
