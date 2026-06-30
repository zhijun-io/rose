package io.zhijun.spring.boot.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 在 {@link ApplicationReadyEvent} 时输出条件评估报告。
 */
public class ConditionEvaluationReportListener
        implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ConditionEvaluationReportListener.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        reportConditions(event.getApplicationContext());
    }

    protected void reportConditions(ConfigurableApplicationContext context) {
        ConditionsReportMessageBuilder messageBuilder = new ConditionsReportMessageBuilder(context);
        for (String message : messageBuilder.build()) {
            logger.info(message);
        }
    }
}
