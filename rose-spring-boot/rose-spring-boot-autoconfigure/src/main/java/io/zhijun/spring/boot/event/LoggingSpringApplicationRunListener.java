package io.zhijun.spring.boot.event;

import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static io.zhijun.spring.boot.util.SpringApplicationUtils.log;

/**
 * {@link org.springframework.boot.SpringApplicationRunListener} 日志记录
 */
public class LoggingSpringApplicationRunListener extends SpringApplicationRunListenerAdapter {

    public LoggingSpringApplicationRunListener(SpringApplication springApplication, String[] args) {
        super(springApplication, args);
    }

    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        super.starting(bootstrapContext);
        log(getSpringApplication(), getArgs(), "starting... : {}", bootstrapContext);
    }

    @Override
    public void starting() {
        super.starting();
        log(getSpringApplication(), getArgs(), "starting...");
    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        super.environmentPrepared(environment);
        log(getSpringApplication(), getArgs(), "environmentPrepared : {}", environment);
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        super.contextPrepared(context);
        log(getSpringApplication(), getArgs(), context, "contextPrepared : {}", context);
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        super.contextLoaded(context);
        log(getSpringApplication(), getArgs(), context, "contextLoaded : {}", context);
    }

    @Override
    public void started(ConfigurableApplicationContext context) {
        super.started(context);
        log(getSpringApplication(), getArgs(), context, "started : {}", context);
    }

    @Override
    public void running(ConfigurableApplicationContext context) {
        super.running(context);
        log(getSpringApplication(), getArgs(), context, "running : {}", context);
    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        super.failed(context, exception);
        log(getSpringApplication(), getArgs(), context, "failed : {}", exception);
    }
}
