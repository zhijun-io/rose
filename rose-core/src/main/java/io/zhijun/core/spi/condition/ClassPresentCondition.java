package io.zhijun.core.spi.condition;
import io.zhijun.core.spi.Condition;
import io.zhijun.core.spi.condition.annotation.OnClassPresent;
/**
 * 类存在条件实现
 */
public class ClassPresentCondition implements Condition {
    @Override
    public boolean matches(Class<?> implementationClass) {
        OnClassPresent condition = implementationClass.getAnnotation(OnClassPresent.class);
        if (condition == null) {
            return true;
        }
        String[] classNames = condition.value();
        if (classNames.length == 0) {
            return true;
        }
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ClassPresentCondition.class.getClassLoader();
        }
        boolean allMatch = condition.allMatch();
        boolean matches = condition.matches();
        int matchCount = 0;
        for (String className : classNames) {
            try {
                Class.forName(className, false, classLoader);
                matchCount++;
                if (!allMatch) {
                    // 只要有一个匹配就可以
                    break;
                }
            } catch (ClassNotFoundException e) {
                if (allMatch) {
                    // 全匹配情况下有一个不存在就失败
                    return !matches;
                }
            }
        }
        boolean result;
        if (allMatch) {
            result = matchCount == classNames.length;
        } else {
            result = matchCount > 0;
        }
        return matches == result;
    }
}
