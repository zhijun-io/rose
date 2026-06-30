package io.zhijun.spring.boot.env;

import java.util.Set;

import static org.springframework.core.io.support.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;

/**
 * 默认的 {@link DefaultPropertiesPostProcessor} 实现，从 {@code /META-INF/config/default/*.*} 加载属性资源。
 *
 * @see DefaultPropertiesPostProcessor
 */
public class SpringApplicationDefaultPropertiesPostProcessor implements DefaultPropertiesPostProcessor {

    public static final String DEFAULT_PROPERTIES_RESOURCES_PATTERN =
            CLASSPATH_ALL_URL_PREFIX + "/META-INF/config/default/*.*";

    @Override
    public void initializeResources(Set<String> defaultPropertiesResources) {
        defaultPropertiesResources.add(DEFAULT_PROPERTIES_RESOURCES_PATTERN);
    }
}
