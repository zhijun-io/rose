package io.zhijun.spring.core.spi.condition;
import io.zhijun.core.spi.Condition;
import io.zhijun.spring.core.context.SpringContextHolder;
import io.zhijun.spring.core.spi.condition.annotation.OnProfile;
import org.springframework.core.env.Environment;
/**
 * Profile匹配条件实现
 */
public class OnProfileCondition implements Condition {
    @Override
    public boolean matches(Class<?> implementationClass) {
        OnProfile condition = implementationClass.getAnnotation(OnProfile.class);
        if (condition == null) {
            return true;
        }
        Environment environment = SpringContextHolder.getBean(Environment.class);
        if (environment == null) {
            // 非Spring环境，不匹配
            return !condition.matches();
        }
        String[] profiles = condition.value();
        if (profiles.length == 0) {
            return true;
        }
        boolean allMatch = condition.allMatch();
        boolean matches = condition.matches();
        int matchCount = 0;
        for (String profile : profiles) {
            if (environment.acceptsProfiles(profile)) {
                matchCount++;
                if (!allMatch) {
                    // 只要有一个匹配就可以
                    break;
                }
            }
        }
        boolean result;
        if (allMatch) {
            result = matchCount == profiles.length;
        } else {
            result = matchCount > 0;
        }
        return matches == result;
    }
}
