package io.zhijun.spring.boot.event;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static io.zhijun.spring.boot.util.SpringApplicationUtils.log;

/**
 * 日志记录 {@link OnceMainApplicationPreparedEventListener}
 */
public class LoggingOnceMainApplicationPreparedEventListener extends OnceMainApplicationPreparedEventListener {

    public LoggingOnceMainApplicationPreparedEventListener() {
        super.setOrder(LOWEST_PRECEDENCE);
    }

    @Override
    protected void onApplicationEvent(SpringApplication springApplication, String[] args, ConfigurableApplicationContext context) {
        log(springApplication, args, context, "onApplicationPreparedEvent");
    }
}
