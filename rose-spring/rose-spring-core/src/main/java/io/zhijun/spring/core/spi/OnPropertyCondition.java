package io.zhijun.spring.core.spi;
import io.zhijun.core.spi.Condition;
import io.zhijun.spring.core.context.SpringContextHolder;
import io.zhijun.spring.core.spi.annotation.OnProperty;
import org.springframework.core.env.Environment;

import java.util.Arrays;
/**
 * 配置项匹配条件实现
 */
public class OnPropertyCondition implements Condition {
    @Override
    public boolean matches(Class<?> implementationClass) {
        OnProperty condition = implementationClass.getAnnotation(OnProperty.class);
        if (condition == null) {
            return true;
        }
        Environment environment = SpringContextHolder.getBean(Environment.class);
        if (environment == null) {
            // 非Spring环境，不匹配
            return !condition.matches();
        }
        String propertyName = condition.value();
        String propertyValue = environment.resolvePlaceholders(propertyName);
        // 解析占位符后的结果还是${}开头，说明配置不存在
        boolean propertyExists = !propertyValue.startsWith("${");
        if (!propertyExists) {
            return condition.matchIfMissing() == condition.matches();
        }
        // 解析配置项实际值
        String actualValue = environment.getProperty(propertyName);
        String[] expectedValues = condition.havingValue();
        boolean result;
        if (expectedValues.length == 0) {
            // 没有指定期望值，只要配置项存在就匹配
            result = actualValue != null;
        } else {
            // 匹配期望值
            result = Arrays.asList(expectedValues).contains(actualValue);
        }
        return condition.matches() == result;
    }
}
