package io.zhijun.spring.boot.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * {@link SpringApplicationRunListener} 失败报告
 */
public class FailureReportSpringApplicationRunListener extends SpringApplicationRunListenerAdapter {

    public FailureReportSpringApplicationRunListener(SpringApplication springApplication, String[] args) {
        super(springApplication, args);
    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        System.err.print("The Spring Boot application fails to start. The causes are as follows:");
        exception.printStackTrace(System.err);
    }
}
