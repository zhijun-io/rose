package io.zhijun.spring.boot.web.report;

import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.Collections.singleton;

/**
 * 条件评估报告消息构建器。
 *
 * <p>支持通过 {@code rose.spring.boot.conditions.report.base-packages}
 * 配置过滤的包前缀。
 */
public class ConditionsReportMessageBuilder {

    private static final String DEFAULT_BASE_PACKAGE = "io.zhijun";

    public static final String BASE_PACKAGES_PROPERTY_NAME =
            "rose.spring.boot.conditions.report.base-packages";

    private static final Set<String> DEFAULT_BASE_PACKAGES = singleton(DEFAULT_BASE_PACKAGE);

    private final ConfigurableApplicationContext context;

    public ConditionsReportMessageBuilder(ConfigurableApplicationContext context) {
        this.context = context;
    }

    List<String> build() {
        Map<String, ConditionEvaluationReport> reportsMap = ConditionEvaluationReportBuilder.getReportsMap();
        List<String> reportMessages = new ArrayList<String>(reportsMap.size());
        for (Map.Entry<String, ConditionEvaluationReport> entry : reportsMap.entrySet()) {
            reportMessages.add(buildSingle(entry.getKey(), entry.getValue()));
        }
        return reportMessages;
    }

    String buildSingle(String id, ConditionEvaluationReport report) {
        StringBuilder sb = new StringBuilder(lineSeparator());
        appendTitle(id, sb);
        appendExclusions(report, sb);
        appendUnconditionalClasses(report, sb);
        appendConditionAndOutcomes(report, sb);
        return sb.toString();
    }

    private void appendTitle(String id, StringBuilder sb) {
        appendLine(sb, "Spring Boot condition information[context: '{}']:", id);
    }

    private void appendExclusions(ConditionEvaluationReport report, StringBuilder sb) {
        appendLine(sb, "Conditional exclusion list: {}", report.getExclusions());
    }

    private void appendUnconditionalClasses(ConditionEvaluationReport report, StringBuilder sb) {
        appendLine(sb, "List of non-conditional classes: {}", report.getUnconditionalClasses());
    }

    private void appendConditionAndOutcomes(ConditionEvaluationReport report, StringBuilder sb) {
        Set<String> basePackages = getBasePackages();
        Map<String, ConditionEvaluationReport.ConditionAndOutcomes> map =
                report.getConditionAndOutcomesBySource();
        for (Map.Entry<String, ConditionEvaluationReport.ConditionAndOutcomes> entry : map.entrySet()) {
            appendConditionAndOutcomes(entry.getKey(), entry.getValue(), basePackages, sb);
        }
    }

    private void appendConditionAndOutcomes(String source,
                                            ConditionEvaluationReport.ConditionAndOutcomes outcomes,
                                            Set<String> basePackages, StringBuilder sb) {
        boolean matched = false;
        for (String basePackage : basePackages) {
            if (source.startsWith(basePackage)) {
                matched = true;
                break;
            }
        }
        if (matched) {
            appendLine(sb, "Bean definition source: '{}', {}", source, combineOutcomes(outcomes));
        }
    }

    private String combineOutcomes(ConditionEvaluationReport.ConditionAndOutcomes outcomes) {
        StringBuilder sb = new StringBuilder();
        if (outcomes.isFullMatch()) {
            sb.append("Matching conditions:");
        } else {
            sb.append("No matching conditions:");
        }
        outcomes.forEach(cao -> sb.append(cao.getOutcome().getMessage()).append(","));
        return sb.toString();
    }

    private Set<String> getBasePackages() {
        ConfigurableEnvironment environment = context.getEnvironment();
        return environment.getProperty(BASE_PACKAGES_PROPERTY_NAME, Set.class,
                DEFAULT_BASE_PACKAGES);
    }

    private void appendLine(StringBuilder sb, String text, Object... args) {
        sb.append(format(text, args)).append(lineSeparator());
    }
}
