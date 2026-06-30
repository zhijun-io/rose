package io.zhijun.spring.boot.report;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.unmodifiableMap;

/**
 * Spring Boot 条件评估报告构建器，缓存每个 BeanFactory 的报告。
 */
abstract class ConditionEvaluationReportBuilder {

    private static final Map<ConfigurableListableBeanFactory, ConditionEvaluationReport> reports =
            new ConcurrentHashMap<ConfigurableListableBeanFactory, ConditionEvaluationReport>();

    static ConditionEvaluationReport build(ConfigurableListableBeanFactory beanFactory) {
        return reports.computeIfAbsent(beanFactory, ConditionEvaluationReport::get);
    }

    static Map<String, ConditionEvaluationReport> getReportsMap() {
        Map<String, ConditionEvaluationReport> reportsMap =
                new LinkedHashMap<String, ConditionEvaluationReport>(reports.size());
        for (Map.Entry<ConfigurableListableBeanFactory, ConditionEvaluationReport> entry : reports.entrySet()) {
            String id = getBeanFactoryId(entry.getKey());
            reportsMap.put(id, entry.getValue());
        }
        return unmodifiableMap(reportsMap);
    }

    static String getBeanFactoryId(ConfigurableListableBeanFactory beanFactory) {
        if (beanFactory instanceof DefaultListableBeanFactory) {
            return ((DefaultListableBeanFactory) beanFactory).getSerializationId();
        }
        return Integer.toHexString(System.identityHashCode(beanFactory));
    }

    private ConditionEvaluationReportBuilder() {}
}
