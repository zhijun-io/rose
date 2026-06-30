package io.zhijun.spring.boot.report;

import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * {@link ConditionEvaluationReport} 初始化器。
 *
 * <p>在应用上下文刷新前注册一个 BeanFactoryPostProcessor 来构建条件评估报告。
 */
public class ConditionEvaluationReportInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        applicationContext.addBeanFactoryPostProcessor(
                ConditionEvaluationReportBuilder::build);
    }
}
