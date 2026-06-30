package io.zhijun.spring.boot.autoconfigure.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringBootExceptionReporter;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 启动失败时输出条件评估报告的 {@link SpringBootExceptionReporter}。
 */
public class ConditionEvaluationSpringBootExceptionReporter implements SpringBootExceptionReporter {

    private static final Logger logger = LoggerFactory.getLogger(ConditionEvaluationSpringBootExceptionReporter.class);

    private final ConfigurableApplicationContext context;

    public ConditionEvaluationSpringBootExceptionReporter(ConfigurableApplicationContext context) {
        this.context = context;
    }

    @Override
    public boolean reportException(Throwable failure) {
        logger.error("Spring Boot fails to start!", failure);
        reportConditions(context);
        return false;
    }

    protected void reportConditions(ConfigurableApplicationContext context) {
        ConditionsReportMessageBuilder messageBuilder = new ConditionsReportMessageBuilder(context);
        for (String message : messageBuilder.build()) {
            logger.error(message);
        }
    }
}
