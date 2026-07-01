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

    /**
     * Initializes the {@link ConditionEvaluationReport} by registering a
     * {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor} that builds
     * the report for the application context's bean factory.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConditionEvaluationReportInitializer initializer = new ConditionEvaluationReportInitializer();
     *   initializer.initialize(applicationContext);
     * }</pre>
     *
     * @param applicationContext the {@link ConfigurableApplicationContext} to initialize
     */
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        applicationContext.addBeanFactoryPostProcessor(
            ConditionEvaluationReportBuilder::build);
    }
}
