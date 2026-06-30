package io.zhijun.spring.beans.factory.support;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class JoinAliasGenerator implements ConfigurationBeanAliasGenerator {

    public String generateAlias(String prefix, String beanName, Class<?> configClass) {
        String[] prefixArray = prefix.split("\\.");
        String first = prefixArray[0];
        String others = Arrays.stream(prefixArray).skip(1)
                .map(StringUtils::capitalize)
                .collect(Collectors.joining());
        return first + others + delimiter() + beanName;
    }

    protected abstract String delimiter();
}
